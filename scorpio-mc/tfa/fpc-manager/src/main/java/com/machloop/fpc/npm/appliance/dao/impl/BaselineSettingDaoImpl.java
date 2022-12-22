package com.machloop.fpc.npm.appliance.dao.impl;

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
import com.machloop.fpc.npm.appliance.dao.BaselineSettingDao;
import com.machloop.fpc.npm.appliance.data.BaselineSettingDO;

/**
 * @author guosk
 *
 * create at 2021年4月21日, fpc-manager
 */
@Repository
public class BaselineSettingDaoImpl implements BaselineSettingDao {

  private static final String TABLE_APPLIANCE_BASELINE_SETTINGS = "fpc_appliance_baseline_settings";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.BaselineSettingDao#queryBaselineSettings()
   */
  @Override
  public List<BaselineSettingDO> queryBaselineSettings() {
    StringBuilder sql = new StringBuilder();
    sql.append(" select id, source_type, network_id, service_id, category, weighting_model, ");
    sql.append(" windowing_model, windowing_count, update_time ");
    sql.append(" from ").append(TABLE_APPLIANCE_BASELINE_SETTINGS);

    List<BaselineSettingDO> baselineSettingList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(BaselineSettingDO.class));

    return CollectionUtils.isEmpty(baselineSettingList) ? Lists.newArrayListWithCapacity(0)
        : baselineSettingList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.BaselineSettingDao#queryBaselineSettings(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<BaselineSettingDO> queryBaselineSettings(String sourceType, String networkId,
      String serviceId, String category) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select id, source_type, network_id, service_id, category, weighting_model, ");
    sql.append(" windowing_model, windowing_count,update_time ");
    sql.append(" from ").append(TABLE_APPLIANCE_BASELINE_SETTINGS);
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

    if (StringUtils.isNotBlank(category)) {
      sql.append(" and category = :category ");
      params.put("category", category);
    }

    List<BaselineSettingDO> baselineSettingList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(BaselineSettingDO.class));

    return CollectionUtils.isEmpty(baselineSettingList) ? Lists.newArrayListWithCapacity(0)
        : baselineSettingList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.BaselineSettingDao#batchUpdateBaselineSetting(java.util.List)
   */
  @Override
  public int batchUpdateBaselineSetting(List<BaselineSettingDO> baselineSettingDOList) {
    // delete
    StringBuilder deleteSql = new StringBuilder();
    deleteSql.append("delete from ").append(TABLE_APPLIANCE_BASELINE_SETTINGS);
    deleteSql.append(" where source_type = :sourceType and category = :category");
    BaselineSettingDO baselineSettingDO = baselineSettingDOList.get(0);
    if (StringUtils.isNotBlank(baselineSettingDO.getNetworkId())) {
      deleteSql.append(" and network_id = :networkId ");
    }
    if (StringUtils.isNotBlank(baselineSettingDO.getServiceId())) {
      deleteSql.append(" and service_id = :serviceId ");
    }
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(baselineSettingDO);
    jdbcTemplate.update(deleteSql.toString(), paramSource);

    // save
    StringBuilder saveSql = new StringBuilder();
    saveSql.append("insert into ").append(TABLE_APPLIANCE_BASELINE_SETTINGS);
    saveSql.append(" (id, source_type, network_id, service_id, category, weighting_model, ");
    saveSql.append(" windowing_model, windowing_count, update_time, operator_id) ");
    saveSql.append("values(:id, :sourceType, :networkId, :serviceId, :category, :weightingModel, ");
    saveSql.append(" :windowingModel, :windowingCount, :updateTime, :operatorId) ");
    baselineSettingDOList.forEach(baselineSetting -> {
      baselineSetting.setId(IdGenerator.generateUUID());
      baselineSetting.setUpdateTime(DateUtils.now());
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(baselineSettingDOList);
    return jdbcTemplate.batchUpdate(saveSql.toString(), batchSource)[0];
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.BaselineSettingDao#deleteBaselineSetting(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int deleteBaselineSetting(String sourceType, String networkId, String serviceId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_BASELINE_SETTINGS);
    sql.append(" where source_type = :sourceType ");

    if (StringUtils.isNotBlank(networkId)) {
      sql.append(" and network_id = :networkId ");
    }

    if (StringUtils.isNotBlank(serviceId)) {
      sql.append(" and service_id = :serviceId ");
    }

    BaselineSettingDO baselineSettingDO = new BaselineSettingDO();
    baselineSettingDO.setSourceType(sourceType);
    baselineSettingDO.setNetworkId(networkId);
    baselineSettingDO.setServiceId(serviceId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(baselineSettingDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

}
