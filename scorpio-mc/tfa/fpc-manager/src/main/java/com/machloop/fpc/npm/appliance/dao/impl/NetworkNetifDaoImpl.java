package com.machloop.fpc.npm.appliance.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.npm.appliance.dao.NetworkNetifDao;
import com.machloop.fpc.npm.appliance.data.NetworkNetifDO;

/**
 * @author guosk
 *
 * create at 2020年11月11日, fpc-manager
 */
@Repository
public class NetworkNetifDaoImpl implements NetworkNetifDao {

  private static final String TABLE_APPLIANCE_NETWORK_NETIF = "fpc_appliance_network_netif";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkNetifDao#queryAllNetworkNetifs()
   */
  @Override
  public List<NetworkNetifDO> queryAllNetworkNetifs() {
    StringBuilder sql = buildSelectStatement();

    List<NetworkNetifDO> netifList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(NetworkNetifDO.class));

    return CollectionUtils.isEmpty(netifList) ? Lists.newArrayListWithCapacity(0) : netifList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkNetifDao#queryNetworkNetifs(java.lang.String)
   */
  @Override
  public List<NetworkNetifDO> queryNetworkNetifs(String networkId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where network_id = :networkId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);

    List<NetworkNetifDO> netifList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkNetifDO.class));

    return CollectionUtils.isEmpty(netifList) ? Lists.newArrayListWithCapacity(0) : netifList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkNetifDao#staticNetifUsageByNetwork()
   */
  @Override
  public List<Map<String, Object>> staticNetifUsageByNetwork() {
    StringBuilder sql = new StringBuilder();
    sql.append("select network_id as networkId, count(netif_name) as netifCount, ");
    sql.append(" sum(specification) as totalBandwidth ");
    sql.append(" from ").append(TABLE_APPLIANCE_NETWORK_NETIF);
    sql.append(" group by network_id ");

    List<Map<String, Object>> netifList = jdbcTemplate.queryForList(sql.toString(),
        Maps.newHashMapWithExpectedSize(0));

    return CollectionUtils.isEmpty(netifList) ? Lists.newArrayListWithCapacity(0) : netifList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkNetifDao#mergeNetworkNetifs(java.util.List)
   */
  @Override
  public void mergeNetworkNetifs(List<NetworkNetifDO> networkNetifs) {
    // delete
    deleteNetworkNetif(networkNetifs.get(0).getNetworkId());

    // batch insert
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_APPLIANCE_NETWORK_NETIF);
    sql.append(" (id, network_id, netif_name, specification, direction, ");
    sql.append(" timestamp, operator_id) ");
    sql.append(" values (:id, :networkId, :netifName, :specification, :direction, ");
    sql.append(" :timestamp, :operatorId) ");

    networkNetifs.forEach(networkNetif -> {
      networkNetif.setId(IdGenerator.generateUUID());
      networkNetif.setTimestamp(DateUtils.now());
    });
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(networkNetifs);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkNetifDao#deleteNetworkNetif(java.lang.String)
   */
  @Override
  public int deleteNetworkNetif(String networkId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_NETWORK_NETIF);
    sql.append(" where network_id = :networkId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, network_id, netif_name, specification, ");
    sql.append(" direction, timestamp, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_NETWORK_NETIF);

    return sql;
  }

}
