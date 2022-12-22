package com.machloop.fpc.cms.center.knowledge.dao.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.knowledge.dao.SaHierarchyDao;
import com.machloop.fpc.cms.center.knowledge.data.SaHierarchyDO;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author guosk
 *
 * create at 2021年1月29日, fpc-manager
 */
@Repository
public class SaHierarchyDaoImpl implements SaHierarchyDao {

  private static final String TABLE_APPLIANCE_SA_HIERARCHY = "fpccms_appliance_sa_hierarchy";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaHierarchyDao#querySaHierarchys(String, String, String)
   */
  @Override
  public List<SaHierarchyDO> querySaHierarchys(String type, String categoryId,
      String subCategoryId) {
    StringBuilder sql = buildSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where type = :type ");
    params.put("type", type);

    if (StringUtils.isNotBlank(categoryId)) {
      whereSql.append(" and category_id = :categoryId ");
      params.put("categoryId", categoryId);
    }
    if (StringUtils.isNotBlank(subCategoryId)) {
      whereSql.append(" and sub_category_id = :subCategoryId ");
      params.put("subCategoryId", subCategoryId);
    }
    sql.append(whereSql);

    List<SaHierarchyDO> saHierarchyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaHierarchyDO.class));
    return CollectionUtils.isEmpty(saHierarchyList) ? Lists.newArrayListWithCapacity(0)
        : saHierarchyList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaHierarchyDao#querySaHierarchyBySubCategoryIds(List)
   */
  @Override
  public List<SaHierarchyDO> querySaHierarchyBySubCategoryIds(List<String> subCategoryIds) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where sub_category_id in (:subCategoryIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("subCategoryIds", subCategoryIds);

    List<SaHierarchyDO> saHierarchyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaHierarchyDO.class));
    return CollectionUtils.isEmpty(saHierarchyList) ? Lists.newArrayListWithCapacity(0)
        : saHierarchyList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaHierarchyDao#querySaHierarchyByApplicationIds(List)
   */
  @Override
  public List<SaHierarchyDO> querySaHierarchyByApplicationIds(List<String> applicationIds) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where application_id in (:applicationIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("applicationIds", applicationIds);

    List<SaHierarchyDO> saHierarchyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SaHierarchyDO.class));
    return CollectionUtils.isEmpty(saHierarchyList) ? Lists.newArrayListWithCapacity(0)
        : saHierarchyList;
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaHierarchyDao#batchSaveSaHierarchy(List)
   */
  @Override
  public void batchSaveSaHierarchy(List<SaHierarchyDO> saHierarchys) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SA_HIERARCHY);
    sql.append(" (id, type, category_id, sub_category_id, application_id, ");
    sql.append(" create_time, operator_id ) ");
    sql.append(" values (:id, :type, :categoryId, :subCategoryId, :applicationId, ");
    sql.append(" :createTime, :operatorId ) ");

    saHierarchys.forEach(saHierarchyDO -> {
      saHierarchyDO.setId(IdGenerator.generateUUID());
      saHierarchyDO.setCreateTime(DateUtils.now());
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(saHierarchys);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaHierarchyDao#batchUpdateAppsBySubCategory(List, String, String)
   */
  @Override
  public int batchUpdateAppsBySubCategory(List<String> subCategoryIds, String categoryId,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SA_HIERARCHY);
    sql.append(" set category_id = :categoryId, ");
    sql.append(" operator_id = :operatorId ");
    sql.append(" where type = :type and sub_category_id in (:subCategoryIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("categoryId", categoryId);
    params.put("operatorId", operatorId);
    params.put("type", String.valueOf(FpcCmsConstants.METRIC_TYPE_APPLICATION_APP));
    params.put("subCategoryIds", subCategoryIds);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaHierarchyDao#batchDeleteByCategoryId(String)
   */
  @Override
  public int batchDeleteByCategoryId(String categoryId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_SA_HIERARCHY);
    sql.append(" where category_id = :categoryId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("categoryId", categoryId);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaHierarchyDao#batchDeleteBySubCategoryIds(String, List)
   */
  @Override
  public int batchDeleteBySubCategoryIds(String type, List<String> subCategoryIds) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_SA_HIERARCHY);
    sql.append(" where type = :type and sub_category_id in (:subCategoryIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("type", type);
    params.put("subCategoryIds", subCategoryIds);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.knowledge.dao.SaHierarchyDao#batchDeleteByApplicationIds(String, List)
   */
  @Override
  public int batchDeleteByApplicationIds(String type, List<String> applicationIds) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_SA_HIERARCHY);
    sql.append(" where type = :type and application_id in (:applicationIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("type", type);
    params.put("applicationIds", applicationIds);

    return jdbcTemplate.update(sql.toString(), params);
  }

  private StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, type, category_id, sub_category_id, ");
    sql.append(" application_id, create_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SA_HIERARCHY);
    return sql;
  }

}
