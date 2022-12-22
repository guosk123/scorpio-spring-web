package com.machloop.fpc.npm.appliance.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.npm.appliance.dao.NetworkDao;
import com.machloop.fpc.npm.appliance.data.NetworkDO;

/**
 * @author guosk
 *
 * create at 2020年11月10日, fpc-manager
 */
@Repository
public class NetworkDaoImpl implements NetworkDao {

  private static final String TABLE_APPLIANCE_NETWORK = "fpc_appliance_network";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkDao#queryNetworks()
   */
  @Override
  public List<NetworkDO> queryNetworks() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<NetworkDO> networkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkDO.class));

    return CollectionUtils.isEmpty(networkList) ? Lists.newArrayListWithCapacity(0) : networkList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkDao#queryNetworks(java.util.List)
   */
  @Override
  public List<NetworkDO> queryNetworks(List<String> ids) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id in (:ids) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("ids", ids);

    List<NetworkDO> networkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkDO.class));

    return CollectionUtils.isEmpty(networkList) ? Lists.newArrayListWithCapacity(0) : networkList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkDao#queryNetworksByReportState(java.lang.String)
   */
  @Override
  public List<NetworkDO> queryNetworksByReportState(String reportState) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where report_state = :reportState ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("reportState", reportState);

    List<NetworkDO> networkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkDO.class));

    return CollectionUtils.isEmpty(networkList) ? Lists.newArrayListWithCapacity(0) : networkList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkDao#queryNetwork(java.lang.String)
   */
  @Override
  public NetworkDO queryNetwork(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<NetworkDO> networkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkDO.class));

    return CollectionUtils.isEmpty(networkList) ? new NetworkDO() : networkList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkDao#queryNetworkByName(java.lang.String)
   */
  @Override
  public NetworkDO queryNetworkByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<NetworkDO> networkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkDO.class));

    return CollectionUtils.isEmpty(networkList) ? new NetworkDO() : networkList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkDao#saveNetwork(com.machloop.fpc.npm.appliance.data.NetworkDO)
   */
  @Override
  public NetworkDO saveNetwork(NetworkDO networkDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_NETWORK);
    sql.append(" (id, name, netif_type, extra_settings, report_state, report_action, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :netifType, :extraSettings, :reportState, :reportAction, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    networkDO.setId(StringUtils.replace(IdGenerator.generateUUID(), "-", "_"));
    networkDO.setCreateTime(DateUtils.now());
    networkDO.setUpdateTime(networkDO.getCreateTime());
    networkDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(networkDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return networkDO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkDao#updateNetwork(com.machloop.fpc.npm.appliance.data.NetworkDO)
   */
  @Override
  public int updateNetwork(NetworkDO networkDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NETWORK);
    sql.append(" set name = :name, netif_type = :netifType, ");
    sql.append(" extra_settings = :extraSettings, report_state = :reportState, ");
    sql.append(" report_action = :reportAction, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    networkDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(networkDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkDao#updateNetworkReportState(java.util.List, java.lang.String)
   */
  @Override
  public int updateNetworkReportState(List<String> networkIds, String reportState) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NETWORK);
    sql.append(" set report_state = :reportState ");
    sql.append(" where id in (:ids) ");

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("reportState", reportState);
    map.put("ids", networkIds);

    return jdbcTemplate.update(sql.toString(), map);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkDao#deleteNetwork(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteNetwork(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NETWORK);
    sql.append(" set report_state = :reportState, report_action = :reportAction, ");
    sql.append(" deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    NetworkDO networkDO = new NetworkDO();
    networkDO.setReportState(Constants.BOOL_NO);
    networkDO.setReportAction(FpcCmsConstants.SYNC_ACTION_DELETE);
    networkDO.setDeleted(Constants.BOOL_YES);
    networkDO.setDeleteTime(DateUtils.now());
    networkDO.setOperatorId(operatorId);
    networkDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(networkDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, netif_type, extra_settings, report_state, report_action, ");
    sql.append(" deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_NETWORK);

    return sql;
  }

}
