package com.machloop.fpc.cms.center.appliance.dao.postgres;

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
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.SendPolicyDao;
import com.machloop.fpc.cms.center.appliance.data.SendPolicyDO;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
@Repository
public class SendPolicyDaoImpl implements SendPolicyDao {


  private static final String TABLE_APPLIANCE_SEND_POLICY = "fpccms_appliance_send_policy";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;


  @Override
  public List<SendPolicyDO> querySendPolicies() {

    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendPolicyDO.class));
  }

  @Override
  public List<SendPolicyDO> querySendPoliciesByExternelReceiverId(String id) {
    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted and external_receiver_id = :externalReceiverId ");
    sql.append(whereSql);

    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("externalReceiverId", id);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendPolicyDO.class));
  }

  @Override
  public List<SendPolicyDO> querySendPoliciesBySendRuleId(String id) {
    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted and send_rule_id = :sendRuleId ");
    sql.append(whereSql);

    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("sendRuleId", id);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendPolicyDO.class));
  }

  @Override
  public List<String> querySendPoliciesIds(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_SEND_POLICY);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and assign_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public SendPolicyDO querySendPolicyByAssignId(String assignId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and assign_id = :assignId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignId", assignId);

    List<SendPolicyDO> sendPolicyDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendPolicyDO.class));

    return CollectionUtils.isEmpty(sendPolicyDOList) ? new SendPolicyDO() : sendPolicyDOList.get(0);
  }

  @Override
  public List<SendPolicyDO> queryAssignSendPolicyIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_SEND_POLICY);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<SendPolicyDO> sendPolicyDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendPolicyDO.class));

    return CollectionUtils.isEmpty(sendPolicyDOList) ? Lists.newArrayListWithCapacity(0)
        : sendPolicyDOList;
  }

  @Override
  public SendPolicyDO querySendPolicy(String id) {

    StringBuilder sql = buildSelectStatement();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where id = :id and deleted = :deleted ");
    params.put("id", id);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    sql.append(whereSql);

    List<SendPolicyDO> sendPolicyDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendPolicyDO.class));

    return CollectionUtils.isEmpty(sendPolicyDOList) ? new SendPolicyDO() : sendPolicyDOList.get(0);
  }

  @Override
  public List<SendPolicyDO> querySendPoliciesStateOn() {

    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted and state = :state ");
    sql.append(whereSql);

    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("state", Constants.BOOL_YES);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendPolicyDO.class));
  }

  @Override
  public SendPolicyDO querySendPolicyByName(String name) {

    StringBuilder sql = buildSelectStatement();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where name = :name and deleted = :deleted ");
    params.put("name", name);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    sql.append(whereSql);

    List<SendPolicyDO> sendPolicyDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendPolicyDO.class));

    return CollectionUtils.isEmpty(sendPolicyDOList) ? new SendPolicyDO() : sendPolicyDOList.get(0);
  }

  @Override
  public void saveSendPolicy(SendPolicyDO sendPolicyDO) {

    StringBuilder sql = new StringBuilder();
    sql.append(" insert into ").append(TABLE_APPLIANCE_SEND_POLICY);
    sql.append(" (id, name, external_receiver_id, send_rule_id, assign_id, state, ");
    sql.append(" update_time, create_time, operator_id) ");
    sql.append(" values (:id, :name, :externalReceiverId, :sendRuleId, :assignId, :state, ");
    sql.append(" :updateTime, :createTime, :operatorId) ");

    if (StringUtils.isBlank(sendPolicyDO.getId())) {
      sendPolicyDO.setId(IdGenerator.generateUUID());
    }

    sendPolicyDO.setCreateTime(DateUtils.now());
    sendPolicyDO.setUpdateTime(sendPolicyDO.getCreateTime());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sendPolicyDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public void updateSendPolicy(SendPolicyDO sendPolicyDO) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SEND_POLICY);
    sql.append(
        " set name = :name, external_receiver_id = :externalReceiverId, send_rule_id = :sendRuleId, state = :state, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    sendPolicyDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sendPolicyDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public void updateSendPolicyTimeByExternalReceiverId(String externalReceiverId,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SEND_POLICY);
    sql.append(" set update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where deleted = :deleted and external_receiver_id = :externalReceiverId ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("externalReceiverId", externalReceiverId);
    params.put("updateTime", DateUtils.now());
    params.put("operatorId", operatorId);

    jdbcTemplate.update(sql.toString(), params);

  }

  @Override
  public void updateSendPolicyTimeBySendRuleId(String sendRuleId, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SEND_POLICY);
    sql.append(" set update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where deleted = :deleted and send_rule_id = :sendRuleId ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("sendRuleId", sendRuleId);
    params.put("updateTime", DateUtils.now());
    params.put("operatorId", operatorId);

    jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public void deleteSendPolicy(String id, String operatorId) {


    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SEND_POLICY);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    SendPolicyDO sendPolicyDO = new SendPolicyDO();
    sendPolicyDO.setDeleted(Constants.BOOL_YES);
    sendPolicyDO.setDeleteTime(DateUtils.now());
    sendPolicyDO.setOperatorId(operatorId);
    sendPolicyDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sendPolicyDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public void changeSendPolicyState(String id, String state, String operatorId) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SEND_POLICY);
    sql.append(" set state = :state, update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    SendPolicyDO sendPolicyDO = new SendPolicyDO();

    sendPolicyDO.setOperatorId(operatorId);
    sendPolicyDO.setId(id);
    sendPolicyDO.setState(state);
    sendPolicyDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sendPolicyDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  private StringBuilder buildSelectStatement() {

    StringBuilder sql = new StringBuilder();
    sql.append(" select id, name, external_receiver_id, send_rule_id, assign_id, state, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SEND_POLICY);

    return sql;
  }
}
