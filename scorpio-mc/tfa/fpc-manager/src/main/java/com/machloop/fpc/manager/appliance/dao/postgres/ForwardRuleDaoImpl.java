package com.machloop.fpc.manager.appliance.dao.postgres;

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
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.appliance.dao.ForwardRuleDao;
import com.machloop.fpc.manager.appliance.data.ForwardRuleDO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/7 17:21,IntelliJ IDEA
 *
 */
@Repository
public class ForwardRuleDaoImpl implements ForwardRuleDao {

  private static final String TABLE_APPLIANCE_FORWARD_RULE = "fpc_appliance_forward_rule";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Page<ForwardRuleDO> queryForwardRules(Pageable page) {
    StringBuilder sql = buildSelectStatement();
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    PageUtils.appendPage(sql, page, ForwardRuleDO.class);

    List<ForwardRuleDO> forwardRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ForwardRuleDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_FORWARD_RULE);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(forwardRuleList, page, total);

  }

  @Override
  public ForwardRuleDO queryForwardRule(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<ForwardRuleDO> forwardRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ForwardRuleDO.class));
    return CollectionUtils.isEmpty(forwardRuleList) ? new ForwardRuleDO() : forwardRuleList.get(0);
  }

  @Override
  public ForwardRuleDO queryForwardRuleByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<ForwardRuleDO> forwardRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ForwardRuleDO.class));
    return CollectionUtils.isEmpty(forwardRuleList) ? new ForwardRuleDO() : forwardRuleList.get(0);
  }

  @Override
  public ForwardRuleDO saveForwardRule(ForwardRuleDO forwardRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_FORWARD_RULE);
    sql.append(" (id, name, default_action, except_bpf, except_tuple, ");
    sql.append(" forward_rule_in_cms_id, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :defaultAction, :exceptBpf, :exceptTuple, ");
    sql.append(" :forwardRuleInCmsId, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(forwardRuleDO.getId())) {
      forwardRuleDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(forwardRuleDO.getForwardRuleInCmsId())) {
      forwardRuleDO.setForwardRuleInCmsId("");
    }
    forwardRuleDO.setCreateTime(DateUtils.now());
    forwardRuleDO.setUpdateTime(forwardRuleDO.getCreateTime());
    forwardRuleDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(forwardRuleDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return forwardRuleDO;
  }

  @Override
  public int updateForwardRule(ForwardRuleDO forwardRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_FORWARD_RULE);
    sql.append(" set name = :name, default_action = :defaultAction, ");
    sql.append(" except_bpf = :exceptBpf, except_tuple = :exceptTuple, ");
    sql.append(" description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    forwardRuleDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(forwardRuleDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public int deleteForwardRule(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_FORWARD_RULE);
    sql.append(" set deleted = :deleted, deleted_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ForwardRuleDO forwardRuleDO = new ForwardRuleDO();
    forwardRuleDO.setDeleted(Constants.BOOL_YES);
    forwardRuleDO.setDeleteTime(DateUtils.now());
    forwardRuleDO.setOperatorId(operatorId);
    forwardRuleDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(forwardRuleDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public List<ForwardRuleDO> queryForwardRules() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" order by create_time asc ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<ForwardRuleDO> forwardRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ForwardRuleDO.class));

    return CollectionUtils.isEmpty(forwardRuleList) ? Lists.newArrayListWithCapacity(0)
        : forwardRuleList;
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, default_action, except_bpf, except_tuple, ");
    sql.append(" forward_rule_in_cms_id, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_FORWARD_RULE);
    return sql;
  }


}

