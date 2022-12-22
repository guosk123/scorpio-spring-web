package com.machloop.fpc.cms.center.appliance.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao;
import com.machloop.fpc.cms.center.appliance.data.AssignmentActionDO;

/**
 * @author liyongjun
 *
 * create at 2019年11月8日, fpc-cms-center
 */
@Repository
public class AssignmentActionDaoImpl implements AssignmentActionDao {

  private static final String TABLE_ASSIGNMENT_ACTION = "fpccms_appliance_assignment_action";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#queryAssignmentActions(java.util.List, java.lang.String, java.lang.String)
   */
  @Override
  public List<AssignmentActionDO> queryAssignmentActions(List<String> fpcSerialNumbers,
      String assignmentType, String state) {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where 1=1 ");

    if (CollectionUtils.isNotEmpty(fpcSerialNumbers)) {
      whereSql.append(" and fpc_serial_number in (:fpcSerialNumbers) ");
      params.put("fpcSerialNumbers", fpcSerialNumbers);
    }

    if (StringUtils.isNotBlank(assignmentType)) {
      whereSql.append(" and type = :type ");
      params.put("type", assignmentType);
    }

    if (StringUtils.isNotBlank(state)) {
      whereSql.append(" and state = :state ");
      params.put("state", state);
    }

    sql.append(whereSql);

    sql.append(" order by assignment_time desc ");

    List<AssignmentActionDO> assignmentActionList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentActionDO.class));

    return CollectionUtils.isNotEmpty(assignmentActionList) ? assignmentActionList
        : Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#queryAssignmentActionsByMessageIds(java.util.List)
   */
  @Override
  public List<AssignmentActionDO> queryAssignmentActionsByMessageIds(List<String> messageIds) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where message_id in ( :messageIds ) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("messageIds", messageIds);

    List<AssignmentActionDO> assignmentActionList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentActionDO.class));

    return CollectionUtils.isNotEmpty(assignmentActionList) ? assignmentActionList
        : Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#queryAssignmentActionsByTaskAction(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<AssignmentActionDO> queryAssignmentActionsByTaskAction(String taskPolicyId,
      String type, String action) {
    StringBuilder sql = buildSelectStatement();
    Map<String, String> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where 1=1 ");

    if (StringUtils.isNotBlank(taskPolicyId)) {
      sql.append(" and task_policy_id = :taskPolicyId ");
      params.put("taskPolicyId", taskPolicyId);
    }
    if (StringUtils.isNotBlank(type)) {
      sql.append(" and type = :type ");
      params.put("type", type);
    }
    if (StringUtils.isNotBlank(action)) {
      sql.append(" and action = :action ");
      params.put("action", action);
    }

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentActionDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#queryAssignmentActionsByAssignmentId(java.lang.String)
   */
  @Override
  public List<AssignmentActionDO> queryAssignmentActionsByAssignmentId(String assignmentId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where assignment_id = :assignmentId ");

    Map<String, String> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("assignmentId", assignmentId);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentActionDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#batchSaveAssignmentActions(java.util.List)
   */
  @Override
  public void batchSaveAssignmentActions(List<AssignmentActionDO> assignmentActionList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ASSIGNMENT_ACTION);
    sql.append(" (id, fpc_serial_number, message_id, assignment_id, task_policy_id, type, ");
    sql.append(" action, state, assignment_time) ");
    sql.append(" values(:id, :fpcSerialNumber, :messageId, :assignmentId, :taskPolicyId, :type, ");
    sql.append(" :action, :state, :assignmentTime) ");

    for (AssignmentActionDO assignmentAction : assignmentActionList) {
      assignmentAction.setId(IdGenerator.generateUUID());
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(assignmentActionList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#updateAssignmentActions(java.util.List, java.lang.String)
   */
  @Override
  public void updateAssignmentActions(List<String> taskPolicyIds, String state) {

    if (CollectionUtils.isEmpty(taskPolicyIds)) {
      return;
    }

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_ACTION);
    sql.append(" set state = :state ");
    sql.append(" where task_policy_id in (:taskPolicyIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("state", state);
    params.put("taskPolicyIds", taskPolicyIds);

    jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#updateAssignmentActions(java.util.List)
   */
  @Override
  public void updateAssignmentActions(List<Map<String, String>> assignmentActionList) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_ACTION);
    sql.append(" set state = :state ");
    sql.append(" where message_id = :messageId ");

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(assignmentActionList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#updateAssignmentAction(com.machloop.fpc.cms.center.appliance.data.AssignmentActionDO)
   */
  @Override
  public void updateAssignmentAction(String messageId, String state) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_ACTION);
    sql.append(" set state = :state ");
    sql.append(" where message_id = :messageId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("state", state);
    params.put("messageId", messageId);

    jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#batchCancelAssignment(java.util.List)
   */
  @Override
  public void batchCancelAssignment(List<String> ids) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_ACTION);
    sql.append(" set state = :state ");
    sql.append(" where id in (:ids) and state = :waitState ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("state", CenterConstants.TASK_ASSIGNMENT_STATE_CANCEL);
    params.put("ids", ids);
    params.put("waitState", CenterConstants.TASK_ASSIGNMENT_STATE_WAIT);

    jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#updateAssignmentStateByAssignmentId(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void updateAssignmentStateByAssignmentId(String assignmentId, String originalState,
      String newState) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_ACTION);
    sql.append(" set state = :newState ");
    sql.append(" where assignment_id = :assignmentId and state = :originalState");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("newState", newState);
    params.put("assignmentId", assignmentId);
    params.put("originalState", originalState);

    jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao#updateAssignmentStateByTaskPolicyId(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void updateAssignmentStateByTaskPolicyId(String taskPolicyId, String type,
      String originalState, String newState) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_ACTION);
    sql.append(" set state = :newState ");
    sql.append(" where task_policy_id = :taskPolicyId and type = :type and state = :originalState");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("newState", newState);
    params.put("taskPolicyId", taskPolicyId);
    params.put("type", type);
    params.put("originalState", originalState);

    jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, fpc_serial_number, message_id, assignment_id, task_policy_id, ");
    sql.append(" type, action, state, assignment_time ");
    sql.append(" from ").append(TABLE_ASSIGNMENT_ACTION);
    return sql;
  }
}
