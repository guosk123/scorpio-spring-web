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
import com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao;
import com.machloop.fpc.cms.center.knowledge.data.GeoCustomCountryDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
@Repository
public class GeoCustomCountryDaoImpl implements GeoCustomCountryDao {

  private static final String TABLE_APPLIANCE_GEOIP_COUNTRY = "fpccms_appliance_geoip_country";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#queryGeoCustomCountrys(java.lang.String)
   */
  @Override
  public List<GeoCustomCountryDO> queryGeoCustomCountrys(String deleted) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, deleted);

    List<GeoCustomCountryDO> customCountryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoCustomCountryDO.class));
    return CollectionUtils.isEmpty(customCountryList) ? Lists.newArrayListWithCapacity(0)
        : customCountryList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#queryGeoCustomCountryIds(java.util.Date)
   */
  @Override
  public List<GeoCustomCountryDO> queryGeoCustomCountryIds(boolean onlyLocal) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and assign_id = '' ");
    }

    List<GeoCustomCountryDO> customCountryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoCustomCountryDO.class));
    return CollectionUtils.isEmpty(customCountryList) ? Lists.newArrayListWithCapacity(0)
        : customCountryList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#queryGeoCustomCountry(java.lang.String)
   */
  @Override
  public GeoCustomCountryDO queryGeoCustomCountry(String id) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and id = :id ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<GeoCustomCountryDO> customCountryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoCustomCountryDO.class));
    return CollectionUtils.isEmpty(customCountryList) ? new GeoCustomCountryDO()
        : customCountryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#queryGeoCustomCountryByName(java.lang.String)
   */
  @Override
  public GeoCustomCountryDO queryGeoCustomCountryByName(String name) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and name = :name ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<GeoCustomCountryDO> customCountryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoCustomCountryDO.class));
    return CollectionUtils.isEmpty(customCountryList) ? new GeoCustomCountryDO()
        : customCountryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#queryGeoCustomCountryByCountryId(java.lang.String)
   */
  @Override
  public GeoCustomCountryDO queryGeoCustomCountryByCountryId(String countryId) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and country_id = :countryId ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("countryId", countryId);

    List<GeoCustomCountryDO> customCountryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoCustomCountryDO.class));
    return CollectionUtils.isEmpty(customCountryList) ? new GeoCustomCountryDO()
        : customCountryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#queryGeoCustomCountryByAssignId(java.lang.String)
   */
  @Override
  public GeoCustomCountryDO queryGeoCustomCountryByAssignId(String assignId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and assign_id = :assignId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignId", assignId);

    List<GeoCustomCountryDO> customCountryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoCustomCountryDO.class));

    return CollectionUtils.isEmpty(customCountryList) ? new GeoCustomCountryDO()
        : customCountryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#queryAssignGeoCustomCountryIds(java.util.Date)
   */
  @Override
  public List<GeoCustomCountryDO> queryAssignGeoCustomCountryIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_GEOIP_COUNTRY);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<GeoCustomCountryDO> customCountryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoCustomCountryDO.class));

    return CollectionUtils.isEmpty(customCountryList) ? Lists.newArrayListWithCapacity(0)
        : customCountryList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#countGeoCustomCountrys()
   */
  @Override
  public int countGeoCustomCountrys() {
    StringBuilder sql = new StringBuilder();
    sql.append("select count(id) from ").append(TABLE_APPLIANCE_GEOIP_COUNTRY);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    return jdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
  }

  public GeoCustomCountryDO saveOrRecoverGeoCustomCountry(GeoCustomCountryDO countryDO) {
    GeoCustomCountryDO exist = queryGeoCustomCountryById(
        countryDO.getId() == null ? "" : countryDO.getId());
    if (StringUtils.isBlank(exist == null ? "" : exist.getId())) {
      return saveGeoCustomCountry(countryDO);
    } else {
      recoverAndUpdateGeoCustomCountry(countryDO);
      return queryGeoCustomCountryById(countryDO.getId());
    }
  }

  /**
   * 为了避免主键冲突的情况，此方法会将deleted=1的数据更新为deleted=0，并对数据做相应的更新
   */
  private int recoverAndUpdateGeoCustomCountry(GeoCustomCountryDO countryDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_GEOIP_COUNTRY);
    sql.append(" set name = :name, longitude = :longitude, ");
    sql.append(" latitude = :latitude, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" deleted = :deleted");
    sql.append(" where id = :id ");

    countryDO.setUpdateTime(DateUtils.now());
    countryDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(countryDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private GeoCustomCountryDO queryGeoCustomCountryById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<GeoCustomCountryDO> countryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(GeoCustomCountryDO.class));

    return CollectionUtils.isEmpty(countryList) ? new GeoCustomCountryDO() : countryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#saveGeoCustomCountry(com.machloop.fpc.cms.center.knowledge.data.GeoCustomCountryDO)
   */
  @Override
  public GeoCustomCountryDO saveGeoCustomCountry(GeoCustomCountryDO countryDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_GEOIP_COUNTRY);
    sql.append(" (id, assign_id, name, country_id, longitude, latitude, ");
    sql.append(" description, deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :assignId, :name, :countryId, :longitude, :latitude, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId) ");

    if (StringUtils.isBlank(countryDO.getId())) {
      countryDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(countryDO.getAssignId())) {
      countryDO.setAssignId("");
    }
    countryDO.setCreateTime(DateUtils.now());
    countryDO.setUpdateTime(countryDO.getCreateTime());
    countryDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(countryDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return countryDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#batchSaveGeoCustomCountrys(java.util.List)
   */
  @Override
  public List<GeoCustomCountryDO> batchSaveGeoCustomCountrys(List<GeoCustomCountryDO> countrys) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_GEOIP_COUNTRY);
    sql.append(" (id, name, country_id, longitude, latitude, ");
    sql.append(" description, deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :name, :countryId, :longitude, :latitude, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId) ");

    countrys.forEach(country -> {
      country.setId(IdGenerator.generateUUID());
      country.setCreateTime(DateUtils.now());
      country.setUpdateTime(country.getCreateTime());
      country.setDeleted(Constants.BOOL_NO);
      if (StringUtils.isBlank(country.getAssignId())) {
        country.setAssignId("");
      }
    });

    SqlParameterSource[] createBatch = SqlParameterSourceUtils.createBatch(countrys);
    jdbcTemplate.batchUpdate(sql.toString(), createBatch);
    return countrys;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#updateGeoCustomCountry(com.machloop.fpc.cms.center.knowledge.data.GeoCustomCountryDO)
   */
  @Override
  public int updateGeoCustomCountry(GeoCustomCountryDO countryDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_GEOIP_COUNTRY);
    sql.append(" set name = :name, longitude = :longitude, ");
    sql.append(" latitude = :latitude, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    countryDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(countryDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.GeoCustomCountryDao#deleteGeoCustomCountry(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteGeoCustomCountry(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_GEOIP_COUNTRY);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    GeoCustomCountryDO countryDO = new GeoCustomCountryDO();
    countryDO.setDeleted(Constants.BOOL_YES);
    countryDO.setDeleteTime(DateUtils.now());
    countryDO.setOperatorId(operatorId);
    countryDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(countryDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id, name, country_id, longitude, latitude, description, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_GEOIP_COUNTRY);
    return sql;
  }

}
