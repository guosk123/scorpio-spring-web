package com.machloop.fpc.cms.center.knowledge.dao.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao;
import com.machloop.fpc.cms.center.knowledge.data.SaCustomApplicationDO;
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
public class SaCustomApplicationDaoImpl implements SaCustomApplicationDao {

  private static final String TABLE_APPLIANCE_SA_APPLICATION = "fpccms_appliance_sa_application";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#querySaCustomApps()
   */
  @Override
  public List<SaCustomApplicationDO> querySaCustomApps() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<SaCustomApplicationDO> customAppDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomApplicationDO.class));
    return CollectionUtils.isEmpty(customAppDOList) ? Lists.newArrayListWithCapacity(0)
        : customAppDOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#querySaCustomAppIds(java.util.Date)
   */
  @Override
  public List<String> querySaCustomAppIds() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_SA_APPLICATION);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#querySaCustomAppBySubCategoryId(String)
   */
  @Override
  public List<SaCustomApplicationDO> querySaCustomAppBySubCategoryId(String subCategoryId) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted and sub_category_id = :subCategoryId ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("subCategoryId", subCategoryId);

    List<SaCustomApplicationDO> customAppDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomApplicationDO.class));
    return CollectionUtils.isEmpty(customAppDOList) ? Lists.newArrayListWithCapacity(0)
        : customAppDOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#querySaCustomAppByAppIds(List)
   */
  @Override
  public List<SaCustomApplicationDO> querySaCustomAppByAppIds(List<String> appIds) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted and application_id in (:appIds) ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("appIds", appIds);

    List<SaCustomApplicationDO> customAppDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomApplicationDO.class));
    return CollectionUtils.isEmpty(customAppDOList) ? Lists.newArrayListWithCapacity(0)
        : customAppDOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#querySaCustomApp(String)
   */
  @Override
  public SaCustomApplicationDO querySaCustomApp(String id) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and id = :id ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<SaCustomApplicationDO> customRulesList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomApplicationDO.class));
    return CollectionUtils.isEmpty(customRulesList) ? new SaCustomApplicationDO()
        : customRulesList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#querySaCustomAppByName(String)
   */
  @Override
  public SaCustomApplicationDO querySaCustomAppByName(String name) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and name = :name ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<SaCustomApplicationDO> customRulesList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomApplicationDO.class));
    return CollectionUtils.isEmpty(customRulesList) ? new SaCustomApplicationDO()
        : customRulesList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#queryCustomAppByAssignId(java.lang.String)
   */
  @Override
  public SaCustomApplicationDO queryCustomAppByAssignId(String assignId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and assign_id = :assignId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignId", assignId);

    List<SaCustomApplicationDO> customAppDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomApplicationDO.class));

    return CollectionUtils.isEmpty(customAppDOList) ? new SaCustomApplicationDO()
        : customAppDOList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#queryAssignSaCustomAppIds(java.util.Date)
   */
  @Override
  public List<SaCustomApplicationDO> queryAssignSaCustomAppIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_SA_APPLICATION);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<SaCustomApplicationDO> customAppDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaCustomApplicationDO.class));

    return CollectionUtils.isEmpty(customAppDOList) ? Lists.newArrayListWithCapacity(0)
        : customAppDOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#saveSaCustomApp(com.machloop.fpc.cms.center.knowledge.data.SaCustomApplicationDO)
   */
  @Override
  public SaCustomApplicationDO saveSaCustomApp(SaCustomApplicationDO applicationDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SA_APPLICATION);
    sql.append(" (id, assign_id, name, application_id, category_id, ");
    sql.append(" sub_category_id, l7_protocol_id, rule, ");
    sql.append(" description, deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :assignId, :name, :applicationId, :categoryId,  ");
    sql.append(" :subCategoryId, :l7ProtocolId, :rule, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(applicationDO.getId())) {
      applicationDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(applicationDO.getAssignId())) {
      applicationDO.setAssignId("");
    }
    applicationDO.setCreateTime(DateUtils.now());
    applicationDO.setUpdateTime(applicationDO.getCreateTime());
    applicationDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(applicationDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return applicationDO;
  }

  /** 
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#batchSaveCustomApp(List)
   */
  @Override
  public void batchSaveCustomApp(List<SaCustomApplicationDO> applicationDOs) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SA_APPLICATION);
    sql.append(" (id, name, application_id, category_id, ");
    sql.append(" sub_category_id, l7_protocol_id, rule, ");
    sql.append(" description, deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :applicationId, :categoryId, ");
    sql.append(" :subCategoryId, :l7ProtocolId, :rule, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId ) ");

    applicationDOs.forEach(applicationDO -> {
      applicationDO.setId(IdGenerator.generateUUID());
      applicationDO.setCreateTime(DateUtils.now());
      applicationDO.setUpdateTime(applicationDO.getCreateTime());
      applicationDO.setDeleted(Constants.BOOL_NO);
      if (StringUtils.isBlank(applicationDO.getAssignId())) {
        applicationDO.setAssignId("");
      }
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(applicationDOs);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#batchUpdateApps(List, String, String, String)
   */
  @Override
  public int batchUpdateApps(List<String> appIds, String categoryId, String subCategoryId,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SA_APPLICATION);
    sql.append(" set category_id = :categoryId, sub_category_id = :subCategoryId, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where application_id in (:appIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("categoryId", categoryId);
    params.put("subCategoryId", subCategoryId);
    params.put("updateTime", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("appIds", appIds);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#batchUpdateAppsBySubCategory(List, String, String)
   */
  @Override
  public int batchUpdateAppsBySubCategory(List<String> subCategoryIds, String categoryId,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SA_APPLICATION);
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
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#updateSaCustomApp(com.machloop.fpc.cms.center.knowledge.data.SaCustomApplicationDO)
   */
  @Override
  public int updateSaCustomApp(SaCustomApplicationDO applicationDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SA_APPLICATION);
    sql.append(" set name = :name, category_id = :categoryId, ");
    sql.append(" sub_category_id = :subCategoryId, l7_protocol_id = :l7ProtocolId, rule = :rule, ");
    sql.append(" description = :description, update_time = :updateTime, ");
    sql.append(" operator_id = :operatorId ");
    sql.append(" where id = :id ");

    applicationDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(applicationDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#countSaCustomApps()
   */
  @Override
  public int countSaCustomApps() {
    StringBuilder sql = new StringBuilder();
    sql.append("select count(id) from ").append(TABLE_APPLIANCE_SA_APPLICATION);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    int total = jdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
    return total;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#deleteSaCustomApp(String, String)
   */
  @Override
  public int deleteSaCustomApp(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SA_APPLICATION);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    SaCustomApplicationDO applicationDO = new SaCustomApplicationDO();
    applicationDO.setDeleted(Constants.BOOL_YES);
    applicationDO.setDeleteTime(DateUtils.now());
    applicationDO.setOperatorId(operatorId);
    applicationDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(applicationDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaCustomApplicationDao#batchDeleteCustomApp(List)
   */
  @Override
  public int batchDeleteCustomApp(List<String> appIds) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_SA_APPLICATION);
    sql.append(" where application_id in (:appIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("appIds", appIds);

    return jdbcTemplate.update(sql.toString(), params);
  }

  private StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id, name, application_id, category_id, ");
    sql.append(" sub_category_id, l7_protocol_id, rule, description, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SA_APPLICATION);
    return sql;
  }
}
