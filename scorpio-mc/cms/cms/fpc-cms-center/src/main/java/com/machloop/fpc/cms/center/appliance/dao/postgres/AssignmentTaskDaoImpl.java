package com.machloop.fpc.cms.center.appliance.dao.postgres;

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
import com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao;
import com.machloop.fpc.cms.center.appliance.data.AssignmentTaskDO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class AssignmentTaskDaoImpl implements AssignmentTaskDao {

  private static final String TABLE_ASSIGNMENT_TASK = "fpccms_appliance_assignment_task";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao#queryAssignmentTasks(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<AssignmentTaskDO> queryAssignmentTasks(Pageable page, String name,
                                                     String filterConditionType, String mode,String source,String sourceType) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and name like :name");
      params.put("name", "%" + name + "%");
    }
    if (StringUtils.isNotBlank(filterConditionType)) {
      whereSql.append(" and filter_condition_type = :filterConditionType");
      params.put("filterConditionType", filterConditionType);
    }
    if (StringUtils.isNotBlank(mode)) {
      whereSql.append(" and mode = :mode ");
      params.put("mode", mode);
    }
    if (StringUtils.isNotBlank(source)){
      whereSql.append(" and source like :source ");
      params.put("source","%"+source+"%");
    }
    if (StringUtils.isNotBlank(sourceType)) {
      if (sourceType.equals("rest")){
        whereSql.append(" and source like :source1 ");
        params.put("source1", "REST" + "%");
      }else if (sourceType.equals("assignment")){
        whereSql.append(" and source like :source2 ");
        params.put("source2", "assignment" + "%");
      }else{
        whereSql.append(" and source not like :source3 ");
        params.put("source3", "REST" + "%");
        whereSql.append(" and source not like :source4 ");
        params.put("source4", "assignment" + "%");
      }
    }
    sql.append(whereSql);

    PageUtils.appendPage(sql, page, AssignmentTaskDO.class);

    List<AssignmentTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
            new BeanPropertyRowMapper<>(AssignmentTaskDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_ASSIGNMENT_TASK);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(taskList, page, total);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao#queryHigherAssignmentTasks(java.util.Date, java.util.Date)
   */
  @Override
  public List<AssignmentTaskDO> queryHigherAssignmentTasks(Date startTime, Date endTime) {
    StringBuilder sql = buildSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where deleted = :deleted and assign_task_id != '' ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (startTime != null) {
      sql.append(" and update_time > :startTime ");
      params.put("startTime", startTime);
    }
    if (startTime != null) {
      sql.append(" and update_time <= :endTime ");
      params.put("endTime", endTime);
    }
    sql.append(" order by create_time DESC ");

    List<AssignmentTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentTaskDO.class));
    return CollectionUtils.isNotEmpty(taskList) ? taskList
        : Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao#queryAssignmentTask(java.lang.String, boolean)
   */
  @Override
  public AssignmentTaskDO queryAssignmentTask(String id, boolean isContainDelete) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    if (!isContainDelete) {
      sql.append(" and deleted = :deleted ");
      params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    }

    List<AssignmentTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentTaskDO.class));
    return CollectionUtils.isEmpty(taskList) ? new AssignmentTaskDO() : taskList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao#queryAssignmentTaskByName(java.lang.String)
   */
  @Override
  public AssignmentTaskDO queryAssignmentTaskByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<AssignmentTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentTaskDO.class));
    return CollectionUtils.isEmpty(taskList) ? new AssignmentTaskDO() : taskList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao#queryAssignmentTaskByAssignTaskId(java.lang.String)
   */
  @Override
  public AssignmentTaskDO queryAssignmentTaskByAssignTaskId(String assignTaskId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and assign_task_id = :assignTaskId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignTaskId", assignTaskId);

    List<AssignmentTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssignmentTaskDO.class));
    return CollectionUtils.isEmpty(taskList) ? new AssignmentTaskDO() : taskList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao#saveAssignmentTask(com.machloop.fpc.cms.center.appliance.data.AssignmentTaskDO)
   */
  @Override
  public AssignmentTaskDO saveAssignmentTask(AssignmentTaskDO assignmentTaskDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ASSIGNMENT_TASK);
    sql.append(" (id, assign_task_id, assign_task_time, name, source, filter_start_time, ");
    sql.append(" filter_end_time, filter_network_id, filter_condition_type, ");
    sql.append(" filter_tuple, filter_bpf, filter_raw, mode, ");
    sql.append(" replay_netif, replay_rate, replay_rate_unit, forward_action, ");
    sql.append(" description, deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :assignTaskId, :assignTaskTime, :name, :source, :filterStartTime, ");
    sql.append(" :filterEndTime, :filterNetworkId, :filterConditionType, ");
    sql.append(" :filterTuple, :filterBpf, :filterRaw, :mode, ");
    sql.append(" :replayNetif, :replayRate, :replayRateUnit, :forwardAction, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId ) ");

    assignmentTaskDO.setId(IdGenerator.generateUUID());
    assignmentTaskDO.setCreateTime(DateUtils.now());
    assignmentTaskDO.setUpdateTime(assignmentTaskDO.getCreateTime());
    assignmentTaskDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(assignmentTaskDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return assignmentTaskDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao#updateAssignmentTask(com.machloop.fpc.cms.center.appliance.data.AssignmentTaskDO)
   */
  @Override
  public int updateAssignmentTask(AssignmentTaskDO assignmentTaskDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_TASK);
    sql.append(" set name = :name, filter_tuple = :filterTuple, filter_raw = :filterRaw, ");
    sql.append(" description = :description, update_time = :updateTime, operator_id = :operatorId");
    sql.append(" where id = :id ");

    assignmentTaskDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(assignmentTaskDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao#updateAssignmentTaskByAssignTaskId(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date)
   */
  @Override
  public int updateAssignmentTaskByAssignTaskId(String assignTaskId, String name,
      String filterTuple, String filterRaw, String description, Date assignTaskTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_TASK);
    sql.append(" set name = :name, filter_tuple = :filterTuple, filter_raw = :filterRaw, ");
    sql.append(" description = :description, assign_task_time = :assignTaskTime, ");
    sql.append(" update_time = :updateTime ");
    sql.append(" where deleted = :deleted and assign_task_id = :assignTaskId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("assignTaskId", assignTaskId);
    params.put("deleted", Constants.BOOL_NO);
    params.put("name", name);
    params.put("filterTuple", filterTuple);
    params.put("filterRaw", filterRaw);
    params.put("description", description);
    params.put("assignTaskTime", assignTaskTime);
    params.put("updateTime", DateUtils.now());

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao#deleteAssignmentTask(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteAssignmentTask(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ASSIGNMENT_TASK);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime,");
    sql.append(" operator_id = :operatorId where id = :id ");

    AssignmentTaskDO assignmentTaskDO = new AssignmentTaskDO();
    assignmentTaskDO.setDeleted(Constants.BOOL_YES);
    assignmentTaskDO.setDeleteTime(DateUtils.now());
    assignmentTaskDO.setOperatorId(operatorId);
    assignmentTaskDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(assignmentTaskDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_task_id, assign_task_time, name, source, ");
    sql.append(" filter_start_time, filter_end_time, filter_network_id, ");
    sql.append(" filter_condition_type, filter_tuple, filter_bpf, filter_raw, ");
    sql.append(" mode, replay_netif, replay_rate, replay_rate_unit, forward_action, ");
    sql.append(" description, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_ASSIGNMENT_TASK);
    return sql;
  }

}
