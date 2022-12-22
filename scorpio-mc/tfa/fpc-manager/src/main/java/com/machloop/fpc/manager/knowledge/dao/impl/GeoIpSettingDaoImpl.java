package com.machloop.fpc.manager.knowledge.dao.impl;

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
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao;
import com.machloop.fpc.manager.knowledge.data.GeoIpSettingDO;

/**
 * @author guosk
 *
 * create at 2021年8月25日, fpc-manager
 */
@Repository
public class GeoIpSettingDaoImpl implements GeoIpSettingDao {

  private static final String TABLE_APPLIANCE_GEOIP_SETTINGS = "fpc_appliance_geoip_settings";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#queryGeoIpSettings()
   */
  @Override
  public List<GeoIpSettingDO> queryGeoIpSettings() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<GeoIpSettingDO> geoIpSettings = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoIpSettingDO.class));
    return CollectionUtils.isEmpty(geoIpSettings) ? Lists.newArrayListWithCapacity(0)
        : geoIpSettings;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#queryGeoCountryIpSettingIds(boolean)
   */
  @Override
  public List<GeoIpSettingDO> queryGeoCountryIpSettingIds(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select country_id, province_id, city_id from ")
        .append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and geoip_setting_in_cms_id = '' ");
    }

    List<GeoIpSettingDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoIpSettingDO.class));
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#queryGeoCountryIpSettingIds(java.util.Date)
   */
  @Override
  public List<String> queryGeoCountryIpSettingIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select geoip_setting_in_cms_id from ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" where deleted = :deleted and geoip_setting_in_cms_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#queryGeoCountryIpSettings()
   */
  @Override
  public List<GeoIpSettingDO> queryGeoCountryIpSettings() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and province_id = '0' and city_id = '0' ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<GeoIpSettingDO> geoIpSettings = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoIpSettingDO.class));
    return CollectionUtils.isEmpty(geoIpSettings) ? Lists.newArrayListWithCapacity(0)
        : geoIpSettings;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#queryGeoIpSetting(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public GeoIpSettingDO queryGeoIpSetting(String countryId, String provinceId, String cityId) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and country_id = :countryId ");
    whereSql.append(" and province_id = :provinceId and city_id = :cityId ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("countryId", countryId);
    params.put("provinceId", provinceId);
    params.put("cityId", cityId);

    List<GeoIpSettingDO> geoIpSettings = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoIpSettingDO.class));
    return CollectionUtils.isEmpty(geoIpSettings) ? new GeoIpSettingDO() : geoIpSettings.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#queryGeoIpSettingByCountryId(java.lang.String)
   */
  @Override
  public GeoIpSettingDO queryGeoIpSettingByCountryId(String countryId) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and country_id = :countryId ");
    whereSql.append(" and province_id = '0' and city_id = '0' ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("countryId", countryId);

    List<GeoIpSettingDO> geoIpSettings = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoIpSettingDO.class));
    return CollectionUtils.isEmpty(geoIpSettings) ? new GeoIpSettingDO() : geoIpSettings.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#saveOrUpdateGeoIpSetting(com.machloop.fpc.manager.knowledge.data.GeoIpSettingDO)
   */
  @Override
  public int saveOrUpdateGeoIpSetting(GeoIpSettingDO geoIpSetting) {
    int update = updateGeoIpSetting(geoIpSetting);
    return update > 0 ? update : saveGeoIpSetting(geoIpSetting);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#saveGeoIpSetting(com.machloop.fpc.manager.knowledge.data.GeoIpSettingDO)
   */
  public int saveGeoIpSetting(GeoIpSettingDO geoIpSetting) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" (id, country_id, province_id, city_id, ip_address, geoip_setting_in_cms_id, ");
    sql.append(" deleted, create_time, update_time, operator_id) ");
    sql.append(
        " values (:id, :countryId, :provinceId, :cityId, :ipAddress, :geoipSettingInCmsId, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    if (StringUtils.isBlank(geoIpSetting.getGeoipSettingInCmsId())) {
      geoIpSetting.setGeoipSettingInCmsId("");
    }
    if (StringUtils.isBlank(geoIpSetting.getId())) {
      geoIpSetting.setId(IdGenerator.generateUUID());

    }
    geoIpSetting.setCreateTime(DateUtils.now());
    geoIpSetting.setUpdateTime(geoIpSetting.getCreateTime());
    geoIpSetting.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(geoIpSetting);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#updateGeoIpSetting(com.machloop.fpc.manager.knowledge.data.GeoIpSettingDO)
   */
  @Override
  public int updateGeoIpSetting(GeoIpSettingDO geoIpSetting) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" set ip_address = :ipAddress, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where deleted = :deleted and country_id = :countryId ");
    sql.append(" and province_id = :provinceId and city_id = :cityId ");

    geoIpSetting.setUpdateTime(DateUtils.now());
    geoIpSetting.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(geoIpSetting);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#batchSaveGeoIpSettings(java.util.List)
   */
  @Override
  public int batchSaveGeoIpSettings(List<GeoIpSettingDO> geoIpSettings) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" (id, country_id, province_id, city_id, ip_address, geoip_setting_in_cms_id, ");
    sql.append(" deleted, create_time, update_time, operator_id) ");
    sql.append(
        " values (:id, :countryId, :provinceId, :cityId, :ipAddress, :geoipSettingInCmsId, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    geoIpSettings.forEach(geoIpSetting -> {
      if (StringUtils.isBlank(geoIpSetting.getId())) {
        geoIpSetting.setId(IdGenerator.generateUUID());
      }
      geoIpSetting.setCreateTime(DateUtils.now());
      geoIpSetting.setUpdateTime(geoIpSetting.getCreateTime());
      geoIpSetting.setDeleted(Constants.BOOL_NO);
      if (StringUtils.isBlank(geoIpSetting.getGeoipSettingInCmsId())) {
        geoIpSetting.setGeoipSettingInCmsId("");
      }
    });

    SqlParameterSource[] createBatch = SqlParameterSourceUtils.createBatch(geoIpSettings);
    return jdbcTemplate.batchUpdate(sql.toString(), createBatch)[0];
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#deleteGeoIpSetting(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int deleteGeoIpSetting(String countryId, String provinceId, String cityId,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where deleted = '0' and country_id = :countryId and province_id = :provinceId ");
    sql.append(" and city_id = :cityId ");

    GeoIpSettingDO geoIpSettingDO = new GeoIpSettingDO();
    geoIpSettingDO.setDeleted(Constants.BOOL_YES);
    geoIpSettingDO.setDeleteTime(DateUtils.now());
    geoIpSettingDO.setOperatorId(operatorId);
    geoIpSettingDO.setCountryId(countryId);
    geoIpSettingDO.setProvinceId(provinceId);
    geoIpSettingDO.setCityId(cityId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(geoIpSettingDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao#deleteByCountryIds(java.util.List, java.lang.String)
   */
  @Override
  public int deleteByCountryIds(List<String> countryIds, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where deleted = '0' and country_id in (:countryIds) ");

    Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    paramMap.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_YES);
    paramMap.put("deleteTime", DateUtils.now());
    paramMap.put("operatorId", operatorId);
    paramMap.put("countryIds", countryIds);

    return jdbcTemplate.update(sql.toString(), paramMap);
  }

  private StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append(
        "select id, country_id, province_id, city_id, ip_address, geoip_setting_in_cms_id, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    return sql;
  }

}
