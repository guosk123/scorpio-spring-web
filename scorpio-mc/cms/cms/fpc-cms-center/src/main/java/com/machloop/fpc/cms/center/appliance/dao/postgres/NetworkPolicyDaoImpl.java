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
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.cms.center.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author "Minjiajun"
 *
 * create at 2021年12月1日, fpc-cms-center
 */
@Repository
public class NetworkPolicyDaoImpl implements NetworkPolicyDao {

  private static final String TABLE_APPLIANCE_NETWORK_POLICY = "fpccms_appliance_network_policy";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#queryNetworkPolicyByPolicyType(java.lang.String)
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
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#queryNetworkPolicyIds(java.util.Date)
   */
  @Override
  public List<String> queryNetworkPolicyIds(Date beforeTime) {

    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where timestamp < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public List<String> queryNetworkPolicyIdsExceptSendPolicy(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where timestamp < :beforeTime and policy_type <> :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("beforeTime", beforeTime);
    params.put("policyType", FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public List<String> queryNetworkPolicyIdsOfSendPolicy(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where timestamp < :beforeTime and policy_type = :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("beforeTime", beforeTime);
    params.put("policyType", FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#queryNetworkPolicys()
   */
  @Override
  public List<NetworkPolicyDO> queryNetworkPolicys() {
    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#queryNetworkPolicyByNetworkId(java.lang.String)
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

  @Override
  public List<NetworkPolicyDO> queryNetworkPolicyByNetworkId(String networkId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where network_id = :networkId");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  @Override
  public List<NetworkPolicyDO> queryNetworkPolicyByNetworkIdAndPolicySource(String networkId,
      String policyType, String policySource) {
    StringBuilder sql = buildSelectStatement();
    sql.append(
        " where network_id = :networkId and policy_type = :policyType and policy_source = :policySource ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);
    params.put("policyType", policyType);
    params.put("policySource", policySource);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  @Override
  public List<NetworkPolicyDO> queryNetworkPolicyByPolicyTypeAndPolicySource(String policyType,
      String policySource) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where  policy_type = :policyType and policy_source = :policySource ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyType", policyType);
    params.put("policySource", policySource);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
  }

  @Override
  public List<NetworkPolicyDO> queryNetworkPolicyByNetworkIdAndPolicySourceOfGroup(String networkId,
      String policyType) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where network_id = :networkId and policy_type = :policyType ");
    sql.append(" and policy_source <> '' and policy_source <> '0' ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);
    params.put("policyType", policyType);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? Lists.newArrayListWithCapacity(0) : policyList;
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
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#queryNetworkPolicy(java.lang.String)
   */
  @Override
  public NetworkPolicyDO queryNetworkPolicy(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<NetworkPolicyDO> policyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(policyList) ? new NetworkPolicyDO() : policyList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#queryNetworkPolicyByPolicyId(java.lang.String, java.lang.String)
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
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#queryAssignNetworkPolicyIds(java.util.Date)
   */
  @Override
  public List<String> queryAssignNetworkPolicyIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select assign_id from ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where assign_id != '' ");
    sql.append(" and timestamp < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#queryNetworkPolicyByAssignId(java.lang.String)
   */
  @Override
  public NetworkPolicyDO queryNetworkPolicyByAssignId(String assignId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where assign_id = :assignId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("assignId", assignId);

    List<NetworkPolicyDO> networkPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkPolicyDO.class));

    return CollectionUtils.isEmpty(networkPolicyList) ? new NetworkPolicyDO()
        : networkPolicyList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#updateNetworkPolicy(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int updateNetworkPolicy(NetworkPolicyDO networkPolicyDO) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(
        " set policy_id = :policyId, timestamp = :timestamp, operator_id = :operatorId, policy_source = :policySource ");
    sql.append(" where network_id = :networkId and policy_type = :policyType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyId", networkPolicyDO.getPolicyId());
    params.put("timestamp", DateUtils.now());
    params.put("operatorId", networkPolicyDO.getOperatorId());
    params.put("networkId", networkPolicyDO.getNetworkId());
    params.put("policyType", networkPolicyDO.getPolicyType());
    params.put("policySource", StringUtils.defaultIfEmpty(networkPolicyDO.getPolicySource(), ""));

    return jdbcTemplate.update(sql.toString(), params);
  }

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

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#saveNetworkPolicy(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public NetworkPolicyDO saveNetworkPolicy(NetworkPolicyDO networkPolicyDO) {

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(
        " (id, assign_id, network_id, policy_type, policy_id, timestamp, operator_id, policy_source) ");
    sql.append(
        " values (:id, :assignId, :networkId, :policyType, :policyId, :timestamp, :operatorId, :policySource) ");

    networkPolicyDO.setId(StringUtils.replace(IdGenerator.generateUUID(), "-", "_"));
    networkPolicyDO.setTimestamp(DateUtils.now());
    if (StringUtils.isBlank(networkPolicyDO.getAssignId())) {
      networkPolicyDO.setAssignId("");
    }

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(networkPolicyDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return networkPolicyDO;
  }

  @Override
  public List<NetworkPolicyDO> saveNetworkPolicy(List<NetworkPolicyDO> networkPolicyDOList,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(
        " (id, assign_id, network_id, policy_type, policy_id, timestamp, operator_id, policy_source) ");
    sql.append(
        " values (:id, :assignId, :networkId, :policyType, :policyId, :timestamp, :operatorId, :policySource) ");

    for (NetworkPolicyDO networkPolicyDO : networkPolicyDOList) {
      if (StringUtils.isBlank(networkPolicyDO.getId())) {
        networkPolicyDO.setId(IdGenerator.generateUUID());
      }
      if (StringUtils.isBlank(networkPolicyDO.getAssignId())) {
        networkPolicyDO.setAssignId("");
      }
      networkPolicyDO.setTimestamp(DateUtils.now());
      networkPolicyDO.setOperatorId(operatorId);
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(networkPolicyDOList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
    return CollectionUtils.isEmpty(networkPolicyDOList)
        ? Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE)
        : networkPolicyDOList;
  }

  @Override
  public void mergeNetworkPolicys(List<NetworkPolicyDO> policyList) {

    deleteNetworkPolicyByNetworkIdAndPolicySource(policyList.get(0).getNetworkId(),
        policyList.get(0).getPolicySource());

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" (id, network_id, policy_type, policy_id, policy_source, ");
    sql.append(" timestamp, operator_id) ");
    sql.append(" values (:id, :networkId, :policyType, :policyId, :policySource, ");
    sql.append(" :timestamp, :operatorId) ");

    policyList.forEach(policy -> {
      policy.setId(IdGenerator.generateUUID());
      policy.setTimestamp(DateUtils.now());
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(policyList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);

  }

  @Override
  public void deleteNetworkPolicyByNetworkIdAndPolicySource(String networkId, String policySource) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where network_id = :networkId ");
    sql.append(" and policy_source = :policySource ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);
    params.put("policySource", policySource);
    jdbcTemplate.update(sql.toString(), params);

  }

  @Override
  public void deleteNetworkPolicyByPolicyTypeAndPolicySource(String policyType, String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where policy_type = :policyType ");
    sql.append(" and policy_source = :policySource ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("policyType", policyType);
    params.put("policySource", id);
    jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#deleteNetworkPolicy(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteNetworkPolicy(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" set operator_id = :operatorId ");
    sql.append(" where id = :id ");

    NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
    networkPolicyDO.setOperatorId(operatorId);
    networkPolicyDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(networkPolicyDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao#deleteNetworkPolicyByPolicyId(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteNetworkPolicyByPolicyId(String policyId, String policyType) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_NETWORK_POLICY);
    sql.append(" where policy_id = :policyId ");
    if (StringUtils.isNotBlank(policyType)) {
      sql.append(" and policy_type = :policyType ");
      params.put("policyType", policyType);
    }
    params.put("policyId", policyId);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id, network_id, policy_type, policy_id, policy_source, ");
    sql.append(" timestamp, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_NETWORK_POLICY);

    return sql;
  }

}
