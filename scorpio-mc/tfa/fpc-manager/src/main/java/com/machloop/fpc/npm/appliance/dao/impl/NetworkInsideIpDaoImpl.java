package com.machloop.fpc.npm.appliance.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.machloop.fpc.npm.appliance.dao.NetworkInsideIpDao;
import com.machloop.fpc.npm.appliance.data.NetworkInsideIpDO;

/**
 * @author guosk
 *
 * create at 2020年11月11日, fpc-manager
 */
@Repository
public class NetworkInsideIpDaoImpl implements NetworkInsideIpDao {

  private static final String TABLE_APPLIANCE_NETWORK_INSIDE_IP = "fpc_appliance_network_inside_ip";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkInsideIpDao#queryNetworkInsideIps(java.lang.String)
   */
  @Override
  public List<NetworkInsideIpDO> queryNetworkInsideIps(String networkId) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, network_id, ip_address, ip_start, ip_end, ");
    sql.append(" timestamp, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_NETWORK_INSIDE_IP);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(networkId)) {
      sql.append(" where network_id = :networkId ");
      params.put("networkId", networkId);
    }

    List<NetworkInsideIpDO> networkIpList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkInsideIpDO.class));

    return CollectionUtils.isEmpty(networkIpList) ? Lists.newArrayListWithCapacity(0)
        : networkIpList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkInsideIpDao#mergeNetworkInsideIps(java.util.List)
   */
  @Override
  public void mergeNetworkInsideIps(List<NetworkInsideIpDO> networkInsideIps) {
    // delete
    deleteNetworkInsideIp(networkInsideIps.get(0).getNetworkId());

    // batch insert
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_APPLIANCE_NETWORK_INSIDE_IP);
    sql.append(" (id, network_id, ip_address, ip_start, ip_end, ");
    sql.append(" timestamp, operator_id) ");
    sql.append(" values (:id, :networkId, :ipAddress, :ipStart, :ipEnd, ");
    sql.append(" :timestamp, :operatorId) ");

    networkInsideIps.forEach(networkInsideIp -> {
      networkInsideIp.setId(IdGenerator.generateUUID());
      networkInsideIp.setTimestamp(DateUtils.now());
    });
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(networkInsideIps);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetworkInsideIpDao#deleteNetworkInsideIp(java.lang.String)
   */
  @Override
  public int deleteNetworkInsideIp(String networkId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_NETWORK_INSIDE_IP);
    sql.append(" where network_id = :networkId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", networkId);
    return jdbcTemplate.update(sql.toString(), params);
  }

}
