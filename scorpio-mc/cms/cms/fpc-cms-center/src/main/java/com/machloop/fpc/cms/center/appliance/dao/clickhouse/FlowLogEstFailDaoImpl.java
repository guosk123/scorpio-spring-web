package com.machloop.fpc.cms.center.appliance.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.cms.center.appliance.dao.FlowLogEstFailDao;
import com.machloop.fpc.cms.center.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;

import reactor.util.function.Tuple2;

/**
 * @author guosk
 *
 * create at 2022年3月31日, fpc-cms-center
 */
@Repository
public class FlowLogEstFailDaoImpl implements FlowLogEstFailDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowLogEstFailDaoImpl.class);

  private static final String TABLE_FLOW_LOG_EST_FAIL_RECORD = "d_fpc_flow_log_est_fail_record";

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.FlowLogEstFailDao#queryFlowLogs(java.lang.String, com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.cms.center.appliance.vo.FlowLogQueryVO, java.lang.String)
   */
  @Override
  public Page<Map<String, Object>> queryFlowLogs(String queryId, Pageable page,
      FlowLogQueryVO queryVO, String columns) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    Set<String> sortProperties = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    Iterator<Order> iterator = page.getSort().iterator();
    while (iterator.hasNext()) {
      sortProperties.add(iterator.next().getProperty());
    }
    StringBuilder sql = buildSelectStatement(securityQueryId, getTableName(queryVO), columns,
        sortProperties);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, sql, params);
    fillAdditionalConditions(parseQuerySource(queryVO), sql, params);

    PageUtils.appendPage(sql, page, FlowLogQueryVO.class);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs page, sql: {}, params: {}", sql.toString(), params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(result)) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
    }

    result.forEach(item -> {
      List<String> networkIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (item.get("network_id") != null) {
        String[] networkIdArray = JsonHelper.deserialize(
            JsonHelper.serialize(item.get("network_id")), new TypeReference<String[]>() {
            }, false);
        if (networkIdArray != null) {
          networkIdList = Lists.newArrayList(networkIdArray);
        }
      }
      item.put("network_id", networkIdList);
      List<String> serviceIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (item.get("service_id") != null) {
        String[] serviceIdArray = JsonHelper.deserialize(
            JsonHelper.serialize(item.get("service_id")), new TypeReference<String[]>() {
            }, false);
        if (serviceIdArray != null) {
          serviceIdList = Lists.newArrayList(serviceIdArray);
        }
      }
      item.put("service_id", serviceIdList);
    });

    return new PageImpl<>(result, page, 0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.FlowLogEstFailDao#queryFlowLogs(java.lang.String, com.machloop.fpc.cms.center.appliance.vo.FlowLogQueryVO, java.lang.String, com.machloop.alpha.common.base.page.Sort, int)
   */
  @Override
  public List<Map<String, Object>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO,
      String columns, Sort sort, int size) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    Set<String> sortProperties = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    Iterator<Order> iterator = sort.iterator();
    while (iterator.hasNext()) {
      sortProperties.add(iterator.next().getProperty());
    }
    StringBuilder sql = buildSelectStatement(securityQueryId, getTableName(queryVO), columns,
        sortProperties);

    // 过滤数据
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, sql, params);
    fillAdditionalConditions(parseQuerySource(queryVO), sql, params);

    PageUtils.appendSort(sql, sort, FlowLogQueryVO.class);
    sql.append(" limit ");
    sql.append(size);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs limit size, sql: {}, params: {}", sql.toString(), params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    result.forEach(item -> {
      List<String> networkIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (item.get("network_id") != null) {
        String[] networkIdArray = JsonHelper.deserialize(
            JsonHelper.serialize(item.get("network_id")), new TypeReference<String[]>() {
            }, false);
        if (networkIdArray != null) {
          networkIdList = Lists.newArrayList(networkIdArray);
        }
      }
      item.put("network_id", networkIdList);
      List<String> serviceIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (item.get("service_id") != null) {
        String[] serviceIdArray = JsonHelper.deserialize(
            JsonHelper.serialize(item.get("service_id")), new TypeReference<String[]>() {
            }, false);
        if (serviceIdArray != null) {
          serviceIdList = Lists.newArrayList(serviceIdArray);
        }
      }
      item.put("service_id", serviceIdList);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.FlowLogEstFailDao#countFlowLogs(java.lang.String, com.machloop.fpc.cms.center.appliance.vo.FlowLogQueryVO)
   */
  @Override
  public long countFlowLogs(String queryId, FlowLogQueryVO queryVO) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    StringBuilder totalSql = new StringBuilder(securityQueryId);
    totalSql.append("select count(1) from ");
    totalSql.append(getTableName(queryVO));

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, totalSql, params);
    fillAdditionalConditions(parseQuerySource(queryVO), totalSql, params);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("count flow logs, sql: {}, params: {}", totalSql.toString(), params);
    }

    return queryForLongWithExceptionHandle(totalSql.toString(), params);
  }

  private String getTableName(FlowLogQueryVO queryVO) {
    return TABLE_FLOW_LOG_EST_FAIL_RECORD;
  }

  private StringBuilder buildSelectStatement(String queryId, String tableName, String columns,
      Set<String> sortPropertys) {
    StringBuilder sql = new StringBuilder(queryId);

    if (!StringUtils.equals(columns, "*")) {
      // 必须查询排序字段
      sortPropertys = sortPropertys.stream()
          .filter(sortProperty -> !StringUtils.contains(columns, sortProperty))
          .collect(Collectors.toSet());

      sql.append("select ").append(CsvUtils.convertCollectionToCSV(sortPropertys));
      if (StringUtils.isNotBlank(columns)) {
        sql.append(CollectionUtils.isNotEmpty(sortPropertys) ? "," : "").append(columns);
      }
      sql.append(" from ").append(tableName);
      return sql;
    }

    sql.append("select network_id, service_id, start_time, report_time, ");
    sql.append(" ipv4_initiator, ipv4_responder, ipv6_initiator, ipv6_responder, ");
    sql.append(" ip_locality_initiator, ip_locality_responder, ");
    sql.append(" port_initiator, port_responder, tcp_session_state ");
    sql.append(" from ").append(tableName);
    return sql;
  }

  private void enrichWhereSql(FlowLogQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {
    whereSql.append(" where 1 = 1 ");

    // 使用dsl表达式查询
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String, Map<String, Object>> dsl = dslConverter.converte(queryVO.getDsl(), false,
            queryVO.getTimePrecision(), queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime());
        whereSql.append(" and ");
        whereSql.append(dsl.getT1());
        params.putAll(dsl.getT2());
        return;
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), whereSql, params);

    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      whereSql.append(" and has(network_id, :networkId)=1 ");
      params.put("networkId", queryVO.getNetworkId());
    }
    if (StringUtils.isNotBlank(queryVO.getServiceId())) {
      whereSql.append(" and has(service_id, :serviceId)=1 ");
      params.put("serviceId", queryVO.getServiceId());
    }
  }

  /**
   * 解析实际查询的网络业务
   * @param queryVO
   * @return
   */
  private List<Map<String, Object>> parseQuerySource(FlowLogQueryVO queryVO) {
    List<Map<String, Object>> sourceConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      sourceConditions = queryVO.getNetworkIds().stream().map(networkId -> {
        Map<String, Object> termKey = Maps.newHashMap();
        termKey.put("network_id", networkId);
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
            conditionSql.append(" has(");
            conditionSql.append(entry.getKey()).append(" , :").append(entry.getKey()).append(index);
            conditionSql.append(" )=1");
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

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    whereSql.append(String.format(" and report_time %s toDateTime64(:start_time, 9, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and report_time %s toDateTime64(:end_time, 9, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  private <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result.addAll(clickHouseTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper));
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query flow log has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query flow log failed, error msg: {}",
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
        LOGGER.info("query flow log has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query flow log failed, error msg: {}",
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

}
