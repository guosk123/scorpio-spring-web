package com.machloop.fpc.cms.center.central.dao.postgres;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.central.bo.CentralNetifUsage;
import com.machloop.fpc.cms.center.central.dao.CentralNetifDao;
import com.machloop.fpc.cms.center.central.data.CentralNetifDO;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@Repository
public class CentralNetifDaoImpl implements CentralNetifDao {

  private static final String TABLE_DEVICE_NETIF = "fpccms_central_device_netif";
  private static final String TABLE_DEVICE_NETIF_HISTORY = "fpccms_central_device_netif_history";
  private static final String TABLE_DEVICE_NETIF_HISTORY_5MIN = "fpccms_central_device_netif_history_5min";
  private static final String REAL_COLLECT_POINT_COUNT = "5";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  private LicenseService licenseService;

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#queryCentralNetifProfiles(java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<CentralNetifDO> queryCentralNetifProfiles(String deviceType,
      String monitoredSerialNumber, List<String> categoryList) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, device_type, monitored_serial_number, netif_name, ");
    sql.append(" state, category, specification ");
    sql.append(" from ").append(TABLE_DEVICE_NETIF);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumber", monitoredSerialNumber);

    if (CollectionUtils.isNotEmpty(categoryList)) {
      sql.append(" and category in ( :categoryList ) ");
      params.put("categoryList", categoryList);
    }

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralNetifDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#queryCentralNetifById(java.lang.String)
   */
  @Override
  public CentralNetifDO queryCentralNetifById(String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, device_type, monitored_serial_number, netif_name, ");
    sql.append(" state, category, specification ");
    sql.append(" from ").append(TABLE_DEVICE_NETIF);
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<CentralNetifDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralNetifDO.class));
    return CollectionUtils.isEmpty(list) ? new CentralNetifDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#queryCentralNetifs(java.lang.String, java.lang.String, java.lang.String, java.util.List, int, java.util.Date, java.util.Date)
   */
  @Override
  public List<CentralNetifDO> queryCentralNetifs(String deviceType, String monitoredSerialNumber,
      String netifName, List<String> categoryList, int interval, Date startTime, Date endTime) {
    StringBuilder sql = buildSelectStatement(interval);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumber", monitoredSerialNumber);

    if (StringUtils.isNotBlank(netifName)) {
      sql.append(" and netif_name = :netifName ");
      params.put("netifName", netifName);
    }
    if (CollectionUtils.isNotEmpty(categoryList)) {
      sql.append(" and category in ( :categoryList ) ");
      params.put("categoryList", categoryList);
    }
    if (startTime != null) {
      sql.append(" and metric_time >= :startTime ");
      params.put("startTime", startTime);
    }
    if (endTime != null) {
      sql.append(" and metric_time < :endTime ");
      params.put("endTime", endTime);
    }
    
    sql.append(" order by metric_time asc");
    
    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralNetifDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#queryCentralNetifs(java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<CentralNetifDO> queryCentralNetifs(String deviceType, String monitoredSerialNumber,
      List<Date> metricTimeList) {
    if (CollectionUtils.isEmpty(metricTimeList)) {
      return Lists.newArrayListWithCapacity(Constants.MAP_DEFAULT_SIZE);
    }

    StringBuilder sql = buildSelectStatement(Constants.ONE_MINUTE_SECONDS);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");
    sql.append(" and metric_time in ( :metricTimeList ) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumber", monitoredSerialNumber);
    params.put("metricTimeList", metricTimeList);

    List<CentralNetifDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralNetifDO.class));

    return CollectionUtils.isEmpty(list)
        ? Lists.newArrayListWithCapacity(Constants.MAP_DEFAULT_SIZE)
        : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#queryCentralNetifsBySerialNumbers(java.lang.String, java.util.List, int, java.util.Date, java.util.Date)
   */
  @Override
  public List<CentralNetifDO> queryCentralNetifsBySerialNumbers(String deviceType,
      List<String> monitoredSerialNumbers, int interval, Date startTime, Date endTime) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select monitored_serial_number, metric_time, ");
    sql.append(" SUM(rx_bps) as rx_bps, SUM(tx_bps) as tx_bps, ");
    sql.append(" SUM(rx_pps) as rx_pps, SUM(tx_pps) as tx_pps from ");
    String tableName = TABLE_DEVICE_NETIF_HISTORY;
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName = TABLE_DEVICE_NETIF_HISTORY_5MIN;
    }
    sql.append(tableName);
    sql.append(" where metric_time >= :startTime ");
    sql.append(" and metric_time < :endTime ");
    sql.append(" and category != :category ");
    sql.append(" and device_type = :deviceType ");
    sql.append(" and monitored_serial_number in ( :monitoredSerialNumbers ) ");
    sql.append(" group by monitored_serial_number,metric_time ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("startTime", startTime);
    params.put("endTime", endTime);
    params.put("category", FpcCmsConstants.DEVICE_NETIF_CATEGORY_MGMT);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumbers", monitoredSerialNumbers);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralNetifDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#queryCentralNetifMetricTime(java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<Date> queryCentralNetifMetricTime(String deviceType, String monitoredSerialNumber,
      List<Date> metricTimeList) {
    if (CollectionUtils.isEmpty(metricTimeList)) {
      return Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    }

    StringBuilder sql = new StringBuilder();
    sql.append(" select distinct metric_time from ").append(TABLE_DEVICE_NETIF_HISTORY);
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
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#queryTotalReceivingNetifs(java.lang.String, java.util.List, int, java.util.Date, java.util.Date)
   */
  @Override
  public List<CentralNetifDO> queryTotalReceivingNetifs(String deviceType,
      List<String> monitoredSerialNumbers, int interval, Date startTime, Date endTime) {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder sql = new StringBuilder();
    sql.append("select SUM(rx_bps) as rx_bps, SUM(rx_pps) as rx_pps, metric_time from ");
    String tableName = TABLE_DEVICE_NETIF_HISTORY;
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName = TABLE_DEVICE_NETIF_HISTORY_5MIN;
    }
    sql.append(tableName);

    sql.append(" where category = :category and device_type = :deviceType ");
    params.put("category", FpcCmsConstants.DEVICE_NETIF_CATEGORY_INGEST);
    params.put("deviceType", deviceType);

    if (CollectionUtils.isNotEmpty(monitoredSerialNumbers)) {
      sql.append(" and monitored_serial_number in ( :monitoredSerialNumbers ) ");
      params.put("monitoredSerialNumbers", monitoredSerialNumbers);
    }

    sql.append(" and metric_time >= :startTime ");
    params.put("startTime", startTime);

    sql.append(" and metric_time < :endTime ");
    params.put("endTime", endTime);

    sql.append(" group by metric_time ");

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralNetifDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#queryNetifUsagesByRanking()
   */
  @Override
  public List<CentralNetifUsage> queryNetifUsagesByRanking() {
    StringBuilder sql = new StringBuilder();
    sql.append("select device_type, monitored_serial_number as fpc_id, netif_name, category, ");
    sql.append(" specification as total_bandwidth, ");
    sql.append(" SUM(rx_bps + tx_bps) as usaged_bandwidth from ");
    sql.append(TABLE_DEVICE_NETIF);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number != :monitoredSerialNumber ");
    sql.append(" and specification > 0 ");
    sql.append(
        " group by device_type, monitored_serial_number, netif_name, category, specification ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", FpcCmsConstants.DEVICE_TYPE_TFA);
    params.put("monitoredSerialNumber", licenseService.queryDeviceSerialNumber());

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralNetifUsage.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#saveCentralNetifs(java.util.List)
   */
  @Override
  public void saveCentralNetifs(List<CentralNetifDO> netifTraffics) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_DEVICE_NETIF_HISTORY);
    sql.append(" (id, device_type, monitored_serial_number, netif_name, category, ");
    sql.append(" rx_bps, tx_bps, rx_pps, tx_pps, metric_time) ");
    sql.append(" values (:id, :deviceType, :monitoredSerialNumber, :netifName, :category, ");
    sql.append(" :rxBps, :txBps, :rxPps, :txPps, :metricTime) ");

    for (CentralNetifDO centralNetifDO : netifTraffics) {
      centralNetifDO.setId(IdGenerator.generateUUID());
    }
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(netifTraffics);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.global.dao.DeviceNetifDao#saveOrUpdateDeviceNetif(java.util.List)
   */
  @Transactional
  @Override
  public void saveOrUpdateCentralNetifs(List<CentralNetifDO> netifs) {
    // delete device netif by deviceId
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_DEVICE_NETIF);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", netifs.get(0).getDeviceType());
    params.put("monitoredSerialNumber", netifs.get(0).getMonitoredSerialNumber());

    jdbcTemplate.update(sql.toString(), params);

    // batch insert device netif
    sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_DEVICE_NETIF);
    sql.append(" (id, device_type, monitored_serial_number, netif_name, state, category, ");
    sql.append(" specification, rx_bps, tx_bps, rx_pps, tx_pps, metric_time) ");
    sql.append(
        " values (:id, :deviceType, :monitoredSerialNumber, :netifName, :state, :category, ");
    sql.append(" :specification, :rxBps, :txBps, :rxPps, :txPps, :metricTime) ");

    for (CentralNetifDO netif : netifs) {
      netif.setId(IdGenerator.generateUUID());
    }
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(netifs);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#updateNetifBelongDeviceSerialNumber(java.lang.String, java.lang.String)
   */
  @Override
  public int updateNetifBelongDeviceSerialNumber(String id, String monitoredSerialNumber) {
    StringBuilder sql = new StringBuilder();
    sql.append(" update ").append(TABLE_DEVICE_NETIF);
    sql.append(" set monitored_serial_number = :monitoredSerialNumber ");
    sql.append(" where id = :id ");

    Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    paramMap.put("monitoredSerialNumber", monitoredSerialNumber);
    paramMap.put("id", id);

    return jdbcTemplate.update(sql.toString(), paramMap);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#rollupCentralNetifs(java.util.Date, java.util.Date, java.lang.String, java.lang.String)
   */
  @Override
  public int rollupCentralNetifs(Date startTime, Date endTime, String deviceType,
      String monitoredSerialNumber) {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // collect data
    StringBuilder sql = new StringBuilder();
    sql.append(" select device_type, monitored_serial_number, netif_name, category,");
    sql.append(" ROUND(SUM(rx_bps)/").append(REAL_COLLECT_POINT_COUNT).append(", 0) as rx_bps, ");
    sql.append(" ROUND(SUM(tx_bps)/").append(REAL_COLLECT_POINT_COUNT).append(", 0) as tx_bps, ");
    sql.append(" ROUND(SUM(rx_pps)/").append(REAL_COLLECT_POINT_COUNT).append(", 0) as rx_pps, ");
    sql.append(" ROUND(SUM(tx_pps)/").append(REAL_COLLECT_POINT_COUNT).append(", 0) as tx_pps ");
    sql.append(" from ").append(TABLE_DEVICE_NETIF_HISTORY);
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
    sql.append(" group by device_type,monitored_serial_number,netif_name,category ");

    List<CentralNetifDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CentralNetifDO.class));
    for (CentralNetifDO centralNetifDO : list) {
      centralNetifDO.setId(IdGenerator.generateUUID());
      centralNetifDO.setMetricTime(startTime);
    }

    // batch insert fpc_center_device_netif_traffic_5min
    sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_DEVICE_NETIF_HISTORY_5MIN);
    sql.append(" (id, device_type, monitored_serial_number, netif_name, category, ");
    sql.append(" rx_bps, tx_bps, rx_pps, tx_pps, metric_time) ");
    sql.append(" values (:id, :deviceType, :monitoredSerialNumber, :netifName, :category, ");
    sql.append(" :rxBps, :txBps, :rxPps, :txPps, :metricTime) ");

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(list);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
    return list.size();
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#deleteCentralNetifs(java.util.Date, int)
   */
  @Override
  public int deleteCentralNetifs(Date beforeTime, int interval) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    String tableName = TABLE_DEVICE_NETIF_HISTORY;
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName = TABLE_DEVICE_NETIF_HISTORY_5MIN;
    }
    sql.append(tableName);
    sql.append(" where metric_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("beforeTime", beforeTime);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#deleteCentralNetifs(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCentralNetifs(String deviceType, String monitoredSerialNumber) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_DEVICE_NETIF);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumber", monitoredSerialNumber);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CentralNetifDao#deleteCentralNetifs(java.lang.String, java.lang.String, java.util.Date, int)
   */
  @Override
  public int deleteCentralNetifs(String deviceType, String monitoredSerialNumber, Date metricTime,
      int interval) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    String tableName = TABLE_DEVICE_NETIF_HISTORY;
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName = TABLE_DEVICE_NETIF_HISTORY_5MIN;
    }
    sql.append(tableName);
    sql.append(" where device_type = :deviceType ");
    sql.append(" and monitored_serial_number = :monitoredSerialNumber ");
    sql.append(" and metric_time = :metricTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("metricTime", metricTime);
    params.put("deviceType", deviceType);
    params.put("monitoredSerialNumber", monitoredSerialNumber);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement(int interval) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, device_type, monitored_serial_number, netif_name, category, ");
    sql.append(" rx_bps, tx_bps, rx_pps, tx_pps, metric_time from ");
    String tableName = TABLE_DEVICE_NETIF_HISTORY;
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName = TABLE_DEVICE_NETIF_HISTORY_5MIN;
    }
    sql.append(tableName);
    return sql;
  }

}
