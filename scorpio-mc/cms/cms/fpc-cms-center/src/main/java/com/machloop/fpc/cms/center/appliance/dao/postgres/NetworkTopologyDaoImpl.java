package com.machloop.fpc.cms.center.appliance.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.NetworkTopologyDao;
import com.machloop.fpc.cms.center.appliance.data.NetworkTopologyDO;

/**
 * @author guosk
 *
 * create at 2021年7月10日, fpc-manager
 */
@Repository
public class NetworkTopologyDaoImpl implements NetworkTopologyDao {

  private static final String TABLE_APPLIANCE_NETWORK_TOPOLOGY = "fpccms_appliance_sensor_network_topology";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkTopologyDao#queryNetworkTopology()
   */
  @Override
  public NetworkTopologyDO queryNetworkTopology() {
    StringBuilder sql = buildSelectStatement();

    List<NetworkTopologyDO> networkTopologyList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(NetworkTopologyDO.class));

    return CollectionUtils.isEmpty(networkTopologyList) ? new NetworkTopologyDO()
        : networkTopologyList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkTopologyDao#queryNetworkTopologyByNetworkId(java.lang.String)
   */
  @Override
  public NetworkTopologyDO queryNetworkTopologyByNetworkId(String networkId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where topology like :networkId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("networkId", "%" + networkId + "%");

    List<NetworkTopologyDO> networkTopologyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetworkTopologyDO.class));

    return CollectionUtils.isEmpty(networkTopologyList) ? new NetworkTopologyDO()
        : networkTopologyList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.NetworkTopologyDao#saveOrUpdateNetworkTopology(com.machloop.fpc.cms.center.appliance.data.NetworkTopologyDO)
   */
  @Override
  public int saveOrUpdateNetworkTopology(NetworkTopologyDO networkTopologyDO) {
    int update = updateNetworkTopology(networkTopologyDO);
    return update > 0 ? update : saveNetworkTopology(networkTopologyDO);
  }

  private int saveNetworkTopology(NetworkTopologyDO networkTopologyDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_NETWORK_TOPOLOGY);
    sql.append(" (id, topology, metric, timestamp, operator_id) ");
    sql.append(" values (:id, :topology, :metric, :timestamp, :operatorId)");

    networkTopologyDO.setId(IdGenerator.generateUUID());
    networkTopologyDO.setTimestamp(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(networkTopologyDO);

    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private int updateNetworkTopology(NetworkTopologyDO networkTopologyDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NETWORK_TOPOLOGY);
    sql.append(" set topology = :topology, metric = :metric, timestamp = :timestamp, ");
    sql.append(" operator_id = :operatorId ");

    networkTopologyDO.setTimestamp(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(networkTopologyDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, topology, metric, timestamp, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_NETWORK_TOPOLOGY);

    return sql;
  }

}
