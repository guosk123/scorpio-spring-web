package com.machloop.fpc.cms.center.sensor.dao.impl;

import java.util.Arrays;
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
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkPermDao;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkPermDO;

/**
 * @author guosk
 *
 * create at 2022年3月10日, fpc-cms-center
 */
@Repository
public class SensorNetworkPermDaoImpl implements SensorNetworkPermDao {

  private static final String TABLE_APPLIANCE_SENSOR_NETWORK_PERM = "fpccms_appliance_sensor_network_perm";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkPermDao#querySensorNetworkPerms(java.util.List)
   */
  @Override
  public List<SensorNetworkPermDO> querySensorNetworkPerms(List<String> userIds) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, user_id, network_id, network_group_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SENSOR_NETWORK_PERM);
    sql.append(" where user_id in (:userIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("userIds", userIds);

    List<SensorNetworkPermDO> sensorNetworkPerms = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorNetworkPermDO.class));

    return CollectionUtils.isEmpty(sensorNetworkPerms) ? Lists.newArrayListWithCapacity(0)
        : sensorNetworkPerms;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkPermDao#updateSensorNetworkPerms(java.util.List)
   */
  @Override
  public int updateSensorNetworkPerms(List<SensorNetworkPermDO> sensorNetworkPerms) {
    // 删除原网络权限
    deleteSensorNetworkPermByUser(sensorNetworkPerms.get(0).getUserId());

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SENSOR_NETWORK_PERM);
    sql.append(" (id, user_id, network_id, network_group_id) ");
    sql.append(" values(:id, :userId, :networkId, :networkGroupId)");

    sensorNetworkPerms.forEach(sensorNetworkPerm -> {
      sensorNetworkPerm.setId(IdGenerator.generateUUID());
    });

    SqlParameterSource[] sqlParameterSources = SqlParameterSourceUtils
        .createBatch(sensorNetworkPerms);
    return Arrays.stream(jdbcTemplate.batchUpdate(sql.toString(), sqlParameterSources)).sum();
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkPermDao#deleteSensorNetworkPermByUser(java.lang.String)
   */
  @Override
  public int deleteSensorNetworkPermByUser(String userId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_SENSOR_NETWORK_PERM);
    sql.append(" where user_id = :userId ");

    Map<String, Object> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    param.put("userId", userId);

    return jdbcTemplate.update(sql.toString(), param);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkPermDao#deleteSensorNetworkPermByNetwork(java.lang.String)
   */
  @Override
  public int deleteSensorNetworkPermByNetwork(String networkId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_SENSOR_NETWORK_PERM);
    sql.append(" where network_id = :networkId ");

    Map<String, Object> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    param.put("networkId", networkId);

    return jdbcTemplate.update(sql.toString(), param);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkPermDao#deleteSensorNetworkPermByNetworkGroup(java.lang.String)
   */
  @Override
  public int deleteSensorNetworkPermByNetworkGroup(String networkGroupId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_SENSOR_NETWORK_PERM);
    sql.append(" where network_group_id = :networkGroupId ");

    Map<String, Object> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    param.put("networkGroupId", networkGroupId);

    return jdbcTemplate.update(sql.toString(), param);
  }

}
