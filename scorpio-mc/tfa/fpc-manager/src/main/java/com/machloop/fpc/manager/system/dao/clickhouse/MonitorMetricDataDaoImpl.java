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
import com.machloop.fpc.manager.system.dao.MonitorMetricDataDao;
import com.machloop.fpc.manager.system.data.MonitorMetricDataDO;

/**
 * @author guosk
 *
 * create at 2021年9月2日, fpc-manager
 */
@Repository
public class MonitorMetricDataDaoImpl implements MonitorMetricDataDao, DataRecordDaoCk {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorMetricDataDaoImpl.class);

  @Autowired
  private ClickHouseStatsJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.system.dao.MonitorMetricDataDao#queryMonitorMetricData(java.util.Date, java.util.Date, int)
   */
  @Override
  public List<MonitorMetricDataDO> queryMonitorMetricData(Date startTime, Date endTime,
      int interval) {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", interval);

    StringBuilder sql = new StringBuilder();
    sql.append(
        "select toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') AS temp_timestamp, ");
    sql.append(" max(memory_used_ratio) as memory_used_ratio, ");
    sql.append(" max(cpu_used_ratio) as cpu_used_ratio, ");
    sql.append(" max(system_fs_used_ratio) as system_fs_used_ratio, ");
    sql.append(" min(system_fs_free) as system_fs_free, ");
    sql.append(" max(index_fs_used_ratio) as index_fs_used_ratio, ");
    sql.append(" min(index_fs_free) as index_fs_free, ");
    sql.append(" max(metadata_fs_used_ratio) as metadata_fs_used_ratio, ");
    sql.append(" min(metadata_fs_free) as metadata_fs_free, ");
    sql.append(" max(metadata_hot_fs_used_ratio) as metadata_hot_fs_used_ratio, ");
    sql.append(" min(metadata_hot_fs_free) as metadata_hot_fs_free, ");
    sql.append(" max(packet_fs_used_ratio) as packet_fs_used_ratio, ");
    sql.append(" min(packet_fs_free) as packet_fs_free ");
    sql.append(" from ").append(ManagerConstants.TABLE_METRIC_MONITOR_DATA_RECORD);

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

    // 聚合
    sql.append(" group by temp_timestamp ");
    sql.append(" order by temp_timestamp ");

    List<Map<String, Object>> results = jdbcTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());

    return results.stream().map(item -> {
      MonitorMetricDataDO monitorMetricData = new MonitorMetricDataDO();
      OffsetDateTime timestamp = (OffsetDateTime) item.get("temp_timestamp");
      monitorMetricData.setTimestamp(Date.from(timestamp.toInstant()));
      monitorMetricData.setCpuUsedRatio(MapUtils.getIntValue(item, "cpu_used_ratio"));
      monitorMetricData.setMemoryUsedRatio(MapUtils.getIntValue(item, "memory_used_ratio"));
      monitorMetricData.setSystemFsUsedRatio(MapUtils.getIntValue(item, "system_fs_used_ratio"));
      monitorMetricData.setSystemFsFree(MapUtils.getLongValue(item, "system_fs_free"));
      monitorMetricData.setIndexFsUsedRatio(MapUtils.getIntValue(item, "index_fs_used_ratio"));
      monitorMetricData.setIndexFsFree(MapUtils.getLongValue(item, "index_fs_free"));
      monitorMetricData
          .setMetadataFsUsedRatio(MapUtils.getIntValue(item, "metadata_fs_used_ratio"));
      monitorMetricData.setMetadataFsFree(MapUtils.getLongValue(item, "metadata_fs_free"));
      monitorMetricData
          .setMetadataHotFsUsedRatio(MapUtils.getIntValue(item, "metadata_hot_fs_used_ratio"));
      monitorMetricData.setMetadataHotFsFree(MapUtils.getLongValue(item, "metadata_hot_fs_free"));
      monitorMetricData.setPacketFsUsedRatio(MapUtils.getIntValue(item, "packet_fs_used_ratio"));
      monitorMetricData.setPacketFsFree(MapUtils.getLongValue(item, "packet_fs_free"));

      return monitorMetricData;
    }).collect(Collectors.toList());
  }

  @Override
  public List<MonitorMetricDataDO> queryLatestMonitorMetricData() {

    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp AS temp_timestamp, memory_used_ratio, cpu_used_ratio, ");
    sql.append("system_fs_used_ratio, system_fs_free, index_fs_used_ratio, ");
    sql.append("index_fs_free, metadata_fs_used_ratio, metadata_fs_free, ");
    sql.append(
        "metadata_hot_fs_used_ratio, metadata_hot_fs_free, packet_fs_used_ratio, packet_fs_free ");
    sql.append("from ").append(ManagerConstants.TABLE_METRIC_MONITOR_DATA_RECORD);

    // 聚合
    sql.append(" order by temp_timestamp desc");

    sql.append(" limit 1");

    List<Map<String, Object>> results = jdbcTemplate.getJdbcTemplate().query(sql.toString(),
        new ColumnMapRowMapper());

    return results.stream().map(item -> {
      MonitorMetricDataDO monitorMetricData = new MonitorMetricDataDO();
      OffsetDateTime timestamp = (OffsetDateTime) item.get("temp_timestamp");
      monitorMetricData.setTimestamp(Date.from(timestamp.toInstant()));
      monitorMetricData.setCpuUsedRatio(MapUtils.getIntValue(item, "cpu_used_ratio"));
      monitorMetricData.setMemoryUsedRatio(MapUtils.getIntValue(item, "memory_used_ratio"));
      monitorMetricData.setSystemFsUsedRatio(MapUtils.getIntValue(item, "system_fs_used_ratio"));
      monitorMetricData.setSystemFsFree(MapUtils.getLongValue(item, "system_fs_free"));
      monitorMetricData.setIndexFsUsedRatio(MapUtils.getIntValue(item, "index_fs_used_ratio"));
      monitorMetricData.setIndexFsFree(MapUtils.getLongValue(item, "index_fs_free"));
      monitorMetricData
          .setMetadataFsUsedRatio(MapUtils.getIntValue(item, "metadata_fs_used_ratio"));
      monitorMetricData.setMetadataFsFree(MapUtils.getLongValue(item, "metadata_fs_free"));
      monitorMetricData
          .setMetadataHotFsUsedRatio(MapUtils.getIntValue(item, "metadata_hot_fs_used_ratio"));
      monitorMetricData.setMetadataHotFsFree(MapUtils.getLongValue(item, "metadata_hot_fs_free"));
      monitorMetricData.setPacketFsUsedRatio(MapUtils.getIntValue(item, "packet_fs_used_ratio"));
      monitorMetricData.setPacketFsFree(MapUtils.getLongValue(item, "packet_fs_free"));

      return monitorMetricData;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.MonitorMetricDataDao#queryMonitorMetricData(java.util.Date)
   */
  @Override
  public List<MonitorMetricDataDO> queryMonitorMetricData(Date afterTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp, memory_used_ratio, cpu_used_ratio, system_fs_used_ratio, ");
    sql.append(" index_fs_used_ratio, metadata_fs_used_ratio, ");
    sql.append(" metadata_hot_fs_used_ratio, packet_fs_used_ratio ");
    sql.append(" from ").append(ManagerConstants.TABLE_METRIC_MONITOR_DATA_RECORD);
    sql.append(" where timestamp > toDateTime64(:afterTime, 3, 'UTC') ");
    sql.append(" order by timestamp desc ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("afterTime",
        DateUtils.toStringFormat(afterTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    List<Map<String, Object>> results = jdbcTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());

    return results.stream().map(item -> {
      MonitorMetricDataDO monitorMetricData = new MonitorMetricDataDO();
      OffsetDateTime timestamp = (OffsetDateTime) item.get("timestamp");
      monitorMetricData.setTimestamp(Date.from(timestamp.toInstant()));
      monitorMetricData.setCpuUsedRatio(MapUtils.getIntValue(item, "cpu_used_ratio"));
      monitorMetricData.setMemoryUsedRatio(MapUtils.getIntValue(item, "memory_used_ratio"));
      monitorMetricData.setSystemFsUsedRatio(MapUtils.getIntValue(item, "system_fs_used_ratio"));
      monitorMetricData.setIndexFsUsedRatio(MapUtils.getIntValue(item, "index_fs_used_ratio"));
      monitorMetricData
          .setMetadataFsUsedRatio(MapUtils.getIntValue(item, "metadata_fs_used_ratio"));
      monitorMetricData
          .setMetadataHotFsUsedRatio(MapUtils.getIntValue(item, "metadata_hot_fs_used_ratio"));
      monitorMetricData.setPacketFsUsedRatio(MapUtils.getIntValue(item, "packet_fs_used_ratio"));

      return monitorMetricData;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.MonitorMetricDataDao#saveMonitorMetricData(com.machloop.fpc.manager.system.data.MonitorMetricDataDO)
   */
  @Override
  public void saveMonitorMetricData(MonitorMetricDataDO monitorMetricDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(ManagerConstants.TABLE_METRIC_MONITOR_DATA_RECORD);
    sql.append("(timestamp, memory_used_ratio, cpu_used_ratio, system_fs_used_ratio, ");
    sql.append(" index_fs_used_ratio, metadata_fs_used_ratio) ");
    sql.append(" values(:timestamp, :memoryUsedRatio, :cpuUsedRatio, :systemFsUsedRatio, ");
    sql.append(" :indexFsUsedRatio, :metadataFsUsedRatio)");

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("timestamp", DateUtils.toStringFormat(monitorMetricDO.getTimestamp(),
        "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    result.put("memoryUsedRatio", monitorMetricDO.getMemoryUsedRatio());
    result.put("cpuUsedRatio", monitorMetricDO.getCpuUsedRatio());
    result.put("systemFsUsedRatio", monitorMetricDO.getSystemFsUsedRatio());
    result.put("indexFsUsedRatio", monitorMetricDO.getIndexFsUsedRatio());
    result.put("metadataFsUsedRatio", monitorMetricDO.getMetadataFsUsedRatio());

    jdbcTemplate.getJdbcTemplate().update(sql.toString(), result);
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
      inputTableName = ManagerConstants.TABLE_METRIC_MONITOR_DATA_RECORD;
      outputTableName = ManagerConstants.TABLE_METRIC_MONITOR_DATA_RECORD + "_5m";
    } else if (intervalSeconds == Constants.ONE_HOUR_SECONDS) {
      // 5分钟汇总到1小时
      inputTableName = ManagerConstants.TABLE_METRIC_MONITOR_DATA_RECORD + "_5m";
      outputTableName = ManagerConstants.TABLE_METRIC_MONITOR_DATA_RECORD + "_1h";
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
    sql.append("select monitored_serial_number, ");
    sql.append(" max(memory_used_ratio) as memory_used_ratio, ");
    sql.append(" max(cpu_used_ratio) as cpu_used_ratio, ");
    sql.append(" max(system_fs_used_ratio) as system_fs_used_ratio, ");
    sql.append(" min(system_fs_free) as system_fs_free, ");
    sql.append(" max(index_fs_used_ratio) as index_fs_used_ratio, ");
    sql.append(" min(index_fs_free) as index_fs_free, ");
    sql.append(" max(metadata_fs_used_ratio) as metadata_fs_used_ratio, ");
    sql.append(" min(metadata_fs_free) as metadata_fs_free, ");
    sql.append(" max(metadata_hot_fs_used_ratio) as metadata_hot_fs_used_ratio, ");
    sql.append(" min(metadata_hot_fs_free) as metadata_hot_fs_free, ");
    sql.append(" max(packet_fs_used_ratio) as packet_fs_used_ratio, ");
    sql.append(" min(packet_fs_free) as packet_fs_free ");
    sql.append(" from ").append(inputTableName);
    sql.append(" where timestamp > toDateTime64(:startTime, 3, 'UTC') ");
    sql.append(" and timestamp <= toDateTime64(:endTime, 3, 'UTC') ");
    sql.append(" group by monitored_serial_number ");

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
    saveSql.append("memory_used_ratio,");
    saveSql.append("cpu_used_ratio,");
    saveSql.append("system_fs_used_ratio,");
    saveSql.append("system_fs_free,");
    saveSql.append("index_fs_used_ratio,");
    saveSql.append("index_fs_free,");
    saveSql.append("metadata_fs_used_ratio,");
    saveSql.append("metadata_fs_free,");
    saveSql.append("metadata_hot_fs_used_ratio,");
    saveSql.append("metadata_hot_fs_free,");
    saveSql.append("packet_fs_used_ratio,");
    saveSql.append("packet_fs_free)");
    saveSql.append(" values (");
    saveSql.append(":timestamp,");
    saveSql.append(":monitored_serial_number,");
    saveSql.append(":memory_used_ratio,");
    saveSql.append(":cpu_used_ratio,");
    saveSql.append(":system_fs_used_ratio,");
    saveSql.append(":system_fs_free,");
    saveSql.append(":index_fs_used_ratio,");
    saveSql.append(":index_fs_free,");
    saveSql.append(":metadata_fs_used_ratio,");
    saveSql.append(":metadata_fs_free,");
    saveSql.append(":metadata_hot_fs_used_ratio,");
    saveSql.append(":metadata_hot_fs_free,");
    saveSql.append(":packet_fs_used_ratio,");
    saveSql.append(":packet_fs_free)");

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(results);
    totalSize = Arrays
        .stream(jdbcTemplate.getJdbcTemplate().batchUpdate(saveSql.toString(), batchSource)).sum();

    return totalSize;
  }

}
