package com.machloop.fpc.cms.center.metric.dao.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.metric.dao.MetricSensorNetworkFlowDao;
import com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年2月8日, fpc-cms-center
 */
@Repository
public class MetricSensorNetworkFlowDaoImpl implements MetricSensorNetworkFlowDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricSensorNetworkFlowDaoImpl.class);

  private static final int SCALE_COUNTS = 4;

  private static final String DIVIDE_NULL_NAN = "NaN";
  private static final String DIVIDE_NULL_INF = "Infinity";

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricSensorNetworkFlowDao#querySensorNetworkByMetric(java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryAllNetworkMetrics(MetricSensorQueryVO queryVO) {

    StringBuilder sql = new StringBuilder();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(" select network_id,  ");
    if (StringUtils.equals(queryVO.getMetric(), CenterConstants.TOTAL_BYTES)) {
      sql.append(" sum(total_bytes) as total_bytes ");
    }
    if (StringUtils.equals(queryVO.getMetric(), CenterConstants.CONCURRENT_SESSIONS)) {
      sql.append(" max(concurrent_sessions) as concurrent_sessions ");
    }
    if (StringUtils.equals(queryVO.getMetric(), CenterConstants.ESTABLISHED_SESSIONS)) {
      sql.append(" sum(established_sessions) as established_sessions ");
    }
    sql.append(" from ").append(getTableName(queryVO.getInterval()));
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(), true, false,
        whereSql, params);
    sql.append(whereSql.toString());
    sql.append(" group by network_id ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryAllNetworkMetrics sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricSensorNetworkFlowDao#querySensorNetworkHistogramByMetric(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> querySensorNetworkHistogramByMetric(MetricSensorQueryVO queryVO,
      List<String> networkIdList) {

    StringBuilder sql = new StringBuilder();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(
        " select toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') as temp_timestamp, ");
    sql.append(" sum(").append(queryVO.getMetric()).append(") as ").append(queryVO.getMetric());
    sql.append(" from ").append(getTableName(queryVO.getInterval()));
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(), false, false,
        whereSql, params);
    sql.append(whereSql.toString());
    sql.append(" and network_id in (:networkIdList) ");
    sql.append(" group by temp_timestamp ");
    sql.append(" order by temp_timestamp asc ");

    params.put("interval", queryVO.getInterval());
    params.put("networkIdList", networkIdList);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("querySensorNetworkHistogramByMetric sql : [{}], param: [{}] ", sql.toString(),
          params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    result.forEach(item -> {
      item.put("timestamp", item.get("temp_timestamp"));
      item.remove("temp_timestamp");
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricSensorNetworkFlowDao#queryEstablishedSuccessRateHistogram(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryEstablishedSuccessRateHistogram(MetricSensorQueryVO queryVO,
      List<String> networkIdList) {
    StringBuilder sql = new StringBuilder();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(
        " select toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') as temp_timestamp, ");
    sql.append(" sum(tcp_established_success_counts) as success, ");
    sql.append(" sum(established_tcp_sessions) as total, ");
    sql.append(" divide(success, total) as ratio ");
    sql.append(" from ").append(getTableName(queryVO.getInterval()));
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(), false, false,
        whereSql, params);

    whereSql.append(" and network_id in (:networkIdList) ");
    sql.append(whereSql.toString());

    sql.append(" group by temp_timestamp ");
    sql.append(" order by temp_timestamp asc ");

    params.put("interval", queryVO.getInterval());
    params.put("networkIdList", networkIdList);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryEstablishedSuccessRateHistogram sql : [{}], param: [{}] ", sql.toString(),
          params);
    }

    List<Map<String, Object>> metricResult = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    metricResult.forEach(item -> {
      item.put("timestamp", item.get("temp_timestamp"));
      item.put("ratio",
          StringUtils.equalsAny(String.valueOf(item.get("ratio")), DIVIDE_NULL_NAN, DIVIDE_NULL_INF)
              ? 0
              : new BigDecimal(String.valueOf(item.get("ratio"))).setScale(SCALE_COUNTS,
                  RoundingMode.HALF_UP));
      item.remove("temp_timestamp");
    });

    return metricResult;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricSensorNetworkFlowDao#queryAllNetworkTcpEstablishSessionCount()
   */
  @Override
  public List<Map<String, Object>> queryAllNetworkTcpEstablishedSessionCount(
      MetricSensorQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(" select network_id, sum(tcp_established_success_counts) as success, ");
    sql.append(" sum(established_tcp_sessions) as total ");
    sql.append(" from ").append(getTableName(queryVO.getInterval()));
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(), false, false,
        whereSql, params);
    sql.append(whereSql.toString());

    sql.append(" group by network_id ");

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    return result;
  }

  private String getTableName(int interval) {
    String tableName = CenterConstants.TABLE_METRIC_NETWORK_DATA_RECORD;
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName = tableName + "_5m";
    } else if (interval >= Constants.ONE_HOUR_SECONDS) {
      tableName = tableName + "_1h";
    }
    return tableName;
  }

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    whereSql.append(" where 1=1 ");
    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    whereSql.append(String.format(" and timestamp %s toDateTime64(:start_time, 9, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and timestamp %s toDateTime64(:end_time, 9, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  protected <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
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
        LOGGER.info("query flow metric has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query flow metric failed, error msg: {}",
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
