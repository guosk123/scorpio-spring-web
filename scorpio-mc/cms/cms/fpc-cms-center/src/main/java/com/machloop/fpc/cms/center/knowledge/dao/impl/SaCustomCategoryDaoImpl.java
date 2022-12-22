package com.machloop.fpc.cms.center.knowledge.dao.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao;
import com.machloop.fpc.cms.center.knowledge.data.SaCustomCategoryDO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author guosk
 *
 * create at 2021年1月4日, fpc-manager
 */
@Repository
public class SaCustomCategoryDaoImpl implements SaCustomCategoryDao {

  private static final String TABLE_APPLIANCE_SA_CATEGORY = "fpccms_appliance_sa_category";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#querySaCustomCategorys()
   */
  @Override
  public List<SaCustomCategoryDO> querySaCustomCategorys() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<SaCustomCategoryDO> customCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomCategoryDO.class));
    return CollectionUtils.isEmpty(customCategoryList) ? Lists.newArrayListWithCapacity(0)
        : customCategoryList;
  }


  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#querySaCustomCategoryIds(java.util.Date)
   */
  @Override
  public List<String> querySaCustomCategoryIds(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_SA_CATEGORY);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and assign_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#querySaCustomCategory(String)
   */
  @Override
  public SaCustomCategoryDO querySaCustomCategory(String id) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and id = :id ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<SaCustomCategoryDO> customCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomCategoryDO.class));
    return CollectionUtils.isEmpty(customCategoryList) ? new SaCustomCategoryDO()
        : customCategoryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#querySaCustomCategoryByName(String)
   */
  @Override
  public SaCustomCategoryDO querySaCustomCategoryByName(String name) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and name = :name ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<SaCustomCategoryDO> customCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomCategoryDO.class));
    return CollectionUtils.isEmpty(customCategoryList) ? new SaCustomCategoryDO()
        : customCategoryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#querySaCustomCategoryByCategoryId(String)
   */
  @Override
  public SaCustomCategoryDO querySaCustomCategoryByCategoryId(String categoryId) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and category_id = :categoryId ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("categoryId", categoryId);

    List<SaCustomCategoryDO> customCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomCategoryDO.class));
    return CollectionUtils.isEmpty(customCategoryList) ? new SaCustomCategoryDO()
        : customCategoryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#queryCustomCategoryByAssignId(java.lang.String)
   */
  @Override
  public SaCustomCategoryDO queryCustomCategoryByAssignId(String assignId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and assign_id = :assignId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignId", assignId);

    List<SaCustomCategoryDO> customCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomCategoryDO.class));

    return CollectionUtils.isEmpty(customCategoryList) ? new SaCustomCategoryDO()
        : customCategoryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#queryAssignSaCustomCategoryIds(java.util.Date)
   */
  @Override
  public List<SaCustomCategoryDO> queryAssignSaCustomCategoryIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_SA_CATEGORY);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<SaCustomCategoryDO> customCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomCategoryDO.class));

    return CollectionUtils.isEmpty(customCategoryList) ? Lists.newArrayListWithCapacity(0)
        : customCategoryList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#countSaCustomCategorys()
   */
  @Override
  public int countSaCustomCategorys() {
    StringBuilder sql = new StringBuilder();
    sql.append("select count(id) from ").append(TABLE_APPLIANCE_SA_CATEGORY);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    return jdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#saveSaCustomCategory(com.machloop.fpc.cms.center.knowledge.data.SaCustomCategoryDO)
   */
  @Override
  public SaCustomCategoryDO saveSaCustomCategory(SaCustomCategoryDO categoryDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SA_CATEGORY);
    sql.append(" (id, assign_id, name, category_id, ");
    sql.append(" description, deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :assignId, :name, :categoryId, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(categoryDO.getId())) {
      categoryDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(categoryDO.getAssignId())) {
      categoryDO.setAssignId("");
    }
    categoryDO.setCreateTime(DateUtils.now());
    categoryDO.setUpdateTime(categoryDO.getCreateTime());
    categoryDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(categoryDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return categoryDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#updateSaCustomCategory(com.machloop.fpc.cms.center.knowledge.data.SaCustomCategoryDO)
   */
  @Override
  public int updateSaCustomCategory(SaCustomCategoryDO categoryDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SA_CATEGORY);
    sql.append(" set name = :name, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    categoryDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(categoryDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomCategoryDao#deleteSaCustomCategory(String, String)
   */
  @Override
  public int deleteSaCustomCategory(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SA_CATEGORY);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    SaCustomCategoryDO categoryDO = new SaCustomCategoryDO();
    categoryDO.setDeleted(Constants.BOOL_YES);
    categoryDO.setDeleteTime(DateUtils.now());
    categoryDO.setOperatorId(operatorId);
    categoryDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(categoryDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id, name, category_id, description, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SA_CATEGORY);
    return sql;
  }
}
