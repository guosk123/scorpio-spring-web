package com.machloop.fpc.cms.center.knowledge.dao.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao;
import com.machloop.fpc.cms.center.knowledge.data.SaCustomSubCategoryDO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月18日, fpc-manager
 */
@Repository
public class SaCustomSubCategoryDaoImpl implements SaCustomSubCategoryDao {

  private static final String TABLE_APPLIANCE_SA_SUBCATEGORY = "fpccms_appliance_sa_subcategory";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#querySaCustomSubCategorys()
   */
  @Override
  public List<SaCustomSubCategoryDO> querySaCustomSubCategorys() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<SaCustomSubCategoryDO> customSubCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomSubCategoryDO.class));
    return CollectionUtils.isEmpty(customSubCategoryList) ? Lists.newArrayListWithCapacity(0)
        : customSubCategoryList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao#querySaCustomSubCategoryIds(java.util.Date)
   */
  @Override
  public List<String> querySaCustomSubCategoryIds() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_SA_SUBCATEGORY);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao#querySubCategoryByCategoryId(String)
   */
  @Override
  public List<SaCustomSubCategoryDO> querySubCategoryByCategoryId(String categoryId) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted and category_id = :categoryId ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("categoryId", categoryId);

    List<SaCustomSubCategoryDO> customSubCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomSubCategoryDO.class));
    return CollectionUtils.isEmpty(customSubCategoryList) ? Lists.newArrayListWithCapacity(0)
        : customSubCategoryList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao#querySubCategoryBySubCategoryIds(List)
   */
  @Override
  public List<SaCustomSubCategoryDO> querySubCategoryBySubCategoryIds(List<String> subCategoryIds) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted and sub_category_id in (:subCategoryIds) ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("subCategoryIds", subCategoryIds);

    List<SaCustomSubCategoryDO> customSubCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomSubCategoryDO.class));
    return CollectionUtils.isEmpty(customSubCategoryList) ? Lists.newArrayListWithCapacity(0)
        : customSubCategoryList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#querySaCustomSubCategory(String)
   */
  @Override
  public SaCustomSubCategoryDO querySaCustomSubCategory(String id) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and id = :id ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<SaCustomSubCategoryDO> customSubCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomSubCategoryDO.class));
    return CollectionUtils.isEmpty(customSubCategoryList) ? new SaCustomSubCategoryDO()
        : customSubCategoryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#querySaCustomSubCategoryByName(String)
   */
  @Override
  public SaCustomSubCategoryDO querySaCustomSubCategoryByName(String name) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and name = :name ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<SaCustomSubCategoryDO> customSubCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomSubCategoryDO.class));
    return CollectionUtils.isEmpty(customSubCategoryList) ? new SaCustomSubCategoryDO()
        : customSubCategoryList.get(0);
  }

  /** 
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao#querySaCustomSubCategoryBySubCategoryId(String)
   */
  @Override
  public SaCustomSubCategoryDO querySaCustomSubCategoryBySubCategoryId(String subCategoryId) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and sub_category_id = :subCategoryId ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("subCategoryId", subCategoryId);

    List<SaCustomSubCategoryDO> customSubCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomSubCategoryDO.class));
    return CollectionUtils.isEmpty(customSubCategoryList) ? new SaCustomSubCategoryDO()
        : customSubCategoryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao#queryCustomSubCategoryByAssignId(java.lang.String)
   */
  @Override
  public SaCustomSubCategoryDO queryCustomSubCategoryByAssignId(String assignId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and assign_id = :assignId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignId", assignId);

    List<SaCustomSubCategoryDO> customSubCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomSubCategoryDO.class));

    return CollectionUtils.isEmpty(customSubCategoryList) ? new SaCustomSubCategoryDO()
        : customSubCategoryList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao#queryAssignSaCustomSubCategorys(java.lang.String)
   */
  @Override
  public List<SaCustomSubCategoryDO> queryAssignSaCustomSubCategoryIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_SA_SUBCATEGORY);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<SaCustomSubCategoryDO> customSubCategoryList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomSubCategoryDO.class));

    return CollectionUtils.isEmpty(customSubCategoryList) ? Lists.newArrayListWithCapacity(0)
        : customSubCategoryList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao#saveSaCustomSubCategory(com.machloop.fpc.cms.center.knowledge.data.SaCustomSubCategoryDO)
   */
  @Override
  public SaCustomSubCategoryDO saveSaCustomSubCategory(SaCustomSubCategoryDO subCategoryDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SA_SUBCATEGORY);
    sql.append(" (id, assign_id, name, sub_category_id, category_id, ");
    sql.append(" description, deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :assignId, :name, :subCategoryId, :categoryId, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(subCategoryDO.getId())) {
      subCategoryDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(subCategoryDO.getAssignId())) {
      subCategoryDO.setAssignId("");
    }
    subCategoryDO.setCreateTime(DateUtils.now());
    subCategoryDO.setUpdateTime(subCategoryDO.getCreateTime());
    subCategoryDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(subCategoryDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return subCategoryDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao#batchSaveCustomSubCategory(List)
   */
  @Override
  public void batchSaveCustomSubCategory(List<SaCustomSubCategoryDO> subCategoryDOs) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SA_SUBCATEGORY);
    sql.append(" (id, name, sub_category_id, category_id, ");
    sql.append(" description, deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :subCategoryId, :categoryId, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId ) ");

    subCategoryDOs.forEach(subCategoryDO -> {
      subCategoryDO.setId(IdGenerator.generateUUID());
      subCategoryDO.setCreateTime(DateUtils.now());
      subCategoryDO.setUpdateTime(subCategoryDO.getCreateTime());
      subCategoryDO.setDeleted(Constants.BOOL_NO);
      if (StringUtils.isBlank(subCategoryDO.getAssignId())) {
        subCategoryDO.setAssignId("");
      }
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(subCategoryDOs);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao#batchUpdateCategoryId(List, String, String)
   */
  @Override
  public int batchUpdateCategoryId(List<String> subCategoryIds, String categoryId,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SA_SUBCATEGORY);
    sql.append(" set category_id = :categoryId, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where sub_category_id in (:subCategoryIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("categoryId", categoryId);
    params.put("updateTime", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("subCategoryIds", subCategoryIds);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#updateSaCustomSubCategory(com.machloop.fpc.cms.center.knowledge.data.SaCustomSubCategoryDO)
   */
  @Override
  public int updateSaCustomSubCategory(SaCustomSubCategoryDO subCategoryDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SA_SUBCATEGORY);
    sql.append(" set name = :name, category_id = :categoryId, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId");
    sql.append(" where id = :id ");

    subCategoryDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(subCategoryDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#countSaCustomSubCategorys()
   */
  @Override
  public int countSaCustomSubCategorys() {
    StringBuilder sql = new StringBuilder();
    sql.append("select count(id) from ").append(TABLE_APPLIANCE_SA_SUBCATEGORY);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    return jdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#deleteSaCustomSubCategory(String, String)
   */
  @Override
  public int deleteSaCustomSubCategory(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SA_SUBCATEGORY);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    SaCustomSubCategoryDO subCategoryDO = new SaCustomSubCategoryDO();
    subCategoryDO.setDeleted(Constants.BOOL_YES);
    subCategoryDO.setDeleteTime(DateUtils.now());
    subCategoryDO.setOperatorId(operatorId);
    subCategoryDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(subCategoryDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomSubCategoryDao#batchDeleteSubCategory(List)
   */
  @Override
  public int batchDeleteSubCategory(List<String> subCategoryIds) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_SA_SUBCATEGORY);
    sql.append(" where sub_category_id in (:subCategoryIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("subCategoryIds", subCategoryIds);

    return jdbcTemplate.update(sql.toString(), params);
  }

  private StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id, name, sub_category_id, category_id, ");
    sql.append(" description, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SA_SUBCATEGORY);
    return sql;
  }
}
