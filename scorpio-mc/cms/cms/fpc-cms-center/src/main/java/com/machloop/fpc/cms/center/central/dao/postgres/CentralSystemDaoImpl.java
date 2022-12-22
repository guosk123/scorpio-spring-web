package com.machloop.fpc.cms.center.central.dao.postgres;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.central.bo.FpcStorageSpaceUsage;
import com.machloop.fpc.cms.center.central.dao.CentralSystemDao;
import com.machloop.fpc.cms.center.central.data.CentralSystemDO;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@Repository
public class CentralSystemDaoImpl implements CentralSystemDao {

  private static final String TABLE_SYSTEM_METRIC = "fpccms_central_system_metric";
  private static final String TABLE_SYSTEM_METRIC_HISTORY = "fpccms_central_system_metric_history";
  private static final String TABLE_SYSTEM_METRIC_HISTORY_5MIN = "fpccms_central_system_metric_history_5min";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  private LicenseService licenseService;

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#queryCentralSystems(java.lang.String, java.lang.String, int, java.util.Date, java.util.Date)
   */
  @Override
  public List<CentralSystemDO> queryCentralSystems(String deviceType, String monitoredSerialNumber,
      int interval, Date startTime, Date endTime) {
    StringBuilder sql = buildSelectStatement(interval);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumber", monitoredSerialNumber);

    if (startTime != null) {
      sql.append(" and metric_time >= :startTime ");
      params.put("startTime", startTime);
    }
    if (endTime != null) {
      sql.append(" and metric_time < :endTime ");
      params.put("endTime", endTime);
    }

    sql.append(" order by metric_time ");

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralSystemDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#querySensorSpace(java.util.List)
   */
  @Override
  public List<CentralSystemDO> querySensorSpace(List<String> monitoredSerialNumbers) {
    StringBuilder sql = new StringBuilder();
    sql.append("select device_type, monitored_serial_number, fs_store_total_byte, ");
    sql.append(" fs_system_total_byte, fs_index_total_byte, fs_metadata_total_byte, ");
    sql.append(" fs_metadata_hot_total_byte, fs_packet_total_byte, metric_time ");
    sql.append(" from ").append(TABLE_SYSTEM_METRIC);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number in (:monitoredSerialNumber) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", FpcCmsConstants.DEVICE_TYPE_TFA);
    params.put("monitoredSerialNumber", monitoredSerialNumbers);

    List<CentralSystemDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralSystemDO.class));
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#queryCentralSystemsMetricTime(java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<Date> queryCentralSystemsMetricTime(String deviceType, String monitoredSerialNumber,
      List<Date> metricTimeList) {
    if (CollectionUtils.isEmpty(metricTimeList)) {
      return Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    }

    StringBuilder sql = new StringBuilder();
    sql.append(" select distinct metric_time from ").append(TABLE_SYSTEM_METRIC_HISTORY);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");
    sql.append(" and metric_time in ( :metricTimeList ) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumber", monitoredSerialNumber);
    params.put("metricTimeList", metricTimeList);

    List<Date> list = jdbcTemplate.queryForList(sql.toString(), params, Date.class);

    return CollectionUtils.isEmpty(list)
        ? Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE)
        : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#queryCentralSystem(java.lang.String, java.lang.String)
   */
  @Override
  public CentralSystemDO queryCentralSystem(String deviceType, String monitoredSerialNumber) {
    StringBuilder sql = new StringBuilder();
    sql.append(
        "select id, device_type, monitored_serial_number, cpu_metric, memory_metric, system_fs_metric, ");
    sql.append(" index_fs_metric, metadata_fs_metric, packet_fs_metric, fs_data_total_byte, ");
    sql.append(" fs_data_used_pct, fs_cache_total_byte, fs_cache_used_pct, data_oldest_time, ");
    sql.append(" data_last24_total_byte, data_predict_total_day, cache_file_avg_byte, ");
    sql.append(" fs_store_total_byte, fs_system_total_byte, fs_index_total_byte, ");
    sql.append(" fs_metadata_total_byte, fs_packet_total_byte, metric_time ");
    sql.append(" from ").append(TABLE_SYSTEM_METRIC);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumber", monitoredSerialNumber);

    List<CentralSystemDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralSystemDO.class));
    return CollectionUtils.isEmpty(list) ? new CentralSystemDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#queryMaxDataOldestTime(java.lang.String, java.util.List)
   */
  @Override
  public CentralSystemDO queryMaxDataOldestTime(String deviceType,
      List<String> monitoredSerialNumberList) {
    StringBuilder sql = new StringBuilder();
    sql.append(
        "select monitored_serial_number, max(data_oldest_time) as data_oldest_time, metric_time ");
    sql.append(" from ").append(TABLE_SYSTEM_METRIC);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number in (:monitoredSerialNumberList)");
    sql.append(" group by monitored_serial_number, metric_time ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumberList", monitoredSerialNumberList);

    List<CentralSystemDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralSystemDO.class));
    return CollectionUtils.isEmpty(list) ? new CentralSystemDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#queryStorageSpaceUsagesByRanking()
   */
  @Override
  public List<FpcStorageSpaceUsage> queryStorageSpaceUsagesByRanking() {
    StringBuilder sql = new StringBuilder();
    sql.append(" select monitored_serial_number as device_serial_number, ");
    sql.append(" fs_data_used_pct, fs_data_total_byte ");
    sql.append(" from ").append(TABLE_SYSTEM_METRIC);
    sql.append(" where monitored_serial_number != :monitoredSerialNumber ");
    sql.append(" order by fs_data_used_pct desc ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("monitoredSerialNumber", licenseService.queryDeviceSerialNumber());

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcStorageSpaceUsage.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#saveCentralSystems(java.util.List)
   */
  @Override
  public void saveCentralSystems(List<CentralSystemDO> centralSystemDOList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_SYSTEM_METRIC_HISTORY);
    sql.append(" (id, device_type, monitored_serial_number, cpu_metric, memory_metric, ");
    sql.append(" system_fs_metric, metric_time) ");
    sql.append(" values (:id, :deviceType, :monitoredSerialNumber, :cpuMetric, :memoryMetric, ");
    sql.append(" :systemFsMetric, :metricTime) ");

    for (CentralSystemDO centralSystemDO : centralSystemDOList) {
      centralSystemDO.setId(IdGenerator.generateUUID());
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(centralSystemDOList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /** 
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#saveCentralSystem(com.machloop.fpc.cms.center.central.data.CentralSystemDO)
   */
  @Override
  public CentralSystemDO saveCentralSystem(CentralSystemDO centralSystemDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_SYSTEM_METRIC_HISTORY);
    sql.append(" (id, device_type, monitored_serial_number, cpu_metric, memory_metric, ");
    sql.append(" system_fs_metric, metric_time) ");
    sql.append(" values (:id, :deviceType, :monitoredSerialNumber, :cpuMetric, :memoryMetric, ");
    sql.append(" :systemFsMetric, :metricTime) ");

    centralSystemDO.setId(IdGenerator.generateUUID());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(centralSystemDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return centralSystemDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#saveOrUpdateCentralSystem(com.machloop.fpc.cms.center.central.data.CentralSystemDO)
   */
  @Override
  public int saveOrUpdateCentralSystem(CentralSystemDO centralSystemDO) {
    int update = update(centralSystemDO);
    return update > 0 ? update : save(centralSystemDO);
  }

  private int save(CentralSystemDO centralSystemDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_SYSTEM_METRIC);
    sql.append(" (id, device_type, monitored_serial_number, cpu_metric, memory_metric, ");
    sql.append(" system_fs_metric, index_fs_metric, metadata_fs_metric, metadata_hot_fs_metric, ");
    sql.append(" packet_fs_metric, fs_data_total_byte, fs_data_used_pct, fs_cache_total_byte, ");
    sql.append(" fs_cache_used_pct, data_oldest_time, data_last24_total_byte, ");
    sql.append(" data_predict_total_day, cache_file_avg_byte, fs_store_total_byte, ");
    sql.append(" fs_system_total_byte, fs_index_total_byte, fs_metadata_total_byte, ");
    sql.append(" fs_metadata_hot_total_byte, fs_packet_total_byte, metric_time) ");

    sql.append(" values (:id, :deviceType, :monitoredSerialNumber, :cpuMetric, :memoryMetric, ");
    sql.append(" :systemFsMetric, :indexFsMetric, :metadataFsMetric, :metadataHotFsMetric, ");
    sql.append(" :packetFsMetric, :fsDataTotalByte, :fsDataUsedPct, :fsCacheTotalByte, ");
    sql.append(" :fsCacheUsedPct, :dataOldestTime, :dataLast24TotalByte, ");
    sql.append(" :dataPredictTotalDay, :cacheFileAvgByte, :fsStoreTotalByte, ");
    sql.append(" :fsSystemTotalByte, :fsIndexTotalByte, :fsMetadataTotalByte, ");
    sql.append(" :fsMetadataHotTotalByte, :fsPacketTotalByte, :metricTime) ");

    centralSystemDO.setId(IdGenerator.generateUUID());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(centralSystemDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private int update(CentralSystemDO centralSystemDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_SYSTEM_METRIC);
    sql.append(" set cpu_metric = :cpuMetric, memory_metric = :memoryMetric, ");
    sql.append(" system_fs_metric = :systemFsMetric, index_fs_metric = :indexFsMetric, ");
    sql.append(" metadata_fs_metric = :metadataFsMetric, ");
    sql.append(" metadata_hot_fs_metric = :metadataHotFsMetric, ");
    sql.append(" packet_fs_metric = :packetFsMetric, fs_data_total_byte = :fsDataTotalByte, ");
    sql.append(" fs_data_used_pct = :fsDataUsedPct, fs_cache_total_byte = :fsCacheTotalByte, ");
    sql.append(" fs_cache_used_pct = :fsCacheUsedPct, data_oldest_time = :dataOldestTime, ");
    sql.append(" data_last24_total_byte = :dataLast24TotalByte, ");
    sql.append(" data_predict_total_day = :dataPredictTotalDay, ");
    sql.append(" cache_file_avg_byte = :cacheFileAvgByte, ");
    sql.append(" fs_store_total_byte = :fsStoreTotalByte, ");
    sql.append(" fs_system_total_byte = :fsSystemTotalByte, ");
    sql.append(" fs_index_total_byte = :fsIndexTotalByte, ");
    sql.append(" fs_metadata_total_byte = :fsMetadataTotalByte, ");
    sql.append(" fs_metadata_hot_total_byte = :fsMetadataHotTotalByte, ");
    sql.append(" fs_packet_total_byte = :fsPacketTotalByte, ");
    sql.append(" metric_time = :metricTime ");
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(centralSystemDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#rollupCentralSystem(java.util.Date, java.util.Date, java.lang.String, java.lang.String)
   */
  @Override
  public int rollupCentralSystem(Date startTime, Date endTime, String deviceType,
      String monitoredSerialNumber) {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // collect data
    StringBuilder sql = new StringBuilder();
    sql.append(" select device_type, monitored_serial_number, ");
    sql.append(" MAX(cpu_metric) as cpu_metric, ");
    sql.append(" MAX(memory_metric) as memory_metric, ");
    sql.append(" MAX(system_fs_metric) as system_fs_metric ");
    sql.append(" from ").append(TABLE_SYSTEM_METRIC_HISTORY);
    sql.append(" where metric_time >= :startTime ");
    sql.append(" and metric_time < :endTime ");
    params.put("startTime", startTime);
    params.put("endTime", endTime);

    if (StringUtils.isNotBlank(deviceType)) {
      sql.append(" and device_type = :deviceType ");
      params.put("deviceType", deviceType);
    }
    if (StringUtils.isNotBlank(monitoredSerialNumber)) {
      sql.append(" and monitored_serial_number = :monitoredSerialNumber ");
      params.put("monitoredSerialNumber", monitoredSerialNumber);
    }

    sql.append(" group by device_type, monitored_serial_number ");

    List<CentralSystemDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralSystemDO.class));
    for (CentralSystemDO centralSystemDO : list) {
      centralSystemDO.setId(IdGenerator.generateUUID());
      centralSystemDO.setMetricTime(startTime);
    }

    // batch insert fpc_center_device_monitor_5min
    sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_SYSTEM_METRIC_HISTORY_5MIN);
    sql.append(" (id, device_type, monitored_serial_number, cpu_metric, memory_metric, ");
    sql.append(" system_fs_metric, metric_time) ");
    sql.append(" values (:id, :deviceType, :monitoredSerialNumber, :cpuMetric, :memoryMetric, ");
    sql.append(" :systemFsMetric, :metricTime) ");

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(list);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
    return list.size();
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#deleteCentralSystem(java.util.Date, int)
   */
  @Override
  public int deleteCentralSystem(Date beforeTime, int interval) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    String tableName = TABLE_SYSTEM_METRIC_HISTORY;
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName = TABLE_SYSTEM_METRIC_HISTORY_5MIN;
    }
    sql.append(tableName);
    sql.append(" where metric_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("beforeTime", beforeTime);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#deleteCentralSystem(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCentralSystem(String deviceType, String monitoredSerialNumber) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_SYSTEM_METRIC);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumber", monitoredSerialNumber);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralSystemDao#deleteCentralSystem(java.lang.String, java.lang.String, java.util.Date, int)
   */
  @Override
  public int deleteCentralSystem(String deviceType, String monitoredSerialNumber, Date metricTime,
      int interval) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    String tableName = TABLE_SYSTEM_METRIC_HISTORY;
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName = TABLE_SYSTEM_METRIC_HISTORY_5MIN;
    }
    sql.append(tableName);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");
    sql.append(" and metric_time = :metricTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumber", monitoredSerialNumber);
    params.put("metricTime", metricTime);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement(int interval) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, device_type, monitored_serial_number, cpu_metric, memory_metric, ");
    sql.append(" system_fs_metric, metric_time from ");
    String tableName = TABLE_SYSTEM_METRIC_HISTORY;
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName = TABLE_SYSTEM_METRIC_HISTORY_5MIN;
    }
    sql.append(tableName);
    return sql;
  }

}
