package com.machloop.fpc.cms.center.global.dao.impl;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao;
import com.machloop.fpc.cms.center.broker.data.FpcNetworkDO;
import com.machloop.fpc.cms.center.global.dao.CounterDao;
import com.machloop.fpc.cms.center.global.data.CounterQuery;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2022年3月28日, fpc-manager
 */
@Repository
public class CounterDaoImpl implements CounterDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(CounterDaoImpl.class);

  private static final String TABLE_METADATA_COUNTER = "d_fpc_metadata_counter";

  private static final List<String> BASE_FILTER = Lists.newArrayList("network_id", "service_id");

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private FpcNetworkDao fpcNetworkDao;

  /**
   * @see com.machloop.fpc.cms.center.global.dao.CounterDao#onlyBaseFilter(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public boolean onlyBaseFilter(String sourceType, String dsl, String dataType) {
    if (StringUtils.equals(sourceType, FpcCmsConstants.SOURCE_TYPE_PACKET_FILE)) {
      return false;
    }

    List<Map<String, Object>> filterContents = getFilterContents(dsl);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("dsl filter: {}", JsonHelper.serialize(filterContents, false));
    }

    for (Map<String, Object> filterContent : filterContents) {
      String field = MapUtils.getString(filterContent, "field");
      if (BASE_FILTER.contains(field)) {
        continue;
      } else {
        String operator = MapUtils.getString(filterContent, "operator");
        Object operand = filterContent.get("operand");
        switch (dataType) {
          case FLOW_LOG:
            if (!StringUtils.equals(field, "total_packets") || !StringUtils.equals(operator, ">")
                || (int) operand != 0) {
              return false;
            }
            break;
          case DHCP:
            Map<String, String> dhcpVersionDict = dictManager.getBaseDict()
                .getItemMap("protocol_dhcp_version");
            if (!StringUtils.equals(field, "version") || !StringUtils.equals(operator, "=")
                || !dhcpVersionDict.containsKey(String.valueOf(operand))) {
              return false;
            }
            break;
          case ICMP:
            Map<String, String> icmpVersionDict = dictManager.getBaseDict()
                .getItemMap("protocol_icmp_version");
            if (!StringUtils.equals(field, "version") || !StringUtils.equals(operator, "=")
                || !icmpVersionDict.containsKey(String.valueOf(operand))) {
              return false;
            }
            break;
          case MAIL:
            Map<String, String> protocolDict = dictManager.getBaseDict()
                .getItemMap("protocol_mail_protocol");
            if (!StringUtils.equals(field, "protocol") || !StringUtils.equals(operator, "=")
                || !protocolDict.containsKey(StringUtils.upperCase(String.valueOf(operand)))) {
              return false;
            }
            break;
          default:
            return false;
        }
      }
    }

    return true;
  }

  /**
   * @see com.machloop.fpc.cms.center.global.dao.CounterDao#countFlowLogs(java.lang.String, com.machloop.fpc.cms.center.global.data.CounterQuery)
   */
  @Override
  public long countFlowLogs(String queryId, CounterQuery queryVO) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    StringBuilder countSql = new StringBuilder(securityQueryId);
    countSql.append("select sum(flow_cnt) from ");
    countSql.append(TABLE_METADATA_COUNTER);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, countSql, params);
    fillAdditionalConditions(parseQuerySource(queryVO), countSql, params);

    return queryForLongWithExceptionHandle(countSql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.global.dao.CounterDao#countProtocolLogRecord(com.machloop.fpc.cms.center.global.data.CounterQuery, java.lang.String)
   */
  @Override
  public long countProtocolLogRecord(CounterQuery queryVO, String protocol) {
    List<Map<String, Object>> filterContents = getFilterContents(queryVO.getDsl());
    switch (protocol) {
      case DHCP:
        Map<String,
            String> dhcpVersionDict = dictManager.getBaseDict().getItemMap("protocol_dhcp_version");
        String dhcpVersion = filterContents.stream()
            .filter(item -> StringUtils.equals(MapUtils.getString(item, "field"), "version"))
            .map(item -> MapUtils.getString(item, "operand")).collect(Collectors.toList()).get(0);
        protocol = dhcpVersionDict.get(dhcpVersion);
        break;
      case ICMP:
        Map<String,
            String> icmpVersionDict = dictManager.getBaseDict().getItemMap("protocol_icmp_version");
        String icmpVersion = filterContents.stream()
            .filter(item -> StringUtils.equals(MapUtils.getString(item, "field"), "version"))
            .map(item -> MapUtils.getString(item, "operand")).collect(Collectors.toList()).get(0);
        protocol = icmpVersionDict.get(icmpVersion);
        break;
      case MAIL:
        Map<String,
            String> protocolDict = dictManager.getBaseDict().getItemMap("protocol_mail_protocol");
        String mailProtocol = filterContents.stream()
            .filter(item -> StringUtils.equals(MapUtils.getString(item, "field"), "protocol"))
            .map(item -> MapUtils.getString(item, "operand")).collect(Collectors.toList()).get(0);
        protocol = protocolDict.get(StringUtils.upperCase(mailProtocol));
        break;
      default:
        break;
    }

    StringBuilder countSql = new StringBuilder();
    countSql.append("select ");
    Tuple2<AggsFunctionEnum, String> protocolAggs = protocolAggs().get(protocol);
    countSql.append(protocolAggs.getT1().getOperation()).append("(").append(protocolAggs.getT2())
        .append(") AS ").append(protocol);
    countSql.append(" from ").append(TABLE_METADATA_COUNTER);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, countSql, params);
    fillAdditionalConditions(parseQuerySource(queryVO), countSql, params);

    return queryForLongWithExceptionHandle(countSql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.global.dao.CounterDao#countProtocolLogRecords(com.machloop.fpc.cms.center.global.data.CounterQuery)
   */
  @Override
  public Map<String, Long> countProtocolLogRecords(CounterQuery queryVO) {
    StringBuilder countSql = new StringBuilder();
    countSql.append("select ");
    Iterator<Entry<String, Tuple2<AggsFunctionEnum, String>>> iterator = protocolAggs().entrySet()
        .iterator();
    while (iterator.hasNext()) {
      Entry<String, Tuple2<AggsFunctionEnum, String>> entry = iterator.next();
      countSql.append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());

      if (iterator.hasNext()) {
        countSql.append(", ");
      }
    }
    countSql.append(" from ").append(TABLE_METADATA_COUNTER);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, countSql, params);
    fillAdditionalConditions(parseQuerySource(queryVO), countSql, params);

    List<Map<String, Object>> list = queryWithExceptionHandle(countSql.toString(), params,
        new ColumnMapRowMapper());

    if (CollectionUtils.isNotEmpty(list)) {
      return list.get(0).entrySet().stream().collect(Collectors.toMap(Entry::getKey,
          entry -> Long.parseLong(String.valueOf(entry.getValue()))));
    } else {
      return Maps.newHashMapWithExpectedSize(0);
    }
  }

  private Map<String, Tuple2<AggsFunctionEnum, String>> protocolAggs() {
    Map<String, Tuple2<AggsFunctionEnum, String>> aggFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggFields.put("ARP", Tuples.of(AggsFunctionEnum.SUM, "arp_cnt"));
    aggFields.put("DHCP", Tuples.of(AggsFunctionEnum.SUM, "dhcp_cnt"));
    aggFields.put("DHCPv6", Tuples.of(AggsFunctionEnum.SUM, "dhcpv6_cnt"));
    aggFields.put("DNS", Tuples.of(AggsFunctionEnum.SUM, "dns_cnt"));
    aggFields.put("FTP", Tuples.of(AggsFunctionEnum.SUM, "ftp_cnt"));
    aggFields.put("HTTP", Tuples.of(AggsFunctionEnum.SUM, "http_cnt"));
    aggFields.put("ICMPv4", Tuples.of(AggsFunctionEnum.SUM, "icmpv4_cnt"));
    aggFields.put("ICMPv6", Tuples.of(AggsFunctionEnum.SUM, "icmpv6_cnt"));
    aggFields.put("POP3", Tuples.of(AggsFunctionEnum.SUM, "pop3_cnt"));
    aggFields.put("IMAP", Tuples.of(AggsFunctionEnum.SUM, "imap_cnt"));
    aggFields.put("SMTP", Tuples.of(AggsFunctionEnum.SUM, "smtp_cnt"));
    aggFields.put("MYSQL", Tuples.of(AggsFunctionEnum.SUM, "mysql_cnt"));
    aggFields.put("OSPF", Tuples.of(AggsFunctionEnum.SUM, "ospf_cnt"));
    aggFields.put("POSTGRESQL", Tuples.of(AggsFunctionEnum.SUM, "postgresql_cnt"));
    aggFields.put("SOCKS5", Tuples.of(AggsFunctionEnum.SUM, "socks5_cnt"));
    aggFields.put("SOCKS4", Tuples.of(AggsFunctionEnum.SUM, "socks4_cnt"));
    aggFields.put("DB2", Tuples.of(AggsFunctionEnum.SUM, "db2_cnt"));
    aggFields.put("SIP", Tuples.of(AggsFunctionEnum.SUM, "sip_cnt"));
    aggFields.put("SSH", Tuples.of(AggsFunctionEnum.SUM, "ssh_cnt"));
    aggFields.put("SSL", Tuples.of(AggsFunctionEnum.SUM, "ssl_cnt"));
    aggFields.put("TDS", Tuples.of(AggsFunctionEnum.SUM, "tds_cnt"));
    aggFields.put("TELNET", Tuples.of(AggsFunctionEnum.SUM, "telnet_cnt"));
    aggFields.put("TNS", Tuples.of(AggsFunctionEnum.SUM, "tns_cnt"));
    aggFields.put("LDAP", Tuples.of(AggsFunctionEnum.SUM, "ldap_cnt"));

    return aggFields;
  }

  /**
   * 解析dsl
   * @param dsl
   * @return
   */
  private List<Map<String, Object>> getFilterContents(String dsl) {
    List<Map<String, Object>> filterContents = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(dsl)) {
      try {
        filterContents = spl2SqlHelper.getFilterContent(dsl);
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    return filterContents;
  }

  /**
   * 完善过滤条件
   * @param dsl
   * @param queryVO
   * @param whereSql
   * @param params
   */
  private void enrichWhereSql(CounterQuery queryVO, StringBuilder whereSql,
      Map<String, Object> params) {
    whereSql.append(" where 1 = 1 ");

    // 使用dsl表达式查询
    List<Map<String, Object>> filterContents = getFilterContents(queryVO.getDsl());
    String networkId = "";
    String serviceId = "";
    for (Map<String, Object> filterContent : filterContents) {
      String field = MapUtils.getString(filterContent, "field");
      String operand = MapUtils.getString(filterContent, "operand");
      if (StringUtils.equals(field, "network_id")) {
        networkId = operand;
      }
      if (StringUtils.equals(field, "service_id")) {
        serviceId = operand;
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), whereSql, params);

    if (CollectionUtils.isEmpty(queryVO.getNetworkIds())
        && CollectionUtils.isEmpty(queryVO.getServiceNetworkIds())) {

      networkId = StringUtils.defaultIfBlank(networkId, queryVO.getNetworkId());
      if (StringUtils.isNotBlank(networkId)) {
        whereSql.append(" and network_id = :networkId ");
        params.put("networkId", networkId);
      } else {
        // 查询探针上的有效物理网络
        List<String> networkIds = fpcNetworkDao.queryFpcNetworks(null).stream()
            .map(FpcNetworkDO::getFpcNetworkId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(networkIds)) {
          whereSql.append(" and network_id in (:networkIds) ");
          params.put("networkIds", networkIds);
        } else {
          whereSql.append(" and 1=2 ");
        }
      }

      whereSql.append(" and service_id = :serviceId ");
      params.put("serviceId", StringUtils
          .defaultIfBlank(StringUtils.defaultIfBlank(serviceId, queryVO.getServiceId()), ""));
    }
  }

  /**
   * 过滤时间段
   * @param startTime
   * @param endTime
   * @param includeStartTime
   * @param includeEndTime
   * @param whereSql
   * @param params
   */
  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    whereSql.append(String.format(" and start_time %s toDateTime64(:start_time, 9, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and start_time %s toDateTime64(:end_time, 9, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  private Long queryForLongWithExceptionHandle(String sql, Map<String, ?> paramMap) {
    Long result = 0L;
    try {
      result = clickHouseTemplate.getJdbcTemplate().queryForObject(sql, paramMap, Long.class);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query metadata counter has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query metadata counter failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else if (errorCode == 60 || errorCode == 102) {
        // 60(UNKNOWN_TABLE，表不存在)：集群内没有探针节点，仅存在默认节点，默认节点会从本地查找数据表，此时就会返回表不存在的错误码
        LOGGER.info("not found sensor node.");
      } else if (errorCode == 279 || errorCode == 210 || errorCode == 209) {
        // 279 (ALL_CONNECTION_TRIES_FAILED) 探针上clickhouse端口不通
        // 210 (NETWORK_ERROR)
        // 209 (SOCKET_TIMEOUT)
        LOGGER.warn("connection sensor clikhousenode error.", e.getMessage());
      } else {
        // 其他异常重新抛出
        throw e;
      }
    }
    return result;
  }

  private <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result = clickHouseTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query metadata counter has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query metadata counter failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else if (errorCode == 60 || errorCode == 102) {
        // 60(UNKNOWN_TABLE，表不存在)：集群内没有探针节点，仅存在默认节点，默认节点会从本地查找数据表，此时就会返回表不存在的错误码
        LOGGER.info("not found sensor node.");
      } else if (errorCode == 279 || errorCode == 210 || errorCode == 209) {
        // 279 (ALL_CONNECTION_TRIES_FAILED) 探针上clickhouse端口不通
        // 210 (NETWORK_ERROR)
        // 209 (SOCKET_TIMEOUT)
        LOGGER.warn("connection sensor clikhousenode error.", e.getMessage());
      } else {
        // 其他异常重新抛出
        throw e;
      }
    }
    return result;
  }

  /**
   * 解析实际查询的网络业务
   * @param queryVO
   * @return
   */
  private List<Map<String, Object>> parseQuerySource(CounterQuery queryVO) {
    List<Map<String, Object>> sourceConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      sourceConditions = queryVO.getNetworkIds().stream().map(networkId -> {
        Map<String, Object> termKey = Maps.newHashMap();
        termKey.put("network_id", networkId);
        termKey.put("service_id", "");
        return termKey;
      }).collect(Collectors.toList());
    } else if (CollectionUtils.isNotEmpty(queryVO.getServiceNetworkIds())) {
      sourceConditions = queryVO.getServiceNetworkIds().stream().map(serviceNetworkId -> {
        Map<String, Object> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        termKey.put("service_id", serviceNetworkId.getT1());
        termKey.put("network_id", serviceNetworkId.getT2());

        return termKey;
      }).collect(Collectors.toList());
    }

    return sourceConditions;
  }

  /**
   * 增加额外过滤条件，格式：[{"W":xx},{"A":x1, "B":y1},{"A":x2, "B":y2}]，</br>
   * 单个过滤组合内条件用 "and" 拼接，</br>
   * 过滤组合相同的项将通过 "or" 连接，作为一整项过滤，</br>
   * 过滤组合不同的项用 "and" 连接；</br>
   * 最终过滤语句如下：where xxx and has(W, xx) =1 and (has(A, x1) =1 and has(B, y1) =1) or (has(A, x2) =1 and has(B, y2) =1)
   * @param additionalConditions
   * @param whereSql
   * @param params
   */
  protected void fillAdditionalConditions(List<Map<String, Object>> additionalConditions,
      StringBuilder whereSql, Map<String, Object> params) {
    // 添加附加条件
    if (CollectionUtils.isNotEmpty(additionalConditions)) {
      Map<String,
          List<String>> conditions = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      int index = 0;
      for (Map<String, Object> condition : additionalConditions) {
        StringBuilder conditionSql = new StringBuilder("(");
        Iterator<Entry<String, Object>> iterator = condition.entrySet().iterator();
        while (iterator.hasNext()) {
          Entry<String, Object> entry = iterator.next();
          if (entry.getValue() == null) {
            conditionSql.append("isNull(").append(entry.getKey()).append(")");
          } else {
            conditionSql.append(entry.getKey()).append(" = :").append(entry.getKey()).append(index);
            params.put(entry.getKey() + index, entry.getValue());
          }

          if (iterator.hasNext()) {
            conditionSql.append(" and ");
          }
        }
        conditionSql.append(")");
        String conditionKey = StringUtils.join(condition.keySet(), "_");
        List<String> list = conditions.getOrDefault(conditionKey,
            Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
        list.add(conditionSql.toString());
        conditions.put(conditionKey, list);

        index++;
      }

      conditions.values().forEach(conditionSqlList -> {
        whereSql.append(" and (").append(StringUtils.join(conditionSqlList, " or ")).append(")");
      });
    }
  }

}
