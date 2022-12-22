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
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskDao;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskDO;

/**
 * @author guosk
 *
 * create at 2021年6月16日, fpc-manager
 */
@Repository
public class PacketAnalysisTaskDaoImpl implements PacketAnalysisTaskDao {

  private static final String TABLE_APPLIANCE_PACKET_ANALYSIS_TASK = "fpc_appliance_packet_analysis_task";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskDao#queryPacketAnalysisTasks(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String)
   */
  @Override
  public Page<PacketAnalysisTaskDO> queryPacketAnalysisTasks(Pageable page, String name,
      String status) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, mode, source, file_path, configuration, execution_trace, ");
    sql.append(" status, deleted, create_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and name like :name ");
      params.put("name", "%" + name + "%");
    }

    if (StringUtils.isNotBlank(status)) {
      whereSql.append(" and status = :status ");
      params.put("status", status);
    }

    sql.append(whereSql);
    PageUtils.appendPage(sql, page, PacketAnalysisTaskDO.class);

    List<PacketAnalysisTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PacketAnalysisTaskDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(taskList, page, total);
  }

  @Override
  public List<PacketAnalysisTaskDO> queryPacketAnalysisTaskForRetApi(String name, String status) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, mode, source, file_path, configuration, execution_trace, ");
    sql.append(" status, deleted, create_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and name like :name ");
      params.put("name", "%" + name + "%");
    }

    if (StringUtils.isNotBlank(status)) {
      whereSql.append(" and status = :status ");
      params.put("status", status);
    }

    sql.append(whereSql);

    List<PacketAnalysisTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PacketAnalysisTaskDO.class));

    return CollectionUtils.isEmpty(taskList) ? Lists.newArrayListWithCapacity(0) : taskList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskDao#queryPacketAnalysisTask(java.lang.String)
   */
  @Override
  public PacketAnalysisTaskDO queryPacketAnalysisTask(String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, mode, source, file_path, configuration, execution_trace, ");
    sql.append(" status, deleted, create_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK);
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<PacketAnalysisTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PacketAnalysisTaskDO.class));

    return CollectionUtils.isEmpty(taskList) ? new PacketAnalysisTaskDO() : taskList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskDao#queryPacketAnalysisTaskByName(java.lang.String)
   */
  @Override
  public PacketAnalysisTaskDO queryPacketAnalysisTaskByName(String name) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, mode, source, file_path, configuration, execution_trace, ");
    sql.append(" status, deleted, create_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK);
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<PacketAnalysisTaskDO> taskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PacketAnalysisTaskDO.class));

    return CollectionUtils.isEmpty(taskList) ? new PacketAnalysisTaskDO() : taskList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskDao#savePacketAnalysisTask(com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskDO)
   */
  @Override
  public int savePacketAnalysisTask(PacketAnalysisTaskDO packetAnalysisTaskDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK);
    sql.append(" (id, name, mode, source, file_path, configuration, ");
    sql.append(" deleted, create_time, operator_id)");
    sql.append(" values(:id, :name, :mode, :source, :filePath, :configuration, ");
    sql.append(" :deleted, :createTime, :operatorId)");

    packetAnalysisTaskDO.setId(StringUtils.replace(IdGenerator.generateUUID(), "-", "_"));
    packetAnalysisTaskDO.setDeleted(Constants.BOOL_NO);
    packetAnalysisTaskDO.setCreateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(packetAnalysisTaskDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskDao#deletePacketAnalysisTask(java.lang.String, java.lang.String)
   */
  @Override
  public int deletePacketAnalysisTask(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK);
    sql.append(" set deleted = :deleted, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    PacketAnalysisTaskDO packetAnalysisTaskDO = new PacketAnalysisTaskDO();
    packetAnalysisTaskDO.setDeleted(Constants.BOOL_YES);
    packetAnalysisTaskDO.setDeleteTime(DateUtils.now());
    packetAnalysisTaskDO.setOperatorId(operatorId);
    packetAnalysisTaskDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(packetAnalysisTaskDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }


}
