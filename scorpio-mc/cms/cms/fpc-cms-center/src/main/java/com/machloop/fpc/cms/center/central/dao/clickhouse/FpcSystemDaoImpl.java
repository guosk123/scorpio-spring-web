package com.machloop.fpc.cms.center.central.dao.clickhouse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.central.dao.FpcSystemDao;
import com.machloop.fpc.cms.center.central.data.CentralSystemDO;
import com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO;

/**
 * @author guosk
 *
 * create at 2022年5月5日, fpc-cms-center
 */
@Repository
public class FpcSystemDaoImpl implements FpcSystemDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(FpcSystemDaoImpl.class);

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcSystemDao#queryLatestFpcSystemMetrics(java.lang.String)
   */
  @Override
  public CentralSystemDO queryLatestFpcSystemMetrics(String serialNumber) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select monitored_serial_number, timestamp, memory_used_ratio, cpu_used_ratio, ");
    sql.append(" system_fs_used_ratio, index_fs_used_ratio, metadata_fs_used_ratio, ");
    sql.append(" metadata_hot_fs_used_ratio, packet_fs_used_ratio ");
    sql.append(" from ").append(CenterConstants.TABLE_METRIC_MONITOR_DATA_RECORD);
    sql.append(" where monitored_serial_number = :serialNumber ");
    sql.append(" order by timestamp desc limit 1 ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("serialNumber", serialNumber);

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    if (CollectionUtils.isNotEmpty(result)) {
      Map<String, Object> item = result.get(0);
      CentralSystemDO fpcSystemDO = new CentralSystemDO();
      OffsetDateTime timestamp = (OffsetDateTime) item.get("timestamp");
      fpcSystemDO.setMetricTime(Date.from(timestamp.toInstant()));
      fpcSystemDO.setMonitoredSerialNumber(MapUtils.getString(item, "monitored_serial_number"));
      fpcSystemDO.setMemoryMetric(MapUtils.getIntValue(item, "memory_used_ratio", 0));
      fpcSystemDO.setCpuMetric(MapUtils.getIntValue(item, "cpu_used_ratio", 0));
      fpcSystemDO.setSystemFsMetric(MapUtils.getIntValue(item, "system_fs_used_ratio", 0));
      fpcSystemDO.setIndexFsMetric(MapUtils.getIntValue(item, "index_fs_used_ratio", 0));
      fpcSystemDO.setMetadataFsMetric(MapUtils.getIntValue(item, "metadata_fs_used_ratio", 0));
      fpcSystemDO
          .setMetadataHotFsMetric(MapUtils.getIntValue(item, "metadata_hot_fs_used_ratio", 0));
      fpcSystemDO.setPacketFsMetric(MapUtils.getIntValue(item, "packet_fs_used_ratio", 0));

      return fpcSystemDO;
    } else {
      return new CentralSystemDO();
    }
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcSystemDao#queryFpcSystemHistogramByMetric(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryFpcSystemHistogramByMetric(MetricSensorQueryVO queryVO,
      List<String> topSerialNumberList) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select monitored_serial_number, ");
    sql.append(
        " toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') as metric_time, ");
    if (StringUtils.equals(queryVO.getMetric(), "cpu_metric")) {
      sql.append(" max(cpu_used_ratio) as cpu_metric ");
    }
    if (StringUtils.equals(queryVO.getMetric(), "memory_metric")) {
      sql.append(" max(memory_used_ratio) as memory_metric ");
    }
    sql.append(" from ").append(CenterConstants.TABLE_METRIC_MONITOR_DATA_RECORD);
    sql.append(" where monitored_serial_number in (:topSerialNumberList) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());
    params.put("topSerialNumberList", topSerialNumberList);

    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), sql, params);

    sql.append(" group by monitored_serial_number, metric_time ");
    sql.append(" order by metric_time asc ");

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcSystemDao#queryFpcSystemByMetric(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryFpcSystemByMetric(MetricSensorQueryVO queryVO,
      List<String> tfaSerialNumberList) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select monitored_serial_number ");
    sql.append(" from ").append(CenterConstants.TABLE_METRIC_MONITOR_DATA_RECORD);
    sql.append(" where monitored_serial_number in (:tfaSerialNumberList) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("tfaSerialNumberList", tfaSerialNumberList);
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), sql, params);

    sql.append(" group by monitored_serial_number ");
    if (StringUtils.equals(queryVO.getMetric(), "cpu_metric")) {
      sql.append(" order by max(cpu_used_ratio) desc ");
    }
    if (StringUtils.equals(queryVO.getMetric(), "memory_metric")) {
      sql.append(" order by max(memory_used_ratio) desc ");
    }
    sql.append("limit ").append(queryVO.getTopNumber());

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcSystemDao#queryFpcFreeSpaceMetric(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryFpcFreeSpaceMetric(MetricSensorQueryVO queryVO,
      List<String> tfaSerialNumberList) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select monitored_serial_number, ");
    sql.append(" min(").append(queryVO.getMetric()).append(") as ").append(queryVO.getMetric());
    sql.append(" from ").append(CenterConstants.TABLE_METRIC_MONITOR_DATA_RECORD);
    sql.append(" where monitored_serial_number in (:tfaSerialNumberList) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("tfaSerialNumberList", tfaSerialNumberList);
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), sql, params);

    sql.append(" group by monitored_serial_number ");
    sql.append(" order by ").append(queryVO.getMetric()).append(" desc ");
    sql.append(" limit ").append(queryVO.getTopNumber());

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
