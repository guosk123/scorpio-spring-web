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
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao;
import com.machloop.fpc.cms.center.appliance.data.AssignmentTaskRecordDO;

@Repository
public class AssignmentTaskRecordDaoImpl implements AssignmentTaskRecordDao {

  private static final String TABLE_ASSIGNMENT_TASK_RECORD = "fpccms_appliance_assignment_task_record";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#queryAssignmentTaskRecords(com.machloop.alpha.common.base.page.Pageable, java.lang.String)
   */
  @Override
  public Page<AssignmentTaskRecordDO> queryAssignmentTaskRecords(Pageable page, String taskId) {
    StringBuilder sql = new StringBuilder();
    sql.append("select atr.id, atr.task_id, atr.fpc_task_id, atr.fpc_serial_number, ");
    sql.append(" atr.message_id,  atr.assignment_state, atr.execution_state, ");
    sql.append(" atr.execution_trace, atr.execution_start_time, atr.execution_end_time, ");
    sql.append(" atr.execution_progress, atr.execution_cache_path, atr.pcap_file_url, ");
    sql.append(" atr.operator_id, atr.assignment_time ");
    sql.append(" from ").append(TABLE_ASSIGNMENT_TASK_RECORD);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(
        " atr left join fpccms_central_fpc fpc on atr.fpc_serial_number = fpc.serial_number ");
    whereSql.append(" where fpc.deleted = :deleted ");
    whereSql.append(" and atr.task_id = :taskId ");
    whereSql.append(" and atr.assignment_state not in (:stateList) ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("taskId", taskId);
    params.put("stateList", Lists.newArrayList(CenterConstants.TASK_ASSIGNMENT_STATE_CANCEL,
        CenterConstants.TASK_ASSIGNMENT_STATE_STOP));

    sql.append(whereSql);
    PageUtils.appendPage(sql, page, AssignmentTaskRecordDO.class);

    List<AssignmentTaskRecordDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentTaskRecordDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(atr.id) from ");
    totalSql.append(TABLE_ASSIGNMENT_TASK_RECORD);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(taskList, page, total);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#queryAssignmentTaskRecords(java.lang.String, java.util.List)
   */
  @Override
  public List<AssignmentTaskRecordDO> queryAssignmentTaskRecords(String fpcSerialNumber,
      List<String> taskIds) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where 1=1 ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(fpcSerialNumber)) {
      sql.append(" and fpc_serial_number = :fpcSerialNumber ");
      params.put("fpcSerialNumber", fpcSerialNumber);
    }

    if (CollectionUtils.isNotEmpty(taskIds)) {
      sql.append(" and task_id in (:taskIds)");
      params.put("taskIds", taskIds);
    }

    List<AssignmentTaskRecordDO> assignmentActionList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentTaskRecordDO.class));

    return CollectionUtils.isNotEmpty(assignmentActionList) ? assignmentActionList
        : Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#queryAssignmentTaskRecordsByMessageIds(java.lang.String)
   */
  @Override
  public List<AssignmentTaskRecordDO> queryAssignmentTaskRecordsByMessageIds(
      List<String> messageIds) {

    if (CollectionUtils.isEmpty(messageIds)) {
      return Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    }

    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where message_id in ( :messageIds ) ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("messageIds", messageIds);

    List<AssignmentTaskRecordDO> assignmentActionList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentTaskRecordDO.class));

    return CollectionUtils.isNotEmpty(assignmentActionList) ? assignmentActionList
        : Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#queryAssignmentTaskRecordsByTaskId(java.lang.String)
   */
  @Override
  public List<AssignmentTaskRecordDO> queryAssignmentTaskRecordsByTaskId(String taskId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where task_id = :taskId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("taskId", taskId);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentTaskRecordDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#queryEarliestExecutionTaskRecord(java.lang.String)
   */
  @Override
  public AssignmentTaskRecordDO queryEarliestExecutionTaskRecord(String taskId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where task_id = :taskId and execution_start_time is not null ");
    sql.append(" order by execution_start_time asc limit 1 ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("taskId", taskId);

    List<AssignmentTaskRecordDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentTaskRecordDO.class));

    return CollectionUtils.isEmpty(list) ? new AssignmentTaskRecordDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#queryAssignmentTaskRecord(java.lang.String, java.lang.String)
   */
  @Override
  public AssignmentTaskRecordDO queryAssignmentTaskRecord(String fpcSerialNumber, String taskId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where fpc_serial_number = :fpcSerialNumber and task_id = :taskId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("taskId", taskId);
    params.put("fpcSerialNumber", fpcSerialNumber);

    List<AssignmentTaskRecordDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentTaskRecordDO.class));

    return CollectionUtils.isEmpty(list) ? new AssignmentTaskRecordDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#batchSaveAssignmentTaskRecords(java.util.List)
   */
  @Override
  public void batchSaveAssignmentTaskRecords(
      List<AssignmentTaskRecordDO> assignmentTaskRecordList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_ASSIGNMENT_TASK_RECORD);
    sql.append(" (id, task_id, fpc_serial_number, message_id, assignment_state, execution_state, ");
    sql.append(" execution_trace, execution_start_time, execution_end_time, execution_progress, ");
    sql.append(" operator_id, assignment_time) ");
    sql.append(
        " values(:id, :taskId, :fpcSerialNumber, :messageId, :assignmentState, :executionState, ");
    sql.append(" :executionTrace, :executionStartTime, :executionEndTime, ");
    sql.append(" :executionProgress, :operatorId, :assignmentTime) ");

    for (AssignmentTaskRecordDO assignmentTaskRecord : assignmentTaskRecordList) {
      assignmentTaskRecord.setId(IdGenerator.generateUUID());
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils
        .createBatch(assignmentTaskRecordList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#updateAssignmentTaskRecords(java.util.List, java.lang.String)
   */
  @Override
  public int updateAssignmentTaskRecords(List<String> taskIds, String assignmentState) {

    if (CollectionUtils.isEmpty(taskIds)) {
      return 0;
    }

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_TASK_RECORD);
    sql.append(" set assignment_state = :assignmentState ");
    sql.append(" where task_id in (:taskIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("assignmentState", assignmentState);
    params.put("taskIds", taskIds);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#updateAssignmentTaskRecords(java.util.List)
   */
  @Override
  public void updateAssignmentTaskRecords(List<AssignmentTaskRecordDO> assignmentTaskRecordList) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_TASK_RECORD);
    sql.append(" set fpc_task_id = :fpcTaskId, message_id = :messageId, ");
    sql.append(" assignment_state = :assignmentState, ");
    sql.append(" execution_state = :executionState, ");
    sql.append(" execution_trace = :executionTrace, ");
    sql.append(" execution_start_time = :executionStartTime, ");
    sql.append(" execution_end_time = :executionEndTime, ");
    sql.append(" execution_progress = :executionProgress, ");
    sql.append(" execution_cache_path = :executionCachePath, ");
    sql.append(" pcap_file_url = :pcapFileUrl ");
    sql.append(" where id = :id ");

    SqlParameterSource[] batchSource = SqlParameterSourceUtils
        .createBatch(assignmentTaskRecordList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#updateAssignmentTaskRecordStates(java.util.List)
   */
  @Override
  public void updateAssignmentTaskRecordStates(List<Map<String, String>> taskRecordStateList) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_TASK_RECORD);
    sql.append(" set assignment_state = :state ");
    sql.append(" where message_id = :messageId ");

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(taskRecordStateList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#batchResetAssignmentTaskRecord(java.util.List, java.lang.String, java.util.Date, java.lang.String)
   */
  @Override
  public int batchResetAssignmentTaskRecord(List<String> ids, String operatorId,
      Date assignmentTime, String newState) {
    StringBuilder sql = new StringBuilder();
    sql.append(" update ").append(TABLE_ASSIGNMENT_TASK_RECORD);
    sql.append(" set assignment_state = :newState, execution_state = 0, execution_trace = '', ");
    sql.append(" execution_start_time = NULL, execution_end_time = NULL, execution_progress = 0, ");
    sql.append(" execution_cache_path = '', pcap_file_url = '', operator_id = :operatorId, ");
    sql.append(" assignment_time = :assignmentTime ");
    sql.append(" where id in (:ids) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("newState", newState);
    params.put("operatorId", operatorId);
    params.put("assignmentTime", assignmentTime);
    params.put("ids", ids);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#updateAssignmentTaskRecord(com.machloop.fpc.cms.center.appliance.data.AssignmentTaskRecordDO)
   */
  @Override
  public int updateAssignmentTaskRecord(AssignmentTaskRecordDO assignmentTaskRecordDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_TASK_RECORD);
    sql.append(" set fpc_task_id = :fpcTaskId, message_id = :messageId, ");
    sql.append(" assignment_state = :assignmentState, ");
    sql.append(" execution_state = :executionState, ");
    sql.append(" execution_trace = :executionTrace, ");
    sql.append(" execution_start_time = :executionStartTime, ");
    sql.append(" execution_end_time = :executionEndTime, ");
    sql.append(" execution_progress = :executionProgress, ");
    sql.append(" execution_cache_path = :executionCachePath, ");
    sql.append(" pcap_file_url = :pcapFileUrl ");
    sql.append(" where id = :id ");

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(assignmentTaskRecordDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#updateAssignmentState(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void updateAssignmentState(String taskId, String originalState, String newState) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_TASK_RECORD);
    sql.append(" set assignment_state = :newState ");
    sql.append(" where task_id = :taskId and assignment_state = :originalState");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("newState", newState);
    params.put("taskId", taskId);
    params.put("originalState", originalState);

    jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao#updatePcapFileUrl(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void updatePcapFileUrl(String taskId, String fpcSerialNumber, String pcapFileUrl) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_TASK_RECORD);
    sql.append(" set pcap_file_url = :pcapFileUrl ");
    sql.append(" where task_id = :taskId and fpc_serial_number = :fpcSerialNumber ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("pcapFileUrl", pcapFileUrl);
    params.put("taskId", taskId);
    params.put("fpcSerialNumber", fpcSerialNumber);

    jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, task_id, fpc_task_id, fpc_serial_number, message_id, ");
    sql.append(" assignment_state, execution_state, execution_trace, execution_start_time, ");
    sql.append(" execution_end_time, execution_progress, execution_cache_path, pcap_file_url, ");
    sql.append(" operator_id, assignment_time ");
    sql.append(" from ").append(TABLE_ASSIGNMENT_TASK_RECORD);
    return sql;
  }

}
