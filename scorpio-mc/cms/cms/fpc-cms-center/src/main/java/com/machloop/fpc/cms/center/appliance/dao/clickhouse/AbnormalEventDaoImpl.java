package com.machloop.fpc.cms.center.appliance.dao.clickhouse;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.cms.center.appliance.dao.AbnormalEventDao;
import com.machloop.fpc.cms.center.appliance.data.AbnormalEventDO;
import com.machloop.fpc.cms.center.appliance.vo.AbnormalEventQueryVO;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;

/**
 * @author guosk
 *
 * create at 2021年7月15日, fpc-manager
 */
@Repository
public class AbnormalEventDaoImpl implements AbnormalEventDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbnormalEventDaoImpl.class);

  private static final String TABLE_ANALYSIS_ABNORMAL_EVENT = "d_fpc_analysis_abnormal_event";

  private static final String METRIC_TYPE_TYPE = "type";
  private static final String METRIC_TYPE_LOCATION_INITIATOR = "locationInitiator";
  private static final String METRIC_TYPE_LOCATION_RESPONDER = "locationResponder";

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  /** 
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventDao#queryAbnormalEvents(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.npm.analysis.vo.AbnormalEventQueryVO)
   */
  @Override
  public Page<AbnormalEventDO> queryAbnormalEvents(Pageable page, AbnormalEventQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where 1 = 1 ");
    if (queryVO.getStartTimeDate() != null) {
      whereSql.append(" and start_time >= toDateTime64(:startTime, 3, 'UTC') ");
      params.put("startTime", DateUtils.toStringFormat(queryVO.getStartTimeDate(),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (queryVO.getEndTimeDate() != null) {
      whereSql.append(" and start_time <= toDateTime64(:endTime, 3, 'UTC') ");
      params.put("endTime", DateUtils.toStringFormat(queryVO.getEndTimeDate(),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      whereSql.append(" and network_id = :networkId ");
      params.put("networkId", queryVO.getNetworkId());
    }
    if (queryVO.getType() != null) {
      whereSql.append(" and type = :type ");
      params.put("type", queryVO.getType());
    }
    if (StringUtils.isNotBlank(queryVO.getContent())) {
      whereSql.append(" and content like :content ");
      params.put("content", "%" + queryVO.getContent() + "%");
    }
    if (StringUtils.isNoneBlank(queryVO.getIpAddress())) {
      if (!NetworkUtils.isInetAddress(queryVO.getIpAddress())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的IP地址");
      }

      String ipVersion = NetworkUtils.isInetAddress(queryVO.getIpAddress(), IpVersion.V4) ? "v4"
          : "v6";
      whereSql.append(
          String.format(" and (src_ip%s = toIP%s(:ipAddress) or dest_ip%s = toIP%s(:ipAddress)) ",
              ipVersion, ipVersion, ipVersion, ipVersion));
      params.put("ipAddress", queryVO.getIpAddress());
    }
    if (queryVO.getL7ProtocolId() != null) {
      whereSql.append(" and l7_protocol_id = :l7ProtocolId ");
      params.put("l7ProtocolId", queryVO.getL7ProtocolId());
    }
    sql.append(whereSql);

    PageUtils.appendPage(sql, page, AbnormalEventDO.class);

    List<Map<String, Object>> abnormalEventList = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(1) from ");
    totalSql.append(TABLE_ANALYSIS_ABNORMAL_EVENT);
    totalSql.append(whereSql);

    Long total = queryForLongWithExceptionHandle(totalSql.toString(), params);

    List<AbnormalEventDO> resultList = abnormalEventList.stream()
        .map(item -> convertAbnormalEventMap2DO(item)).collect(Collectors.toList());
    return new PageImpl<>(resultList, page, total);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventDao#queryAbnormalEvents(com.machloop.fpc.npm.analysis.vo.AbnormalEventQueryVO, int)
   */
  @Override
  public List<AbnormalEventDO> queryAbnormalEvents(AbnormalEventQueryVO queryVO, int size) {
    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(" where 1=1 ");
    if (queryVO.getStartTimeDate() != null) {
      sql.append(" and start_time >= toDateTime64(:startTime, 3, 'UTC') ");
      params.put("startTime", DateUtils.toStringFormat(queryVO.getStartTimeDate(),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (queryVO.getEndTimeDate() != null) {
      sql.append(" and start_time <= toDateTime64(:endTime, 3, 'UTC') ");
      params.put("endTime", DateUtils.toStringFormat(queryVO.getEndTimeDate(),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }

    sql.append(" order by start_time asc ");
    sql.append(" limit ").append(size);

    List<Map<String, Object>> abnormalEventList = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    return CollectionUtils.isEmpty(abnormalEventList) ? Lists.newArrayListWithCapacity(0)
        : abnormalEventList.stream().map(item -> convertAbnormalEventMap2DO(item))
            .collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventDao#countAbnormalEvent(java.util.Date, java.util.Date, java.util.List, int)
   */
  @Override
  public List<Map<String, Object>> countAbnormalEvent(Date startTime, Date endTime,
      String metricType, int count) {
    Map<String, String> termFields = getTermFields(metricType);

    StringBuilder sql = new StringBuilder();
    sql.append("select count(1) count, ");
    sql.append(StringUtils.join(termFields.keySet(), ","));
    sql.append(" from ").append(TABLE_ANALYSIS_ABNORMAL_EVENT);
    sql.append(" where ")
        .append(StringUtils.join(termFields.values().stream()
            .filter(filter -> StringUtils.isNotBlank(filter)).collect(Collectors.toList()),
            " and "));

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (startTime != null) {
      sql.append(" and start_time >= toDateTime64(:startTime, 3, 'UTC') ");
      params.put("startTime",
          DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (endTime != null) {
      sql.append(" and start_time <= toDateTime64(:endTime, 3, 'UTC') ");
      params.put("endTime",
          DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }

    sql.append(" group by ").append(StringUtils.join(termFields.keySet(), ","));
    sql.append(" order by count desc");
    if (count > 0) {
      sql.append(" limit ").append(count);
    }

    List<Map<String, Object>> list = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append(
        "select id, start_time, network_id, type, content, description, src_ipv4, src_ipv6, ");
    sql.append(" dest_ipv4, dest_ipv6, dest_port, l7_protocol_id, country_id_initiator, ");
    sql.append(" province_id_initiator, city_id_initiator, country_id_responder, ");
    sql.append(" province_id_responder, city_id_responder ");
    sql.append(" from ").append(TABLE_ANALYSIS_ABNORMAL_EVENT);
    return sql;
  }

  /**
   * 获取统计维度分组字段
   * @param metricType
   * @return <分组字段，过滤条件>
   */
  private Map<String, String> getTermFields(String metricType) {
    Map<String, String> termFields = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    switch (metricType) {
      case METRIC_TYPE_TYPE:
        termFields.put("type", "isNotNull(type)");
        break;
      case METRIC_TYPE_LOCATION_INITIATOR:
        termFields.put("country_id_initiator", "isNotNull(country_id_initiator)");
        termFields.put("province_id_initiator", null);
        break;
      case METRIC_TYPE_LOCATION_RESPONDER:
        termFields.put("country_id_responder", "isNotNull(country_id_responder)");
        termFields.put("province_id_responder", null);
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的统计维度");
    }

    return termFields;
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
        LOGGER.info("queryFlowLogs has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("queryFlowLogs failed, error msg: {}",
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
        LOGGER.info("queryFlowLogs has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("queryFlowLogs failed, error msg: {}",
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

  private AbnormalEventDO convertAbnormalEventMap2DO(Map<String, Object> map) {
    AbnormalEventDO abnormalEventDO = new AbnormalEventDO();

    abnormalEventDO.setId(MapUtils.getString(map, "id"));
    abnormalEventDO.setStartTime(MapUtils.getString(map, "start_time"));
    abnormalEventDO.setNetworkId(MapUtils.getString(map, "network_id"));
    abnormalEventDO.setType(MapUtils.getIntValue(map, "type"));
    abnormalEventDO.setContent(MapUtils.getString(map, "content"));
    abnormalEventDO.setDescription(MapUtils.getString(map, "description"));
    Inet4Address srcIpv4 = (Inet4Address) map.get("src_ipv4");
    abnormalEventDO.setSrcIpv4(srcIpv4 != null ? srcIpv4.getHostAddress() : null);
    Inet6Address srcIpv6 = (Inet6Address) map.get("src_ipv6");
    abnormalEventDO.setSrcIpv6(srcIpv6 != null ? srcIpv6.getHostAddress() : null);
    Inet4Address destIpv4 = (Inet4Address) map.get("dest_ipv4");
    abnormalEventDO.setDestIpv4(destIpv4 != null ? destIpv4.getHostAddress() : null);
    Inet6Address destIpv6 = (Inet6Address) map.get("dest_ipv6");
    abnormalEventDO.setDestIpv6(destIpv6 != null ? destIpv6.getHostAddress() : null);
    abnormalEventDO.setDestPort(MapUtils.getIntValue(map, "dest_port"));
    abnormalEventDO.setL7ProtocolId(MapUtils.getIntValue(map, "l7_protocol_id"));
    abnormalEventDO.setCountryIdInitiator(MapUtils.getInteger(map, "country_id_initiator"));
    abnormalEventDO.setProvinceIdInitiator(MapUtils.getInteger(map, "province_id_initiator"));
    abnormalEventDO.setCityIdInitiator(MapUtils.getInteger(map, "city_id_initiator"));
    abnormalEventDO.setCountryIdResponder(MapUtils.getInteger(map, "country_id_responder"));
    abnormalEventDO.setProvinceIdResponder(MapUtils.getInteger(map, "province_id_responder"));
    abnormalEventDO.setCityIdResponder(MapUtils.getInteger(map, "city_id_responder"));

    return abnormalEventDO;
  }

}
