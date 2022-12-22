package com.machloop.fpc.cms.center.knowledge.dao.impl;

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
import com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao;
import com.machloop.fpc.cms.center.knowledge.data.GeoIpSettingDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
@Repository
public class GeoIpSettingDaoImpl implements GeoIpSettingDao {

  private static final String TABLE_APPLIANCE_GEOIP_SETTINGS = "fpccms_appliance_geoip_settings";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#queryGeoIpSettings()
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
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#queryGeoCountryIpSettingIds(java.util.Date)
   */
  @Override
  public List<String> queryGeoIpSettingIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" where deleted = :deleted and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#queryGeoCountryIpSettings()
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
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#queryGeoIpSetting(java.lang.String, java.lang.String, java.lang.String)
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
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#queryGeoIpSettingByCountryId(java.lang.String)
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
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#queryAssignGeoIpSettingIds(java.util.Date)
   */
  @Override
  public List<GeoIpSettingDO> queryAssignGeoIpSettingIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<GeoIpSettingDO> geoIpSettings = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoIpSettingDO.class));

    return CollectionUtils.isEmpty(geoIpSettings) ? Lists.newArrayListWithCapacity(0)
        : geoIpSettings;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#saveOrUpdateGeoIpSetting(com.machloop.fpc.cms.center.knowledge.data.GeoIpSettingDO)
   */
  @Override
  public GeoIpSettingDO saveOrUpdateGeoIpSetting(GeoIpSettingDO geoIpSetting) {
    int update = updateGeoIpSetting(geoIpSetting);
    return update > 0 ? geoIpSetting : saveGeoIpSetting(geoIpSetting);
  }

  
  public GeoIpSettingDO saveOrRecoverGeoIpSetting(GeoIpSettingDO geoIpSetting) {
    GeoIpSettingDO exist = queryGeoIpSettingById(
        geoIpSetting.getId() == null ? "" : geoIpSetting.getId());
    if (StringUtils.isBlank(exist == null ? "" : exist.getId())) {
      return saveGeoIpSetting(geoIpSetting);
    } else {
      recoverAndUpdateGeoIpSetting(geoIpSetting);
      return queryGeoIpSettingById(geoIpSetting.getId());
    }
  }

  /**
   * 为了避免主键冲突的情况，此方法会将deleted=1的数据更新为deleted=0，并对数据做相应的更新
   */
  private GeoIpSettingDO recoverAndUpdateGeoIpSetting(GeoIpSettingDO geoIpSetting) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" set ip_address = :ipAddress, country_id = :countryId, ");
    sql.append(" province_id = :provinceId, city_id = :cityId, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId, ");
    sql.append(" deleted = :deleted");
    sql.append(" where id = :id ");

    geoIpSetting.setUpdateTime(DateUtils.now());
    geoIpSetting.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(geoIpSetting);
    jdbcTemplate.update(sql.toString(), paramSource);
    return geoIpSetting;
  }

  private GeoIpSettingDO queryGeoIpSettingById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<GeoIpSettingDO> ipSettingList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoIpSettingDO.class));

    return CollectionUtils.isEmpty(ipSettingList) ? new GeoIpSettingDO() : ipSettingList.get(0);
  }
  
  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#saveGeoIpSetting(com.machloop.fpc.cms.center.knowledge.data.GeoIpSettingDO)
   */
  @Override
  public GeoIpSettingDO saveGeoIpSetting(GeoIpSettingDO geoIpSetting) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" (id, assign_id, country_id, province_id, city_id, ip_address, ");
    sql.append(" deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :assignId, :countryId, :provinceId, :cityId, :ipAddress, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    geoIpSetting.setId(IdGenerator.generateUUID());
    geoIpSetting.setCreateTime(DateUtils.now());
    geoIpSetting.setUpdateTime(geoIpSetting.getCreateTime());
    geoIpSetting.setDeleted(Constants.BOOL_NO);
    if (StringUtils.isBlank(geoIpSetting.getAssignId())) {
      geoIpSetting.setAssignId("");
    }

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(geoIpSetting);
    jdbcTemplate.update(sql.toString(), paramSource);
    return geoIpSetting;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#updateGeoIpSetting(com.machloop.fpc.cms.center.knowledge.data.GeoIpSettingDO)
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
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#batchSaveGeoIpSettings(java.util.List)
   */
  @Override
  public List<GeoIpSettingDO> batchSaveGeoIpSettings(List<GeoIpSettingDO> geoIpSettings) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    sql.append(" (id, assign_id, country_id, province_id, city_id, ip_address, ");
    sql.append(" deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :assignId, :countryId, :provinceId, :cityId, :ipAddress, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    geoIpSettings.forEach(geoIpSetting -> {
      geoIpSetting.setId(IdGenerator.generateUUID());
      geoIpSetting.setCreateTime(DateUtils.now());
      geoIpSetting.setUpdateTime(geoIpSetting.getCreateTime());
      geoIpSetting.setDeleted(Constants.BOOL_NO);
      if (StringUtils.isBlank(geoIpSetting.getAssignId())) {
        geoIpSetting.setAssignId("");
      }
    });

    SqlParameterSource[] createBatch = SqlParameterSourceUtils.createBatch(geoIpSettings);
    jdbcTemplate.batchUpdate(sql.toString(), createBatch);
    return geoIpSettings;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoIpSettingDao#deleteGeoIpSetting(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
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

  private StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, country_id, province_id, city_id, ip_address, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_GEOIP_SETTINGS);
    return sql;
  }

}
