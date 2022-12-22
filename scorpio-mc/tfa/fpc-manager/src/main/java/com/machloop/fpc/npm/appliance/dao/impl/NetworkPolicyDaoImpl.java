package com.machloop.fpc.npm.appliance.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
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
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.npm.appliance.data.NetworkPolicyDO;

/**
 * @author guosk
 *
 * create at 2020年11月11日, fpc-manager
 */
@Repository
public class NetworkPolicyDaoImpl implements NetworkPolicyDao {

  private static final String TABLE_APPLIANCE_NETWORK_POLICY = "fpc_appliance_network_policy";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  private static final String DEFAULT_FILTER_RULE_ID = "1";

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao#queryNetworkPolicys()
   */
  @Override
  public List<NetworkPolicyDO> queryNetworkPolicys() {
    StringBuilder sql = buildSelectStatement();
    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  @Override
  public List<String> queryNetworkPolicyOfNetworkIdAndPolicyId(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select network_id, policy_id from ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where timestamp < :beforeTime ");
    sql.append(" and network_policy_in_cms_id != '' ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    params.put("beforeTime", beforeTime);
    List<Map<String, Object>> result = jdbcTemplate.query(sql.toString(), params,
        new ColumnMapRowMapper());
    List<String> networkIdAndPolicyIdList = result.stream().map(
        map -> MapUtils.getString(map, "network_id") + "_" + MapUtils.getString(map, "policy_id"))
        .collect(Collectors.toList());

    return CollectionUtils.isEmpty(networkIdAndPolicyIdList) ? Lists.newArrayListWithCapacity(0)
        : networkIdAndPolicyIdList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao#queryNetworkPolicyByPolicyId(java.lang.String, java.lang.String)
   */
  @Override
  public List<NetworkPolicyDO> queryNetworkPolicyByPolicyId(String policyId, String policyType) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where policy_type = :policyType and policy_id = :policyId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyType", policyType);
    params.put("policyId", policyId);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  @Override
  public NetworkPolicyDO queryNetworkPolicyByPolicyId(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<NetworkPolicyDO> policyDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));
    return CollectionUtils.isEmpty(policyDOList) ? new NetworkPolicyDO() : policyDOList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao#queryNetworkPolicyByPolicyType(java.lang.String)
   */
  @Override
  public List<NetworkPolicyDO> queryNetworkPolicyByPolicyType(String policyType) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where policy_type = :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyType", policyType);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao#queryNetworkPolicyByNetworkId(java.lang.String)
   */
  @Override
  public List<NetworkPolicyDO> queryNetworkPolicyByNetworkId(String networkId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where network_id = :networkId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  @Override
  public List<NetworkPolicyDO> queryNetworkPolicyByNetworkIdAndPolicyType(String networkId,
      String policyType) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where network_id = :networkId and policy_type = :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);
    params.put("policyType", policyType);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao#queryNetworkPolicyByNetworkId(java.lang.String, java.lang.String)
   */
  @Override
  public NetworkPolicyDO queryNetworkPolicyByNetworkId(String networkId, String policyType) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where network_id = :networkId and policy_type = :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);
    params.put("policyType", policyType);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? new NetworkPolicyDO() : policyList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao#saveNetworkPolicy(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public NetworkPolicyDO saveNetworkPolicy(String networkId, String policyId, String policyType,
      String operatorId) {

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" (id, network_id, policy_type, policy_id, ");
    sql.append(" timestamp, operator_id) ");
    sql.append(" values (:id, :networkId, :policyType, :policyId, ");
    sql.append(" :timestamp, :operatorId) ");

    NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
    if (StringUtils.isBlank(networkPolicyDO.getId())) {
      networkPolicyDO.setId(IdGenerator.generateUUID());
    }
    networkPolicyDO.setTimestamp(DateUtils.now());
    networkPolicyDO.setNetworkId(networkId);
    networkPolicyDO.setPolicyId(policyId);
    networkPolicyDO.setPolicyType(policyType);
    networkPolicyDO.setOperatorId(operatorId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(networkPolicyDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return networkPolicyDO;
  }

  @Override
  public int saveNetworkPolicy(List<NetworkPolicyDO> networkPolicyDOList, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" (id, network_id, policy_type, policy_id, ");
    sql.append(" timestamp, operator_id, network_policy_in_cms_id) ");
    sql.append(
        " values ( :id, :networkId, :policyType, :policyId, :timestamp, :operatorId, :networkPolicyInCmsId) ");

    for (NetworkPolicyDO networkPolicyDO : networkPolicyDOList) {
      if (StringUtils.isBlank(networkPolicyDO.getId())) {
        networkPolicyDO.setId(IdGenerator.generateUUID());
      }
      if (StringUtils.isBlank(networkPolicyDO.getNetworkPolicyInCmsId())) {
        networkPolicyDO.setNetworkPolicyInCmsId("");
      }
      networkPolicyDO.setTimestamp(DateUtils.now());
      networkPolicyDO.setOperatorId(operatorId);
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(networkPolicyDOList);
    return jdbcTemplate.batchUpdate(sql.toString(), batchSource)[0];
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao#mergeNetworkPolicys(java.util.List)
   */
  @Override
  public void mergeNetworkPolicys(List<NetworkPolicyDO> networkPolicys) {
    // delete
    deleteNetworkPolicyByNetworkId(networkPolicys.get(0).getNetworkId());

    // batch insert
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" (id, network_id, policy_type, policy_id, ");
    sql.append(" timestamp, operator_id) ");
    sql.append(" values (:id, :networkId, :policyType, :policyId, ");
    sql.append(" :timestamp, :operatorId) ");

    networkPolicys.forEach(networkPolicy -> {
      networkPolicy.setId(IdGenerator.generateUUID());
      networkPolicy.setTimestamp(DateUtils.now());
    });
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(networkPolicys);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  @Override
  public void mergeNetworkPolicysOfSend(List<NetworkPolicyDO> networkPolicys) {

    // delete
    deleteNetworkPolicyByNetworkIdAndPolicyType(networkPolicys.get(0).getNetworkId(),
        FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);

    // batch insert
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" (id, network_id, policy_type, policy_id, ");
    sql.append(" timestamp, operator_id, network_policy_in_cms_id ) ");
    sql.append(" values (:id, :networkId, :policyType, :policyId, ");
    sql.append(" :timestamp, :operatorId, :networkPolicyInCmsId ) ");

    networkPolicys.forEach(networkPolicy -> {
      networkPolicy.setTimestamp(DateUtils.now());
    });
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(networkPolicys);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao#updateNetworkPolicy(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int updateNetworkPolicy(String networkId, String policyId, String policyType,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" set policy_id = :policyId, timestamp = :timestamp, operator_id = :operatorId ");
    sql.append(" where network_id = :networkId and policy_type = :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyId", policyId);
    params.put("timestamp", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("networkId", networkId);
    params.put("policyType", policyType);

    return jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public int updateNetworkPolicyByPolicyId(String id, String networkId, String policyId,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(
        " set policy_id = :policyId, timestamp = :timestamp, operator_id = :operatorId, network_id = :networkId ");
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyId", policyId);
    params.put("timestamp", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("networkId", networkId);
    params.put("id", id);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao#deleteNetworkPolicyByNetworkId(java.lang.String)
   */
  @Override
  public int deleteNetworkPolicyByNetworkId(String networkId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where network_id = :networkId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);
    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao#deleteNetworkPolicyByPolicyId(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteNetworkPolicyByPolicyId(String policyId, String policyType) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where policy_id = :policyId and policy_type = :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyId", policyId);
    params.put("policyType", policyType);

    return jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public void deleteNetworkPolicyByNetworkIdAndPolicyType(String networkId, String policyType) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where network_id = :networkId and policy_type = :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);
    params.put("policyType", policyType);

    jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public int deleteNetworkPolicyByFilterRule(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where policy_id != :policyId and policy_type = :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyType", FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
    params.put("policyId", DEFAULT_FILTER_RULE_ID);

    if (onlyLocal) {
      sql.append(" and network_policy_in_cms_id = '' ");
    }

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, network_id, policy_type, policy_id, ");
    sql.append(" timestamp, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_NETWORK_POLICY);

    return sql;
  }

}
