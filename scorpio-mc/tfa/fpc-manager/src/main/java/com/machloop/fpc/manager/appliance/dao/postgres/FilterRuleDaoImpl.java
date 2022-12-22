package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.*;

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
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.appliance.dao.FilterRuleDao;
import com.machloop.fpc.manager.appliance.data.FilterRuleDO;

/**
 * @author chenshimiao
 *
 * create at 2022/8/9 9:18 AM,cms
 * @version 1.0
 */
@Repository
public class FilterRuleDaoImpl implements FilterRuleDao {

  private static final String TABLE_APPLIANCE_STORAGE_FILTER_RULE = "fpc_appliance_storage_filter_rule";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Page<FilterRuleDO> queryFilterRules(PageRequest page) {
    StringBuilder sql = buildSelectStatement();
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    PageUtils.appendPage(sql, page, FilterRuleDO.class);

    List<FilterRuleDO> filterRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(filterRuleList, page, total);
  }

  @Override
  public FilterRuleDO queryFilterRuleByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name");

    HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<FilterRuleDO> filterRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleDO.class));
    return CollectionUtils.isEmpty(filterRuleList) ? new FilterRuleDO() : filterRuleList.get(0);
  }

  @Override
  public FilterRuleDO queryFilterRuleByCmsFilterPolicyId(String netInCmsId) {
    StringBuilder sql = buildSelectStatement();

    sql.append(" where deleted = :deleted ");
    sql.append(" and storage_rule_in_cms_id = :storageRuleInCmsId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("storageRuleInCmsId", netInCmsId);

    List<FilterRuleDO> filterRuleDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleDO.class));

    return CollectionUtils.isEmpty(filterRuleDOList) ? new FilterRuleDO() : filterRuleDOList.get(0);
  }

  @Override
  public List<FilterRuleDO> queryFilterRule() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted order by priority DESC");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<FilterRuleDO> filterRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleDO.class));

    return CollectionUtils.isEmpty(filterRuleList) ? Lists.newArrayListWithCapacity(0)
        : filterRuleList;
  }

  @Override
  public List<String> queryFilterRule(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select storage_rule_in_cms_id from ").append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    sql.append(" where deleted = :deleted and storage_rule_in_cms_id != '' ");
    sql.append(" and update_time < :beforeTime");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public List<FilterRuleDO> queryFilterRule(Integer page, Integer pageSize) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted order by priority DESC ");
    sql.append(" limit ").append(" :sum");

    HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    Integer sum = (page + 1) * pageSize;
    params.put("sum", sum);

    List<FilterRuleDO> filterRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleDO.class));

    return CollectionUtils.isEmpty(filterRuleList) ? Lists.newArrayListWithCapacity(0)
        : filterRuleList;
  }

  @Override
  public FilterRuleDO saveOrRecoverFilterRule(FilterRuleDO filterRuleDO) {
    FilterRuleDO exist = queryFilterRuleById(
        filterRuleDO.getId() == null ? "" : filterRuleDO.getId());
    if (StringUtils.isBlank(exist == null ? "" : exist.getId())) {
      return saveFilterRule(filterRuleDO);
    } else {
      recoverAndUpdateFilterPolicy(filterRuleDO);
      return queryFilterRule(filterRuleDO.getId());
    }
  }

  @Override
  public FilterRuleDO queryFilterRule(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id");

    HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<FilterRuleDO> filterRuleDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleDO.class));
    return CollectionUtils.isEmpty(filterRuleDOList) ? new FilterRuleDO() : filterRuleDOList.get(0);
  }

  @Override
  public List<String> queryFilterRule(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    sql.append(" where deleted = :deleted");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and storage_rule_in_cms_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public int queryFilterMaxPriority() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ORDER BY priority DESC LIMIT 1");

    HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<FilterRuleDO> filterRuleDOS = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleDO.class));

    return CollectionUtils.isEmpty(filterRuleDOS) ? 0 : filterRuleDOS.get(0).getPriority();
  }

  @Override
  public FilterRuleDO queryFilterRuleByCmsFilterRuleId(String storageRuleInCmsId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and storage_rule_in_cms_id = :storageRuleInCmsId");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("storageRuleInCmsId", storageRuleInCmsId);

    List<FilterRuleDO> filterRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleDO.class));

    return CollectionUtils.isEmpty(filterRuleList) ? new FilterRuleDO() : filterRuleList.get(0);
  }

  @Override
  public int saveFilterRules(List<FilterRuleDO> filterRuleDOList, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    sql.append("" + " (id, name, tuple, storage_rule_in_cms_id, priority, state, ");
    sql.append("description, deleted, create_time, operator_id ) ");
    sql.append(
        " values (:id, :name, :tuple, :storageRuleInCmsId, :priority, :state, :description, ");
    sql.append(":deleted, :createTime, :operatorId )");

    for (FilterRuleDO filterRuleDO : filterRuleDOList) {
      filterRuleDO.setId(IdGenerator.generateUUID());
      filterRuleDO.setCreateTime(DateUtils.now());
      filterRuleDO.setUpdateTime(filterRuleDO.getUpdateTime());
      filterRuleDO.setDeleted(Constants.BOOL_NO);
      filterRuleDO.setOperatorId(operatorId);
      if (StringUtils.isBlank(filterRuleDO.getStorageRuleInCmsId())) {
        filterRuleDO.setStorageRuleInCmsId("");
      }
      if (StringUtils.isBlank(filterRuleDO.getDescription())) {
        filterRuleDO.setDescription("");
      }
    }

    SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(filterRuleDOList);
    return Arrays.stream(jdbcTemplate.batchUpdate(sql.toString(), batch)).sum();
  }

  @Override
  public int updateFilterRule(String id, Integer priority, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    sql.append(" set priority = :priority, update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id");

    FilterRuleDO filterRuleDO = new FilterRuleDO();
    filterRuleDO.setOperatorId(operatorId);
    filterRuleDO.setPriority(priority);
    filterRuleDO.setId(id);
    filterRuleDO.setUpdateTime(DateUtils.now());

    BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(
        filterRuleDO);
    return jdbcTemplate.update(sql.toString(), parameterSource);
  }

  @Override
  public int updateFilterRule(FilterRuleDO filterRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    sql.append(" set name = :name, tuple = :tuple, description = :description, state = :state, ");
    sql.append("  update_time = :updateTime, operator_id = :operatorId, priority = :priority ");
    sql.append(" where id = :id");

    filterRuleDO.setUpdateTime(DateUtils.now());

    BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(
        filterRuleDO);
    return jdbcTemplate.update(sql.toString(), parameterSource);
  }

  @Override
  public int updateFilterRuleState(List<String> idList, String state, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    sql.append(" set state = :state, update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id in (:id)");

    List<
        FilterRuleDO> filterRuleDOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (String id : idList) {
      FilterRuleDO filterRuleDO = new FilterRuleDO();
      filterRuleDO.setState(state);
      filterRuleDO.setUpdateTime(DateUtils.now());
      filterRuleDO.setOperatorId(operatorId);
      filterRuleDO.setId(id);
      filterRuleDOList.add(filterRuleDO);
    }

    SqlParameterSource[] parameterSources = SqlParameterSourceUtils.createBatch(filterRuleDOList);
    return jdbcTemplate.batchUpdate(sql.toString(), parameterSources)[0];
  }

  private int recoverAndUpdateFilterPolicy(FilterRuleDO filterRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    sql.append(
        " set name = :name, tuple = :tuple, priority = :priority, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId, deleted = :deleted ");
    sql.append(" where id = :id");

    filterRuleDO.setUpdateTime(DateUtils.now());
    filterRuleDO.setDeleted(Constants.BOOL_NO);
    SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(filterRuleDO);
    return jdbcTemplate.update(sql.toString(), sqlParameterSource);
  }

  private FilterRuleDO saveFilterRule(FilterRuleDO filterRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    sql.append("" + " (id, name, tuple, storage_rule_in_cms_id, priority, state, ");
    sql.append("description, deleted, create_time, update_time, operator_id ) ");
    sql.append(
        " values (:id, :name, :tuple, :storageRuleInCmsId, :priority, :state, :description, ");
    sql.append(":deleted, :createTime, :updateTime, :operatorId )");

    if (StringUtils.isBlank(filterRuleDO.getId())) {
      filterRuleDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(filterRuleDO.getStorageRuleInCmsId())) {
      filterRuleDO.setStorageRuleInCmsId("");
    }
    filterRuleDO.setState(Constants.BOOL_NO);
    filterRuleDO.setCreateTime(DateUtils.now());
    filterRuleDO.setUpdateTime(filterRuleDO.getCreateTime());
    filterRuleDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(filterRuleDO);
    jdbcTemplate.update(sql.toString(), parameterSource);
    return filterRuleDO;
  }

  @Override
  public int deleteNetworkRule(List<String> idList, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = (:id)");

    List<FilterRuleDO> paramList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (String id : idList) {
      FilterRuleDO filterRuleDO = new FilterRuleDO();
      filterRuleDO.setDeleted(Constants.BOOL_YES);
      filterRuleDO.setDeleteTime(DateUtils.now());
      filterRuleDO.setOperatorId(operatorId);
      filterRuleDO.setId(id);
      paramList.add(filterRuleDO);
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(paramList);
    return jdbcTemplate.batchUpdate(sql.toString(), batchSource)[0];
  }

  private FilterRuleDO queryFilterRuleById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id");

    HashMap<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);
    List<FilterRuleDO> filterRuleDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleDO.class));

    return CollectionUtils.isEmpty(filterRuleDOList) ? new FilterRuleDO() : filterRuleDOList.get(0);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, tuple, storage_rule_in_cms_id, ");
    sql.append("state, description, priority, deleted, create_time, ");
    sql.append("update_time, delete_time, operator_id ");
    sql.append("from ").append(TABLE_APPLIANCE_STORAGE_FILTER_RULE);
    return sql;
  }
}
