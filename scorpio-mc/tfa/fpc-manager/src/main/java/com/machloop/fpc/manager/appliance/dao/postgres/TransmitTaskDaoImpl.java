package com.machloop.fpc.manager.appliance.dao.postgres;

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
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.dao.TransmitTaskDao;
import com.machloop.fpc.manager.appliance.data.TransmitTaskDO;
import com.machloop.fpc.manager.appliance.vo.TransmitTaskQueryVO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
@Repository
public class TransmitTaskDaoImpl implements TransmitTaskDao {

  private static final String TABLE_APPLIANCE_TRANSMIT_TASK = "fpc_appliance_transmit_task";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#queryTransmitTasks(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.manager.appliance.vo.TransmitTaskQueryVO)
   */
  @Override
  public Page<TransmitTaskDO> queryTransmitTasks(Pageable page, TransmitTaskQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(queryVO.getName())) {
      whereSql.append(" and name like :name ");
      params.put("name", "%" + queryVO.getName() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getFilterNetworkId())) {
      whereSql.append(" and filter_network_id = :filterNetworkId ");
      params.put("filterNetworkId", queryVO.getFilterNetworkId());
    }
    if (StringUtils.isNotBlank(queryVO.getFilterPacketFileId())) {
      whereSql.append(" and filter_packet_file_id = :filterPacketFileId ");
      params.put("filterPacketFileId", queryVO.getFilterPacketFileId());
    }
    if (StringUtils.isNotBlank(queryVO.getFilterConditionType())) {
      whereSql.append(" and filter_condition_type = :filterConditionType ");
      params.put("filterConditionType", queryVO.getFilterConditionType());
    }
    if (StringUtils.isNotBlank(queryVO.getMode())) {
      whereSql.append(" and mode = :mode ");
      params.put("mode", queryVO.getMode());
    }
    if (StringUtils.isNotBlank(queryVO.getState())) {
      whereSql.append(" and state = :state ");
      params.put("state", queryVO.getState());
    }
    if (StringUtils.isNotBlank(queryVO.getSource())) {
      whereSql.append(" and source like :source ");
      params.put("source", "%" + queryVO.getSource() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getSourceType())) {
      if (queryVO.getSourceType().equals("rest")) {
        whereSql.append(" and source like :source1 ");
        params.put("source1", "REST" + "%");
      } else if (queryVO.getSourceType().equals("assignment")) {
        whereSql.append(" and source like :source2 ");
        params.put("source2", "assignment" + "%");
      } else {
        whereSql.append(" and source not like :source3 ");
        params.put("source3", "REST" + "%");
        whereSql.append(" and source not like :source4 ");
        params.put("source4", "assignment" + "%");
      }
    }

    sql.append(whereSql);

    PageUtils.appendPage(sql, page, TransmitTaskDO.class);

    List<TransmitTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(TransmitTaskDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_TRANSMIT_TASK);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(taskList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#queryAssignmentTasks()
   */
  @Override
  public List<TransmitTaskDO> queryAssignmentTasks() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted and assign_task_id != '' ");
    whereSql.append(" order by create_time DESC ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<TransmitTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(TransmitTaskDO.class));
    return CollectionUtils.isNotEmpty(taskList) ? taskList
        : Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
  }

  @Override
  public List<TransmitTaskDO> queryTransmitTasksByMode(String mode) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted and mode = :mode and state != :state ");
    whereSql.append(" order by create_time DESC ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("mode", mode);
    params.put("state", FpcConstants.APPLIANCE_TRANSMITTASK_STATE_FINISH);

    List<TransmitTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(TransmitTaskDO.class));
    return CollectionUtils.isNotEmpty(taskList) ? taskList
        : Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#queryTransmitTask(java.lang.String)
   */
  @Override
  public TransmitTaskDO queryTransmitTask(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<TransmitTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(TransmitTaskDO.class));
    return CollectionUtils.isEmpty(taskList) ? new TransmitTaskDO() : taskList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#queryTransmitTaskByName(java.lang.String)
   */
  @Override
  public TransmitTaskDO queryTransmitTaskByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<TransmitTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(TransmitTaskDO.class));
    return CollectionUtils.isEmpty(taskList) ? new TransmitTaskDO() : taskList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#queryTransmitTaskByAssignTaskId(java.lang.String)
   */
  @Override
  public TransmitTaskDO queryTransmitTaskByAssignTaskId(String assignTaskId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and assign_task_id = :assignTaskId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignTaskId", assignTaskId);

    List<TransmitTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(TransmitTaskDO.class));
    return CollectionUtils.isEmpty(taskList) ? new TransmitTaskDO() : taskList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#saveTransmitTask(com.machloop.fpc.manager.appliance.data.TransmitTaskDO)
   */
  @Override
  public TransmitTaskDO saveTransmitTask(TransmitTaskDO transmitTaskDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_TRANSMIT_TASK);
    sql.append(" (id, assign_task_id, assign_task_time, name, source, filter_start_time, ");
    sql.append(" filter_end_time, filter_condition_type, filter_tuple, filter_bpf, filter_raw, ");
    sql.append(" filter_network_id, filter_packet_file_id, mode, replay_netif, replay_rate, ");
    sql.append(" replay_rate_unit, replay_rule, forward_action, ip_tunnel, ");
    sql.append(" description, deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :assignTaskId, :assignTaskTime, :name, :source, :filterStartTime, ");
    sql.append(" :filterEndTime, :filterConditionType, :filterTuple, :filterBpf, :filterRaw, ");
    sql.append(" :filterNetworkId, :filterPacketFileId, :mode, :replayNetif, :replayRate, ");
    sql.append(" :replayRateUnit, :replayRule, :forwardAction, :ipTunnel, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId) ");

    transmitTaskDO.setId(IdGenerator.generateUUID());
    transmitTaskDO.setCreateTime(DateUtils.now());
    transmitTaskDO.setUpdateTime(transmitTaskDO.getCreateTime());
    transmitTaskDO.setState(FpcConstants.APPLIANCE_TRANSMITTASK_STATE_RUN);
    transmitTaskDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(transmitTaskDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return transmitTaskDO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#updateTransmitTask(com.machloop.fpc.manager.appliance.data.TransmitTaskDO)
   */
  @Override
  public int updateTransmitTask(TransmitTaskDO transmitTaskDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_TRANSMIT_TASK);
    sql.append(" set name = :name, filter_tuple = :filterTuple, filter_raw = :filterRaw, ");
    sql.append(" description = :description, replay_netif = :replayNetif, ");
    sql.append(
        " replay_rate_unit = :replayRateUnit, replay_rate = :replayRate, replay_rule = :replayRule, ");
    sql.append(
        " forward_action = :forwardAction, ip_tunnel = :ipTunnel, filter_packet_file_id = :filterPacketFileId, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    transmitTaskDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(transmitTaskDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#updateTransmitTaskByAssignTaskId(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date)
   */
  @Override
  public int updateTransmitTaskByAssignTaskId(String assignTaskId, String name, String filterTuple,
      String filterRaw, String description, Date assignTaskTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_TRANSMIT_TASK);
    sql.append(" set name = :name, filter_tuple = :filterTuple, filter_raw = :filterRaw, ");
    sql.append(" description = :description, assign_task_time = :assignTaskTime ");
    sql.append(" where deleted = :deleted and assign_task_id = :assignTaskId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("assignTaskId", assignTaskId);
    params.put("deleted", Constants.BOOL_NO);
    params.put("name", name);
    params.put("filterTuple", filterTuple);
    params.put("filterRaw", filterRaw);
    params.put("description", description);
    params.put("assignTaskTime", assignTaskTime);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#updateTransmitTaskExecutionDownloadUrl(java.lang.String)
   */
  @Override
  public int updateTransmitTaskExecutionDownloadUrl(String id, String executionDownloadUrl) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_TRANSMIT_TASK);
    sql.append(" set execution_download_url = :executionDownloadUrl ");
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);
    params.put("deleted", Constants.BOOL_NO);
    params.put("executionDownloadUrl", executionDownloadUrl);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#redoTransmitTask(java.lang.String)
   */
  @Override
  public int redoTransmitTask(String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_TRANSMIT_TASK);
    sql.append(" set execution_start_time = NULL, execution_end_time = NULL, ");
    sql.append(" execution_progress = 0, execution_download_url = '', ");
    sql.append(" transfer_time = :transfer_time, update_time = :update_time, state= :state ");
    sql.append(" where id = :id ");

    Date currentTime = DateUtils.now();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);
    params.put("transfer_time", currentTime);
    params.put("update_time", currentTime);
    params.put("state", FpcConstants.APPLIANCE_TRANSMITTASK_STATE_RUN);
    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#stopTransmitTask(java.lang.String)
   */
  @Override
  public int stopTransmitTask(String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_TRANSMIT_TASK)
        .append(" set state= :state where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);
    params.put("transfer_time", DateUtils.now());
    params.put("state", FpcConstants.APPLIANCE_TRANSMITTASK_STATE_STOP);
    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#deleteTransmitTask(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteTransmitTask(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_TRANSMIT_TASK);
    sql.append(" set deleted = :deleted, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    TransmitTaskDO transmitTaskDO = new TransmitTaskDO();
    transmitTaskDO.setDeleted(Constants.BOOL_YES);
    transmitTaskDO.setDeleteTime(DateUtils.now());
    transmitTaskDO.setOperatorId(operatorId);
    transmitTaskDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(transmitTaskDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.TransmitTaskDao#deleteTransmitTaskByAssignTaskId(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteTransmitTaskByAssignTaskId(String assignTaskId, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_TRANSMIT_TASK);
    sql.append(" set deleted = :deleted, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where assign_task_id = :assignTaskId ");

    TransmitTaskDO transmitTaskDO = new TransmitTaskDO();
    transmitTaskDO.setDeleted(Constants.BOOL_YES);
    transmitTaskDO.setDeleteTime(DateUtils.now());
    transmitTaskDO.setOperatorId(operatorId);
    transmitTaskDO.setAssignTaskId(assignTaskId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(transmitTaskDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_task_id, assign_task_time, name, source, ");
    sql.append(" filter_start_time, filter_end_time, filter_condition_type, filter_tuple, ");
    sql.append(" filter_bpf, filter_raw, filter_network_id, filter_packet_file_id, mode, ");
    sql.append(
        " replay_netif, replay_rate, replay_rate_unit, replay_rule, forward_action, ip_tunnel, ");
    sql.append(" description, execution_start_time, execution_end_time, execution_progress, ");
    sql.append(" execution_cache_path, execution_download_url, execution_trace, ");
    sql.append(" state, deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_TRANSMIT_TASK);

    return sql;
  }
}
