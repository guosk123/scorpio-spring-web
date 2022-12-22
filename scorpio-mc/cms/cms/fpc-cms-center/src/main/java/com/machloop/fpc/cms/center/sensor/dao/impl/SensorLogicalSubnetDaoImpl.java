package com.machloop.fpc.cms.center.sensor.dao.impl;

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
import org.springframework.stereotype.Repository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月26日, fpc-cms-center
 */
@Repository
public class SensorLogicalSubnetDaoImpl implements SensorLogicalSubnetDao {

  private static final String TABLE_APPLIANCE_LOGICAL_SUBNET = "fpccms_appliance_sensor_logical_subnet";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#queryLogicalSubnets()
   */
  @Override
  public List<SensorLogicalSubnetDO> querySensorLogicalSubnets() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<SensorLogicalSubnetDO> sensorLogicalSubnetsList = jdbcTemplate.query(sql.toString(),
        params, new BeanPropertyRowMapper<>(SensorLogicalSubnetDO.class));

    return CollectionUtils.isEmpty(sensorLogicalSubnetsList) ? Lists.newArrayListWithCapacity(0)
        : sensorLogicalSubnetsList;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#querySensorLogicalSubnetsByNetwork(java.lang.String)
   */
  @Override
  public List<SensorLogicalSubnetDO> querySensorLogicalSubnetsByNetwork(String networkId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and network_in_sensor_ids like :networkId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("networkId", "%" + networkId + "%");

    List<SensorLogicalSubnetDO> sensorLogicalSubnetsList = jdbcTemplate.query(sql.toString(),
        params, new BeanPropertyRowMapper<>(SensorLogicalSubnetDO.class));

    return CollectionUtils.isEmpty(sensorLogicalSubnetsList) ? Lists.newArrayListWithCapacity(0)
        : sensorLogicalSubnetsList;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#queryAssignLogicalSubnets(java.util.Date)
   */
  @Override
  public List<SensorLogicalSubnetDO> queryAssignLogicalSubnets(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<SensorLogicalSubnetDO> sensorLogicalSubnetsList = jdbcTemplate.query(sql.toString(),
        params, new BeanPropertyRowMapper<>(SensorLogicalSubnetDO.class));

    return CollectionUtils.isEmpty(sensorLogicalSubnetsList) ? Lists.newArrayListWithCapacity(0)
        : sensorLogicalSubnetsList;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#querySensorLogicalSubnets(java.util.Date)
   */
  @Override
  public List<SensorLogicalSubnetDO> querySensorLogicalSubnets(Date beforeTime) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<SensorLogicalSubnetDO> sensorLogicalSubnetsList = jdbcTemplate.query(sql.toString(),
        params, new BeanPropertyRowMapper<>(SensorLogicalSubnetDO.class));

    return CollectionUtils.isEmpty(sensorLogicalSubnetsList) ? Lists.newArrayListWithCapacity(0)
        : sensorLogicalSubnetsList;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#querySensorLogicalSubnetIds(java.util.Date)
   */
  @Override
  public List<String> querySensorLogicalSubnetIds(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and assign_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#queryAssignLogicalSubnetIds(java.util.Date)
   */
  @Override
  public List<String> queryAssignLogicalSubnetIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select assign_id from ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#queryLogicalSubnetByAssignId(java.lang.String)
   */
  @Override
  public SensorLogicalSubnetDO queryLogicalSubnetByAssignId(String assignId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and assign_id = :assignId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignId", assignId);

    List<SensorLogicalSubnetDO> logicalSubnetList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorLogicalSubnetDO.class));

    return CollectionUtils.isEmpty(logicalSubnetList) ? new SensorLogicalSubnetDO()
        : logicalSubnetList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#queryLogicalSubnet(java.lang.String)
   */
  @Override
  public SensorLogicalSubnetDO querySensorLogicalSubnet(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<SensorLogicalSubnetDO> sensorLogicalSubnetsList = jdbcTemplate.query(sql.toString(),
        params, new BeanPropertyRowMapper<>(SensorLogicalSubnetDO.class));

    return CollectionUtils.isEmpty(sensorLogicalSubnetsList) ? new SensorLogicalSubnetDO()
        : sensorLogicalSubnetsList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#queryLogicalSubnetByName(java.lang.String)
   */
  @Override
  public SensorLogicalSubnetDO querySensorLogicalSubnetByName(String sensorLogicalSubnetName) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", sensorLogicalSubnetName);

    List<SensorLogicalSubnetDO> sensorLogicalSubnetsList = jdbcTemplate.query(sql.toString(),
        params, new BeanPropertyRowMapper<>(SensorLogicalSubnetDO.class));

    return CollectionUtils.isEmpty(sensorLogicalSubnetsList) ? new SensorLogicalSubnetDO()
        : sensorLogicalSubnetsList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#saveOrRecoverSensorLogicalSubnet(com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO)
   */
  @Override
  public SensorLogicalSubnetDO saveOrRecoverSensorLogicalSubnet(
      SensorLogicalSubnetDO sensorLogicalSubnetDO) {
    SensorLogicalSubnetDO exist = querySensorLogicalSubnetById(
        sensorLogicalSubnetDO.getId() == null ? "" : sensorLogicalSubnetDO.getId());
    if (StringUtils.isBlank(exist == null ? "" : exist.getId())) {
      return saveSensorLogicalSubnet(sensorLogicalSubnetDO);
    } else {
      recoverAndUpdateSensorLogicalSubnet(sensorLogicalSubnetDO);
      return querySensorLogicalSubnetById(sensorLogicalSubnetDO.getId());
    }
  }

  private SensorLogicalSubnetDO querySensorLogicalSubnetById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<SensorLogicalSubnetDO> logicalSubnetList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorLogicalSubnetDO.class));

    return CollectionUtils.isEmpty(logicalSubnetList) ? new SensorLogicalSubnetDO()
        : logicalSubnetList.get(0);
  }

  private int recoverAndUpdateSensorLogicalSubnet(SensorLogicalSubnetDO sensorLogicalSubnetDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(
        " set name = :name, type = :type, configuration = :configuration, update_time = :updateTime, ");
    sql.append(
        " network_in_sensor_ids = :networkInSensorIds, bandwidth = :bandwidth, operator_id = :operatorId, ");
    sql.append(" deleted = :deleted, delete_time = :deleteTime ");
    sql.append(" where id = :id");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("name", sensorLogicalSubnetDO.getName());
    params.put("type", sensorLogicalSubnetDO.getType());
    params.put("configuration", sensorLogicalSubnetDO.getConfiguration());
    params.put("updateTime", DateUtils.now());
    params.put("networkInSensorIds", sensorLogicalSubnetDO.getNetworkInSensorIds());
    params.put("bandwidth", sensorLogicalSubnetDO.getBandwidth());
    params.put("operatorId", sensorLogicalSubnetDO.getOperatorId());
    params.put("id", sensorLogicalSubnetDO.getId());
    params.put("deleted", Constants.BOOL_NO);
    params.put("deleteTime", null);

    return jdbcTemplate.update(sql.toString(), params);
  }

  private SensorLogicalSubnetDO saveSensorLogicalSubnet(
      SensorLogicalSubnetDO sensorLogicalSubnetDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" (id, assign_id, name, type, network_in_sensor_ids, configuration, bandwidth, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(
        " values (:id, :assignId, :name, :type, :networkInSensorIds, :configuration, :bandwidth, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(sensorLogicalSubnetDO.getId())) {
      sensorLogicalSubnetDO.setId(StringUtils.replace(IdGenerator.generateUUID(), "-", "_"));
    }
    if (StringUtils.isBlank(sensorLogicalSubnetDO.getAssignId())) {
      sensorLogicalSubnetDO.setAssignId("");
    }
    sensorLogicalSubnetDO.setCreateTime(DateUtils.now());
    sensorLogicalSubnetDO.setUpdateTime(sensorLogicalSubnetDO.getCreateTime());
    sensorLogicalSubnetDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sensorLogicalSubnetDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return sensorLogicalSubnetDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#updateLogicalSubnet(java.lang.String, com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO, java.lang.String)
   */
  @Override
  public int updateSensorLogicalSubnet(String id, SensorLogicalSubnetDO sensorLogicalSubnetDO,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(
        " set name = :name, type = :type, configuration = :configuration, update_time = :updateTime, ");
    sql.append(
        " network_in_sensor_ids = :networkInSensorIds, bandwidth = :bandwidth, operator_id = :operatorId ");
    sql.append(" where id = :id");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("name", sensorLogicalSubnetDO.getName());
    params.put("type", sensorLogicalSubnetDO.getType());
    params.put("configuration", sensorLogicalSubnetDO.getConfiguration());
    params.put("updateTime", DateUtils.now());
    params.put("networkInSensorIds", sensorLogicalSubnetDO.getNetworkInSensorIds());
    params.put("bandwidth", sensorLogicalSubnetDO.getBandwidth());
    params.put("operatorId", operatorId);
    params.put("id", id);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao#deleteLogicalSubnet(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteSensorLogicalSubnet(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    SensorLogicalSubnetDO sensorLogicalSubnetDO = new SensorLogicalSubnetDO();
    sensorLogicalSubnetDO.setDeleted(Constants.BOOL_YES);
    sensorLogicalSubnetDO.setDeleteTime(DateUtils.now());
    sensorLogicalSubnetDO.setOperatorId(operatorId);
    sensorLogicalSubnetDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sensorLogicalSubnetDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append(" select id, assign_id, name, type, configuration, network_in_sensor_ids, ");
    sql.append(" bandwidth, deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);

    return sql;

  }
}
