package com.machloop.fpc.cms.center.appliance.dao.postgres;

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
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.MetricSettingDao;
import com.machloop.fpc.cms.center.appliance.data.MetricSettingDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
@Repository
public class MetricSettingDaoImpl implements MetricSettingDao {

  private static final String TABLE_APPLIANCE_METRIC_SETTINGS = "fpccms_appliance_metric_settings";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.npm.appliance.dao.MetricSettingDao#queryMetricSettings(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricSettingDO> queryMetricSettings(String sourceType, String networkId,
      String serviceId, String packetFileId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where source_type = :sourceType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("sourceType", sourceType);

    if (StringUtils.isNotBlank(networkId)) {
      sql.append(" and network_id = :networkId ");
      params.put("networkId", networkId);
    }

    if (StringUtils.isNotBlank(serviceId)) {
      sql.append(" and service_id = :serviceId ");
      params.put("serviceId", serviceId);
    }

    if (StringUtils.isNotBlank(packetFileId)) {
      sql.append(" and packet_file_id = :packetFileId ");
      params.put("packetFileId", packetFileId);
    }

    List<MetricSettingDO> metricSettingList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(MetricSettingDO.class));

    return CollectionUtils.isEmpty(metricSettingList) ? Lists.newArrayListWithCapacity(0)
        : metricSettingList;
  }

  /**
   * @see com.machloop.fpc.cms.center.npm.appliance.dao.MetricSettingDao#queryMetricSettings()
   */
  @Override
  public List<MetricSettingDO> queryMetricSettings() {
    StringBuilder sql = buildSelectStatement();

    List<MetricSettingDO> metricSettingsList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(MetricSettingDO.class));

    return CollectionUtils.isEmpty(metricSettingsList) ? Lists.newArrayListWithCapacity(0)
        : metricSettingsList;
  }

  /**
   * @see com.machloop.fpc.cms.center.npm.appliance.dao.MetricSettingDao#queryMetricSettingIds(java.lang.String, java.lang.String, java.lang.String, java.util.Date)
   */
  @Override
  public List<String> queryMetricSettingIds(String sourceType, String networkId, String serviceId,
      Date beforeTime) {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_METRIC_SETTINGS);
    sql.append(" where update_time < :beforeTime ");
    params.put("beforeTime", beforeTime);

    if (StringUtils.isNotBlank(sourceType)) {
      sql.append(" and source_type = :sourceType ");
      params.put("sourceType", sourceType);
    }
    if (StringUtils.isNotBlank(networkId)) {
      sql.append(" and network_id = :networkId ");
      params.put("networkId", networkId);
    }
    if (StringUtils.isNotBlank(serviceId)) {
      sql.append(" and service_id = :serviceId ");
      params.put("serviceId", serviceId);
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.npm.appliance.dao.MetricSettingDao#queryAssignMetricSettingIds(java.lang.String, java.util.Date)
   */
  @Override
  public List<String> queryAssignMetricSettingIds(String sourceType, Date beforeTime) {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder sql = new StringBuilder();
    sql.append("select assign_id from ").append(TABLE_APPLIANCE_METRIC_SETTINGS);
    sql.append(" where update_time < :beforeTime ");
    params.put("beforeTime", beforeTime);

    if (StringUtils.isNotBlank(sourceType)) {
      sql.append(" and source_type = :sourceType ");
      params.put("sourceType", sourceType);
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.npm.appliance.dao.MetricSettingDao#batchSaveMetricSetting(java.util.List)
   */
  @Override
  public int batchSaveMetricSetting(List<MetricSettingDO> metricSettings) {
    StringBuilder saveSql = new StringBuilder();
    saveSql.append("insert into ").append(TABLE_APPLIANCE_METRIC_SETTINGS);
    saveSql.append(" (id, assign_id, source_type, network_id, service_id, metric, value, ");
    saveSql.append(" update_time, operator_id) ");
    saveSql
        .append(" values(:id, :assignId, :sourceType, :networkId, :serviceId, :metric, :value, ");
    saveSql.append(" :updateTime, :operatorId) ");
    metricSettings.forEach(metricSetting -> {
      metricSetting.setId(IdGenerator.generateUUID());
      metricSetting.setUpdateTime(DateUtils.now());
      if (StringUtils.isBlank(metricSetting.getAssignId())) {
        metricSetting.setAssignId("");
      }
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(metricSettings);
    return jdbcTemplate.batchUpdate(saveSql.toString(), batchSource)[0];
  }

  /**
   * @see com.machloop.fpc.cms.center.npm.appliance.dao.MetricSettingDao#updateMetricSetting(com.machloop.fpc.cms.center.npm.appliance.data.MetricSettingDO)
   */
  @Override
  public int updateMetricSetting(MetricSettingDO metricSettingDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_METRIC_SETTINGS);
    sql.append(" set value = :value, update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where source_type = :sourceType and metric = :metric ");

    if (StringUtils.isNotBlank(metricSettingDO.getNetworkId())) {
      sql.append(" and network_id = :networkId ");
    }

    if (StringUtils.isNotBlank(metricSettingDO.getServiceId())) {
      sql.append(" and service_id = :serviceId ");
    }

    if (StringUtils.isNotBlank(metricSettingDO.getPacketFileId())) {
      sql.append(" and packet_file_id = :packetFileId ");
    }
    metricSettingDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(metricSettingDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.npm.appliance.dao.MetricSettingDao#updateMetricSetting(com.machloop.fpc.cms.center.npm.appliance.data.MetricSettingDO)
   */
  @Override
  public int saveOrUpdateMetricSetting(MetricSettingDO metricSettingDO) {
    int update = updateMetricSetting(metricSettingDO);
    return update > 0 ? update : batchSaveMetricSetting(Lists.newArrayList(metricSettingDO));
  }

  /**
   * @see com.machloop.fpc.cms.center.npm.appliance.dao.MetricSettingDao#deleteMetricSetting(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int deleteMetricSetting(String sourceType, String networkId, String serviceId,
      String packetFileId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_METRIC_SETTINGS);
    sql.append(" where source_type = :sourceType ");

    if (StringUtils.isNotBlank(networkId)) {
      sql.append(" and network_id = :networkId ");
    }

    if (StringUtils.isNotBlank(serviceId)) {
      sql.append(" and service_id = :serviceId ");
    }

    if (StringUtils.isNotBlank(packetFileId)) {
      sql.append(" and packet_file_id = :packetFileId ");
    }

    MetricSettingDO metricSettingDO = new MetricSettingDO();
    metricSettingDO.setSourceType(sourceType);
    metricSettingDO.setNetworkId(networkId);
    metricSettingDO.setServiceId(serviceId);
    metricSettingDO.setPacketFileId(packetFileId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(metricSettingDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append(" select id, assign_id, source_type, network_id, service_id, ");
    sql.append(" packet_file_id, metric, value, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_METRIC_SETTINGS);

    return sql;
  }

}
