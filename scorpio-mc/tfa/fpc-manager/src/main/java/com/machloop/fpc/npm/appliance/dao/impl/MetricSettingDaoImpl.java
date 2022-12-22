package com.machloop.fpc.npm.appliance.dao.impl;

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
import com.machloop.fpc.npm.appliance.dao.MetricSettingDao;
import com.machloop.fpc.npm.appliance.data.MetricSettingDO;

/**
 * @author guosk
 *
 * create at 2021年4月6日, fpc-manager
 */
@Repository
public class MetricSettingDaoImpl implements MetricSettingDao {

  private static final String TABLE_APPLIANCE_METRIC_SETTINGS = "fpc_appliance_metric_settings";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.MetricSettingDao#queryMetricSettings(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricSettingDO> queryMetricSettings(String sourceType, String networkId,
      String serviceId, String packetFileId) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select id, source_type, network_id, service_id, packet_file_id, metric, value, ");
    sql.append(" metric_setting_in_cms_id, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_METRIC_SETTINGS);
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
   * @see com.machloop.fpc.npm.appliance.dao.MetricSettingDao#queryAssignMetricSettingIds(java.lang.String, java.lang.String, java.lang.String, java.util.Date)
   */
  @Override
  public List<String> queryAssignMetricSettingIds(String sourceType, String networkId,
      String serviceId, Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_METRIC_SETTINGS);
    sql.append(" where update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("beforeTime", beforeTime);

    if (StringUtils.isNotBlank(networkId)) {
      sql.append(" and network_id = :networkId ");
      params.put("networkId", networkId);
    }

    if (StringUtils.isNotBlank(serviceId)) {
      sql.append(" and service_id = :serviceId ");
      params.put("serviceId", serviceId);
    }

    if (StringUtils.isNotBlank(sourceType)) {
      sql.append(" and source_type = :sourceType ");
      params.put("sourceType", sourceType);
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.MetricSettingDao#batchSaveMetricSetting(java.util.List)
   */
  @Override
  public int batchSaveMetricSetting(List<MetricSettingDO> metricSettings) {
    StringBuilder saveSql = new StringBuilder();
    saveSql.append("insert into ").append(TABLE_APPLIANCE_METRIC_SETTINGS);
    saveSql.append(" (id, source_type, network_id, service_id, metric, value, ");
    saveSql.append(" metric_setting_in_cms_id, update_time, operator_id) ");
    saveSql.append(" values(:id, :sourceType, :networkId, :serviceId, :metric, :value, ");
    saveSql.append(" :metricSettingInCmsId, :updateTime, :operatorId) ");
    metricSettings.forEach(metricSetting -> {
      if(StringUtils.isBlank(metricSetting.getId())) {
        metricSetting.setId(IdGenerator.generateUUID());
      }
      metricSetting.setUpdateTime(DateUtils.now());
      if (StringUtils.isBlank(metricSetting.getMetricSettingInCmsId())) {
        metricSetting.setMetricSettingInCmsId("");
      }
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(metricSettings);
    return jdbcTemplate.batchUpdate(saveSql.toString(), batchSource)[0];
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.MetricSettingDao#updateMetricSetting(com.machloop.fpc.npm.appliance.data.MetricSettingDO)
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
   * @see com.machloop.fpc.npm.appliance.dao.MetricSettingDao#deleteMetricSetting(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
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

}
