package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
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
import com.machloop.fpc.manager.appliance.dao.ForwardPolicyDao;
import com.machloop.fpc.manager.appliance.data.ForwardPolicyDO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/11 23:03,IntelliJ IDEA
 *
 */
@Repository
public class ForwardPolicyDaoImpl implements ForwardPolicyDao {

  private static final String TABLE_APPLIANCE_FORWARD_POLICY = "fpc_appliance_forward_policy";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Page<ForwardPolicyDO> queryForwardPolicies(PageRequest page) {
    StringBuilder sql = buildSelectStatement();
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    PageUtils.appendPage(sql, page, ForwardPolicyDO.class);

    List<ForwardPolicyDO> forwardPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ForwardPolicyDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_FORWARD_POLICY);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(forwardPolicyList, page, total);
  }

  @Override
  public ForwardPolicyDO queryForwardPolicy(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<ForwardPolicyDO> forwardPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ForwardPolicyDO.class));
    return CollectionUtils.isEmpty(forwardPolicyList) ? new ForwardPolicyDO()
        : forwardPolicyList.get(0);

  }

  @Override
  public List<ForwardPolicyDO> queryForwardPolicys() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<ForwardPolicyDO> forwardPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ForwardPolicyDO.class));
    return CollectionUtils.isEmpty(forwardPolicyList) ? Lists.newArrayListWithCapacity(0)
        : forwardPolicyList;
  }

  @Override
  public List<ForwardPolicyDO> queryForwardPolicyByRuleId(String ruleId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and rule_id = :ruleId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("ruleId", ruleId);

    List<ForwardPolicyDO> forwardPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ForwardPolicyDO.class));
    return CollectionUtils.isEmpty(forwardPolicyList) ? new ArrayList<ForwardPolicyDO>()
        : forwardPolicyList;

  }


  @Override
  public ForwardPolicyDO queryForwardPolicyByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<ForwardPolicyDO> forwardPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ForwardPolicyDO.class));
    return CollectionUtils.isEmpty(forwardPolicyList) ? new ForwardPolicyDO()
        : forwardPolicyList.get(0);
  }

  @Override
  public ForwardPolicyDO saveForwardPolicy(ForwardPolicyDO forwardPolicyDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_FORWARD_POLICY);
    sql.append(" (id, name, rule_id, netif_name, ip_tunnel, ");
    sql.append(" load_balance, forward_policy_in_cms_id, description, state, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :ruleId, :netifName, :ipTunnel, ");
    sql.append(" :loadBalance, :forwardPolicyInCmsId, :description, :state, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(forwardPolicyDO.getId())) {
      forwardPolicyDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(forwardPolicyDO.getForwardPolicyInCmsId())) {
      forwardPolicyDO.setForwardPolicyInCmsId("");
    }
    forwardPolicyDO.setCreateTime(DateUtils.now());
    forwardPolicyDO.setUpdateTime(forwardPolicyDO.getCreateTime());
    forwardPolicyDO.setDeleted(Constants.BOOL_NO);
    forwardPolicyDO.setState(Constants.BOOL_YES);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(forwardPolicyDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return forwardPolicyDO;
  }

  @Override
  public int updateForwardPolicy(ForwardPolicyDO forwardPolicyDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_FORWARD_POLICY);
    sql.append(" set name = :name, rule_id = :ruleId, ");
    sql.append(" netif_name = :netifName, ip_tunnel = :ipTunnel, ");
    sql.append(" load_balance = :loadBalance, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    forwardPolicyDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(forwardPolicyDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public int deleteForwardPolicy(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_FORWARD_POLICY);
    sql.append(" set deleted = :deleted, deleted_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ForwardPolicyDO forwardPolicyDO = new ForwardPolicyDO();
    forwardPolicyDO.setDeleted(Constants.BOOL_YES);
    forwardPolicyDO.setDeleteTime(DateUtils.now());
    forwardPolicyDO.setOperatorId(operatorId);
    forwardPolicyDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(forwardPolicyDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public int deleteForwardPolicyByRuleId(String ruleId, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_FORWARD_POLICY);
    sql.append(" set deleted = :deleted, deleted_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where rule_id = :ruleId ");

    ForwardPolicyDO forwardPolicyDO = new ForwardPolicyDO();
    forwardPolicyDO.setDeleted(Constants.BOOL_YES);
    forwardPolicyDO.setDeleteTime(DateUtils.now());
    forwardPolicyDO.setOperatorId(operatorId);
    forwardPolicyDO.setRuleId(ruleId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(forwardPolicyDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }


  @Override
  public int changeForwardPolicy(String id, String state, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_FORWARD_POLICY);
    sql.append(" set state = :state, update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ForwardPolicyDO forwardPolicyDO = new ForwardPolicyDO();

    forwardPolicyDO.setOperatorId(operatorId);
    forwardPolicyDO.setId(id);
    forwardPolicyDO.setState(state);
    forwardPolicyDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(forwardPolicyDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }


  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, rule_id, netif_name, ip_tunnel, ");
    sql.append(" load_balance, forward_policy_in_cms_id, state, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_FORWARD_POLICY);

    return sql;
  }
}
