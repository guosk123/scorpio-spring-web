package com.machloop.fpc.manager.system.dao.clickhouse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
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
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.manager.global.dao.DataRecordDaoCk;
import com.machloop.fpc.manager.system.dao.MetricDiskIODao;
import com.machloop.fpc.manager.system.data.MetricDiskIODO;

/**
 * @author guosk
 *
 * create at 2021年10月26日, fpc-manager
 */
@Repository
public class MetricDiskIODaoImpl implements MetricDiskIODao, DataRecordDaoCk {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricDiskIODaoImpl.class);

  @Autowired
  private ClickHouseStatsJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.system.dao.MetricDiskIODao#queryMetricDiskIOHistograms(java.util.Date, java.util.Date, int, java.lang.String)
   */
  @Override
  public List<MetricDiskIODO> queryMetricDiskIOHistograms(Date startTime, Date endTime,
      int interval, String partitionName) {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", interval);

    StringBuilder sql = new StringBuilder();
    sql.append(
        "select toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') AS temp_timestamp, ");
    sql.append(" partition_name, ");
    sql.append(" avg(read_byteps) as read_byteps, ");
    sql.append(" max(read_byteps_peak) as read_byteps_peak, ");
    sql.append(" avg(write_byteps) as write_byteps, ");
    sql.append(" max(write_byteps_peak) as write_byteps_peak ");
    sql.append(" from ").append(ManagerConstants.TABLE_METRIC_DISK_IO_DATA_RECORD);

    // 过滤
    sql.append(" where 1=1 ");
    if (startTime != null) {
      sql.append(" and timestamp > toDateTime64(:startTime, 3, 'UTC') ");
      params.put("startTime",
          DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (endTime != null) {
      sql.append(" and timestamp <= toDateTime64(:endTime, 3, 'UTC') ");
      params.put("endTime",
          DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (StringUtils.isNotBlank(partitionName)) {
      sql.append(" and partition_name = :partitionName ");
      params.put("partitionName", partitionName);
    }

    // 聚合
    sql.append(" group by temp_timestamp, partition_name ");
    sql.append(" order by temp_timestamp ");

    List<Map<String, Object>> results = jdbcTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());

    return results.stream().map(item -> {
      MetricDiskIODO metricDiskIOData = new MetricDiskIODO();

      OffsetDateTime timestamp = (OffsetDateTime) item.get("temp_timestamp");
      metricDiskIOData.setTimestamp(Date.from(timestamp.toInstant()));
      metricDiskIOData.setPartitionName(MapUtils.getString(item, "partition_name"));
      metricDiskIOData.setReadByteps(MapUtils.getLongValue(item, "read_byteps"));
      metricDiskIOData.setReadBytepsPeak(MapUtils.getLongValue(item, "read_byteps_peak"));
      metricDiskIOData.setWriteByteps(MapUtils.getLongValue(item, "write_byteps"));
      metricDiskIOData.setWriteBytepsPeak(MapUtils.getLongValue(item, "write_byteps_peak"));

      return metricDiskIOData;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.MetricDiskIODao#queryMetricDiskIOs(java.util.Date)
   */
  @Override
  public List<MetricDiskIODO> queryMetricDiskIOs(Date afterTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp, partition_name, ");
    sql.append(" read_byteps, read_byteps_peak, write_byteps, write_byteps_peak ");
    sql.append(" from ").append(ManagerConstants.TABLE_METRIC_DISK_IO_DATA_RECORD);
    sql.append(" where timestamp > toDateTime64(:afterTime, 3, 'UTC') ");
    sql.append(" order by timestamp desc ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("afterTime",
        DateUtils.toStringFormat(afterTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    List<Map<String, Object>> results = jdbcTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());

    return results.stream().map(item -> {
      MetricDiskIODO metricDiskIOData = new MetricDiskIODO();

      OffsetDateTime timestamp = (OffsetDateTime) item.get("timestamp");
      metricDiskIOData.setTimestamp(Date.from(timestamp.toInstant()));
      metricDiskIOData.setPartitionName(MapUtils.getString(item, "partition_name"));
      metricDiskIOData.setReadByteps(MapUtils.getLongValue(item, "read_byteps"));
      metricDiskIOData.setReadBytepsPeak(MapUtils.getLongValue(item, "read_byteps_peak"));
      metricDiskIOData.setWriteByteps(MapUtils.getLongValue(item, "write_byteps"));
      metricDiskIOData.setWriteBytepsPeak(MapUtils.getLongValue(item, "write_byteps_peak"));

      return metricDiskIOData;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.DataRecordDaoCk#rollup(java.util.Date, java.util.Date)
   */
  @Override
  public int rollup(Date startTime, Date endTime) throws IOException {
    int success = 0;

    String inputTableName;
    String outputTableName;

    long intervalSeconds = (endTime.getTime() - startTime.getTime()) / 1000;
    if (intervalSeconds == Constants.FIVE_MINUTE_SECONDS) {
      // 60秒汇总到5分钟
      inputTableName = ManagerConstants.TABLE_METRIC_DISK_IO_DATA_RECORD;
      outputTableName = ManagerConstants.TABLE_METRIC_DISK_IO_DATA_RECORD + "_5m";
    } else if (intervalSeconds == Constants.ONE_HOUR_SECONDS) {
      // 5分钟汇总到1小时
      inputTableName = ManagerConstants.TABLE_METRIC_DISK_IO_DATA_RECORD + "_5m";
      outputTableName = ManagerConstants.TABLE_METRIC_DISK_IO_DATA_RECORD + "_1h";
    } else {
      return success;
    }

    // 聚合前先查询是否存在当前聚合时间的脏数据，有则清除, 防止重复统计
    StringBuilder countSql = new StringBuilder();
    countSql.append("select count(1) from ");
    countSql.append(outputTableName);
    countSql.append(" where timestamp > toDateTime64(:timestamp, 3, 'UTC') ");

    Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
    paramMap.put("timestamp",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    long dirtyCount = jdbcTemplate.getJdbcTemplate().queryForObject(countSql.toString(), paramMap,
        Long.class);
    if (dirtyCount > 0) {
      StringBuilder deleteSql = new StringBuilder();
      deleteSql.append(" alter table ").append(outputTableName);
      deleteSql.append(" delete where timestamp > toDateTime64(:timestamp, 3, 'UTC') ");

      int clean = jdbcTemplate.getJdbcTemplate().update(deleteSql.toString(), paramMap);
      LOGGER.info("found dirty rollup record in [{}], start to clean, total: [{}], clean: [{}]",
          outputTableName, dirtyCount, clean);
    }

    // 聚合统计inputTableName
    success += aggregate(startTime, endTime, inputTableName, outputTableName);

    LOGGER.info(
        "finish to roll up, input tableName: [{}], out tableName: [{}], startTime: [{}], endTime: [{}], "
            + "total roll up sucess count: [{}]",
        inputTableName, outputTableName, startTime, endTime, success);

    return success;
  }

  private int aggregate(Date startTime, Date endTime, String inputTableName, String outputTableName)
      throws IOException {
    int totalSize = 0;

    StringBuilder sql = new StringBuilder();
    sql.append(" select monitored_serial_number, partition_name, ");
    sql.append(" avg(read_byteps) as read_byteps, ");
    sql.append(" max(read_byteps_peak) as read_byteps_peak, ");
    sql.append(" avg(write_byteps) as write_byteps, ");
    sql.append(" max(write_byteps_peak) as write_byteps_peak ");
    sql.append(" from ").append(inputTableName);
    sql.append(" where timestamp > toDateTime64(:startTime, 3, 'UTC') ");
    sql.append(" and timestamp <= toDateTime64(:endTime, 3, 'UTC') ");
    sql.append(" group by monitored_serial_number, partition_name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("startTime",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("endTime", DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    List<Map<String, Object>> results = jdbcTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
    if (CollectionUtils.isEmpty(results)) {
      return totalSize;
    }

    results.forEach(item -> {
      item.put("timestamp",
          DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    });

    StringBuilder saveSql = new StringBuilder();
    saveSql.append("insert into ");
    saveSql.append(outputTableName);
    saveSql.append("(");
    saveSql.append("timestamp,");
    saveSql.append("monitored_serial_number,");
    saveSql.append("partition_name,");
    saveSql.append("read_byteps,");
    saveSql.append("read_byteps_peak,");
    saveSql.append("write_byteps,");
    saveSql.append("write_byteps_peak)");
    saveSql.append(" values (");
    saveSql.append(":timestamp,");
    saveSql.append(":monitored_serial_number,");
    saveSql.append(":partition_name,");
    saveSql.append(":read_byteps,");
    saveSql.append(":read_byteps_peak,");
    saveSql.append(":write_byteps,");
    saveSql.append(":write_byteps_peak)");

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(results);
    totalSize = Arrays
        .stream(jdbcTemplate.getJdbcTemplate().batchUpdate(saveSql.toString(), batchSource)).sum();

    return totalSize;
  }

}
