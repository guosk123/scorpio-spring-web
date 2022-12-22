package com.machloop.fpc.npm.appliance.dao.impl;

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

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisSubTaskDO;

/**
 * @author guosk
 *
 * create at 2022年3月15日, fpc-manager
 */
@Repository
public class PacketAnalysisSubTaskDaoImpl implements PacketAnalysisSubTaskDao {

  private static final String TABLE_APPLIANCE_PACKET_ANALYSIS_SUB_TASK = "fpc_appliance_packet_analysis_sub_task";
  private static final String TABLE_APPLIANCE_PACKET_ANALYSIS_TASK = "fpc_appliance_packet_analysis_task";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao#queryPacketAnalysisSubTasks(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<PacketAnalysisSubTaskDO> queryPacketAnalysisSubTasks(Pageable page, String name,
      String taskId, String source) {
    StringBuilder sql = new StringBuilder();
    sql.append("select subTask.id as id, subTask.name as name, task_id, packet_start_time, ");
    sql.append(" packet_end_time, size, subTask.file_path as file_path, ");
    sql.append(" subTask.status as status, execution_progress, execution_result, ");
    sql.append(" subTask.deleted as deleted, subTask.create_time as create_time, ");
    sql.append(" subTask.operator_id as operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_SUB_TASK).append(" as subTask ");
    sql.append(" left join ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK).append(" as task ");
    sql.append(" on subTask.task_id = task.id ");

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where subTask.deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and subTask.name like :name ");
      params.put("name", "%" + name + "%");
    }
    if (StringUtils.isNotBlank(taskId)) {
      whereSql.append(" and task_id = :taskId ");
      params.put("taskId", taskId);
    }
    if (StringUtils.isNotBlank(source)) {
      whereSql.append(" and task.source = :source ");
      params.put("source", source);
    }

    sql.append(whereSql);
    PageUtils.appendPage(sql, page, PacketAnalysisSubTaskDO.class);

    List<PacketAnalysisSubTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PacketAnalysisSubTaskDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(subTask.id) ");
    totalSql.append(" from ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_SUB_TASK)
        .append(" as subTask ");
    totalSql.append(" left join ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK).append(" as task ");
    totalSql.append(" on subTask.task_id = task.id ");
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(taskList, page, total);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao#queryPacketAnalysisSubTask(java.lang.String)
   */
  @Override
  public PacketAnalysisSubTaskDO queryPacketAnalysisSubTask(String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, task_id, packet_start_time, packet_end_time, size, ");
    sql.append(" file_path, status, execution_progress, execution_result, ");
    sql.append(" deleted, create_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_SUB_TASK);
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<PacketAnalysisSubTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PacketAnalysisSubTaskDO.class));

    return CollectionUtils.isEmpty(taskList) ? new PacketAnalysisSubTaskDO() : taskList.get(0);

  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao#deleteSubTaskById(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteSubTaskById(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_SUB_TASK);
    sql.append(" set deleted = :deleted, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    PacketAnalysisSubTaskDO packetAnalysisTaskDO = new PacketAnalysisSubTaskDO();
    packetAnalysisTaskDO.setDeleted(Constants.BOOL_YES);
    packetAnalysisTaskDO.setDeleteTime(DateUtils.now());
    packetAnalysisTaskDO.setOperatorId(operatorId);
    packetAnalysisTaskDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(packetAnalysisTaskDO);
    return jdbcTemplate.update(sql.toString(), paramSource);

  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao#deleteSubTaskByTaskId(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteSubTaskByTaskId(String mainTaskId, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_SUB_TASK);
    sql.append(" set deleted = :deleted, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where task_id = :taskId ");

    PacketAnalysisSubTaskDO packetAnalysisTaskDO = new PacketAnalysisSubTaskDO();
    packetAnalysisTaskDO.setDeleted(Constants.BOOL_YES);
    packetAnalysisTaskDO.setDeleteTime(DateUtils.now());
    packetAnalysisTaskDO.setOperatorId(operatorId);
    packetAnalysisTaskDO.setTaskId(mainTaskId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(packetAnalysisTaskDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

}
