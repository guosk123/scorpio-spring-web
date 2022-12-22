package com.machloop.fpc.cms.center.sensor.dao.impl;

import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
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
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
@Repository
public class SensorNetworkDaoImpl implements SensorNetworkDao {

  private static final String TABLE_APPLIANCE_SENSOR_NETWORK = "fpccms_appliance_sensor_network";

  private static final String TABLE_FPC_NETWORK = "fpccms_central_fpc_network";
  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao#querySensorNetworks()
   */
  @Override
  public List<SensorNetworkDO> querySensorNetworks() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where sensor.deleted = :deleted and fpc.deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<SensorNetworkDO> sensorNetworkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorNetworkDO.class));

    return CollectionUtils.isEmpty(sensorNetworkList) ? Lists.newArrayListWithCapacity(0)
        : sensorNetworkList;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao#querySensorNetworksBySensorIdList(java.util.List)
   */
  @Override
  public List<SensorNetworkDO> querySensorNetworksBySensorIdList(List<String> sensorIdList) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where sensor.deleted = :deleted and fpc.deleted = :deleted ");
    sql.append(" and sensor.sensor_id in (:sensorIdList) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("sensorIdList", sensorIdList);

    List<SensorNetworkDO> sensorNetworkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorNetworkDO.class));

    return CollectionUtils.isEmpty(sensorNetworkList) ? Lists.newArrayListWithCapacity(0)
        : sensorNetworkList;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao#querySensorNetwork(java.lang.String)
   */
  @Override
  public SensorNetworkDO querySensorNetwork(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where sensor.deleted = :deleted and fpc.deleted = :deleted ");
    sql.append(" and sensor.id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<SensorNetworkDO> sensorNetworkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorNetworkDO.class));

    return CollectionUtils.isEmpty(sensorNetworkList) ? new SensorNetworkDO()
        : sensorNetworkList.get(0);
  }

  @Override
  public SensorNetworkDO querySensorNetworkByNetworkInSensorId(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where sensor.deleted = :deleted and fpc.deleted = :deleted ");
    sql.append(" and sensor.network_in_sensor_id = :networkInSensorId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("networkInSensorId", id);

    List<SensorNetworkDO> sensorNetworkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorNetworkDO.class));

    return CollectionUtils.isEmpty(sensorNetworkList) ? new SensorNetworkDO()
        : sensorNetworkList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao#saveSensorNetwork(com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO)
   */
  @Override
  public SensorNetworkDO saveSensorNetwork(SensorNetworkDO sensorNetworkDO) {

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SENSOR_NETWORK);
    sql.append(" (id, name, sensor_id, sensor_name, sensor_type, network_in_sensor_id, ");
    sql.append(" owner, deleted, description, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :sensorId, :sensorName, :sensorType, :networkInSensorId, ");
    sql.append(" :owner, :deleted, :description, :createTime, :updateTime, :operatorId ) ");

    sensorNetworkDO.setId(IdGenerator.generateUUID());
    sensorNetworkDO.setCreateTime(DateUtils.now());
    sensorNetworkDO.setUpdateTime(sensorNetworkDO.getCreateTime());
    sensorNetworkDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sensorNetworkDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return sensorNetworkDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao#batchSaveSensorNetworks(java.util.List)
   */
  @Override
  public void batchSaveSensorNetworks(List<SensorNetworkDO> sensorNetworkDOList) {

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_APPLIANCE_SENSOR_NETWORK);
    sql.append(" (id, name, sensor_id, sensor_name, sensor_type, network_in_sensor_id, ");
    sql.append(" owner, description, create_time, operator_id) ");
    sql.append(" values (:id, :name, :sensorId, :sensorName, :sensorType, :networkInSensorId, ");
    sql.append(" :owner, :description, :createTime, :operatorId) ");

    sensorNetworkDOList.forEach(sensorNetworkDO -> {
      sensorNetworkDO.setId(IdGenerator.generateUUID());
      sensorNetworkDO.setCreateTime(DateUtils.now());
    });
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(sensorNetworkDOList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao#updateSensorNetworks(java.lang.String, com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO, java.lang.String)
   */
  @Override
  public int updateSensorNetwork(String id, SensorNetworkDO sensorNetworkDO, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SENSOR_NETWORK);
    sql.append(" set name = :name, update_time = :updateTime, ");
    sql.append(" description = :description, operator_id = :operatorId ");
    sql.append(" where network_in_sensor_id = :networkInSensorId");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("name", sensorNetworkDO.getName());
    params.put("updateTime", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("description", sensorNetworkDO.getDescription());
    params.put("networkInSensorId", sensorNetworkDO.getNetworkInSensorId());

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao#deleteSensorNetwork(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteSensorNetwork(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SENSOR_NETWORK);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    SensorNetworkDO sensorNetworkDO = new SensorNetworkDO();
    sensorNetworkDO.setDeleted(Constants.BOOL_YES);
    sensorNetworkDO.setDeleteTime(DateUtils.now());
    sensorNetworkDO.setOperatorId(operatorId);
    sensorNetworkDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sensorNetworkDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao#deleteSensorNetworkByFpcNetworkId(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteSensorNetworkByFpcNetworkId(String networkId, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SENSOR_NETWORK);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where network_in_sensor_id = :networkInSensorId ");

    SensorNetworkDO sensorNetworkDO = new SensorNetworkDO();
    sensorNetworkDO.setDeleted(Constants.BOOL_YES);
    sensorNetworkDO.setDeleteTime(DateUtils.now());
    sensorNetworkDO.setOperatorId(operatorId);
    sensorNetworkDO.setNetworkInSensorId(networkId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sensorNetworkDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append(" select sensor.id, sensor.name, fpc.fpc_network_name as network_in_sensor_name, ");
    sql.append(" fpc.bandwidth, sensor.sensor_id, sensor.sensor_name, sensor.sensor_type, ");
    sql.append(" sensor.network_in_sensor_id, sensor.owner, sensor.description, sensor.deleted, ");
    sql.append(" sensor.create_time, sensor.update_time, sensor.delete_time, sensor.operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SENSOR_NETWORK).append(" as sensor ");
    sql.append(" left join ").append(TABLE_FPC_NETWORK).append(" as fpc ");
    sql.append(" on sensor.network_in_sensor_id = fpc.fpc_network_id ");

    return sql;
  }
}
