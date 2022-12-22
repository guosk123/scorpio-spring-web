package com.machloop.fpc.cms.center.central.dao.clickhouse;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.machloop.fpc.cms.center.central.dao.FpcDiskIODao;
import com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO;

/**
 * @author guosk
 *
 * create at 2021年12月7日, fpc-cms-center
 */
@Repository
public class FpcDiskIODaoImpl implements FpcDiskIODao {

  private static final Logger LOGGER = LoggerFactory.getLogger(FpcDiskIODaoImpl.class);

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDiskIODao#queryDiskIO(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryDiskIO(MetricSensorQueryVO queryVO,
      List<String> onlineTfaSerialNumberList) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select monitored_serial_number ");
    sql.append(" from ").append(CenterConstants.TABLE_METRIC_DISK_IO_DATA_RECORD);
    sql.append(" where monitored_serial_number in (:onlineTfaSerialNumberList) ");
    sql.append(" and partition_name = :partitionName");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("onlineTfaSerialNumberList", onlineTfaSerialNumberList);
    params.put("partitionName", queryVO.getPartitionName());
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), sql, params);

    sql.append(" group by monitored_serial_number ");
    sql.append(" order by ").append(queryVO.getMetric()).append(" desc ");
    sql.append(" limit ").append(queryVO.getTopNumber());

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDiskIODao#queryDiskIOHistogram(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryDiskIOHistogram(MetricSensorQueryVO queryVO,
      List<String> topSerialNumberList) {
    StringBuilder sql = new StringBuilder();
    sql.append(
        "select toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') AS metric_time, ");
    sql.append(" monitored_serial_number, ");
    sql.append(queryVO.getMetric());
    sql.append(" from ").append(CenterConstants.TABLE_METRIC_DISK_IO_DATA_RECORD);
    sql.append(" where monitored_serial_number in (:topSerialNumberList) ");
    sql.append(" and partition_name = :partitionName");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());
    params.put("topSerialNumberList", topSerialNumberList);
    params.put("partitionName", queryVO.getPartitionName());
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), sql, params);

    sql.append(" group by metric_time, monitored_serial_number ");
    sql.append(" order by metric_time asc ");

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    // 日志开始时间在查询时间范围中
    whereSql.append(String.format(" and timestamp %s toDateTime64(:start_time, 3, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and timestamp %s toDateTime64(:end_time, 3, 'UTC') ",
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
      result = jdbcTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
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