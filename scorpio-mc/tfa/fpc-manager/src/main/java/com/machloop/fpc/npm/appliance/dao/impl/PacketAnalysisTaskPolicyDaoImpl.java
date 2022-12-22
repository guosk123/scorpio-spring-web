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
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskPolicyDao;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskPolicyDO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
@Repository
public class PacketAnalysisTaskPolicyDaoImpl implements PacketAnalysisTaskPolicyDao {

  private static final String TABLE_APPLIANCE_PACKET_ANALYSIS_TASK_POLICY = "fpc_appliance_packet_analysis_task_policy";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;


  @Override
  public List<PacketAnalysisTaskPolicyDO> queryPacketAnalysisTaskPolicyByPolicyIdAndPolicyType(
      String policyId, String policyType) {

    StringBuilder sql = buildSelectStatement();
    sql.append(" where policy_type = :policyType and policy_id = :policyId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyType", policyType);
    params.put("policyId", policyId);


    List<PacketAnalysisTaskPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PacketAnalysisTaskPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  @Override
  public List<PacketAnalysisTaskPolicyDO> queryPolicyIdsByIdAndPolicyType(String id,
      String policyType) {
    StringBuilder sql = buildSelectStatement();
    sql.append(
        " where policy_type = :policyType and packet_analysis_task_id = :packetAnalysisTaskId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyType", policyType);
    params.put("packetAnalysisTaskId", id);


    List<PacketAnalysisTaskPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PacketAnalysisTaskPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  @Override
  public List<PacketAnalysisTaskPolicyDO> queryPolicyIdsByPolicyType(String policyType) {

    StringBuilder sql = buildSelectStatement();
    sql.append(" where policy_type = :policyType  ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyType", policyType);

    List<PacketAnalysisTaskPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PacketAnalysisTaskPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  @Override
  public void savePacketAnalysisTaskPolicy(String packetAnalysisTaskId, String policyId,
      String policyType, String operatorId) {

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK_POLICY);
    sql.append(" (id, packet_analysis_task_id, policy_type, policy_id, ");
    sql.append(" timestamp, operator_id) ");
    sql.append(" values (:id, :networkId, :policyType, :policyId, ");
    sql.append(" :timestamp, :operatorId) ");

    PacketAnalysisTaskPolicyDO packetAnalysisTaskPolicyDO = new PacketAnalysisTaskPolicyDO();
    if (StringUtils.isBlank(packetAnalysisTaskPolicyDO.getId())) {
      packetAnalysisTaskPolicyDO.setId(IdGenerator.generateUUID());
    }
    packetAnalysisTaskPolicyDO.setTimestamp(DateUtils.now());
    packetAnalysisTaskPolicyDO.setPacketAnalysisTaskId(packetAnalysisTaskId);
    packetAnalysisTaskPolicyDO.setPolicyId(policyId);
    packetAnalysisTaskPolicyDO.setPolicyType(policyType);
    packetAnalysisTaskPolicyDO.setOperatorId(operatorId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(packetAnalysisTaskPolicyDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public void deletePacketAnalysisTaskPolicyByPacketAnalysisTaskId(String packetAnalysisTaskId) {

    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK_POLICY);
    sql.append(" where packet_analysis_task_id = :packetAnalysisTaskId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("packetAnalysisTaskId", packetAnalysisTaskId);
    jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public void deletePacketAnalysisTaskPolicyByPolicyId(String policyId, String policyType) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK_POLICY);
    sql.append(" where policy_id = :policyId and policy_type = :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyId", policyId);
    params.put("policyType", policyType);

    jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public void mergePacketAnalysisTaskPolicies(List<PacketAnalysisTaskPolicyDO> policyList) {

    deletePacketAnalysisTaskPolicyByPacketAnalysisTaskId(
        policyList.get(0).getPacketAnalysisTaskId());

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK_POLICY);
    sql.append(" (id, packet_analysis_task_id, policy_type, policy_id, ");
    sql.append(" timestamp, operator_id) ");
    sql.append(" values (:id, :packetAnalysisTaskId, :policyType, :policyId, ");
    sql.append(" :timestamp, :operatorId) ");

    policyList.forEach(policy -> {
      policy.setId(IdGenerator.generateUUID());
      policy.setTimestamp(DateUtils.now());
    });
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(policyList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);

  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, packet_analysis_task_id, policy_type, policy_id, ");
    sql.append(" timestamp, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_PACKET_ANALYSIS_TASK_POLICY);

    return sql;
  }
}
