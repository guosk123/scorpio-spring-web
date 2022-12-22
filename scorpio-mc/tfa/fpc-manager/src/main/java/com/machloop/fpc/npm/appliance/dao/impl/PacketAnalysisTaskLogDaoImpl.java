package com.machloop.fpc.npm.appliance.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskLogDao;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskLogDO;

/**
 * @author guosk
 *
 * create at 2022年3月15日, fpc-manager
 */
@Repository
public class PacketAnalysisTaskLogDaoImpl implements PacketAnalysisTaskLogDao {

  private static final String TABLE_APPLIANCE_PACKET_ANALYSIS_TASK_LOG = "fpc_appliance_packet_analysis_task_log";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskLogDao#queryAnalysisLog(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String)
   */
  @Override
  public Page<PacketAnalysisTaskLogDO> queryAnalysisLog(Pageable page, String taskId,
      String subTaskId) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, task_id, sub_task_id, status, content, arise_time ");
    sql.append(" from ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK_LOG);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where 1=1 ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(taskId)) {
      whereSql.append(" and task_id = :taskId ");
      params.put("taskId", taskId);
    }

    if (StringUtils.isNotBlank(subTaskId)) {
      whereSql.append(" and sub_task_id = :subTaskId ");
      params.put("subTaskId", subTaskId);
    }

    sql.append(whereSql);
    PageUtils.appendPage(sql, page, PacketAnalysisTaskLogDO.class);

    List<PacketAnalysisTaskLogDO> taskLogList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PacketAnalysisTaskLogDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK_LOG);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(taskLogList, page, total);
  }

}
