package com.machloop.fpc.cms.center.sensor.dao.impl;

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
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
@Repository
public class SensorNetworkGroupDaoImpl implements SensorNetworkGroupDao {

  private static final String TABLE_APPLIANCE_SENSOR_NETWORK_GROUP = "fpccms_appliance_sensor_network_group";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao#querySensorNetworkGroups()
   */
  @Override
  public List<SensorNetworkGroupDO> querySensorNetworkGroups() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<SensorNetworkGroupDO> sensorNetworkGroupsList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorNetworkGroupDO.class));

    return CollectionUtils.isEmpty(sensorNetworkGroupsList) ? Lists.newArrayListWithCapacity(0)
        : sensorNetworkGroupsList;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao#querySensorNetworkGroupsByNetwork(java.lang.String)
   */
  @Override
  public List<SensorNetworkGroupDO> querySensorNetworkGroupsByNetwork(String networkId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and network_in_sensor_ids like :networkId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("networkId", "%" + networkId + "%");

    List<SensorNetworkGroupDO> sensorNetworkGroupsList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorNetworkGroupDO.class));

    return CollectionUtils.isEmpty(sensorNetworkGroupsList) ? Lists.newArrayListWithCapacity(0)
        : sensorNetworkGroupsList;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao#querySensorNetworkGroup(java.lang.String)
   */
  @Override
  public SensorNetworkGroupDO querySensorNetworkGroup(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<SensorNetworkGroupDO> sensorNetworkGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorNetworkGroupDO.class));

    return CollectionUtils.isEmpty(sensorNetworkGroupList) ? new SensorNetworkGroupDO()
        : sensorNetworkGroupList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao#queryNetworkGroupsByName(java.lang.String)
   */
  @Override
  public SensorNetworkGroupDO querySensorNetworkGroupByName(String networkGroupsName) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", networkGroupsName);

    List<SensorNetworkGroupDO> sensorNetworkGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SensorNetworkGroupDO.class));

    return CollectionUtils.isEmpty(sensorNetworkGroupList) ? new SensorNetworkGroupDO()
        : sensorNetworkGroupList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao#saveSensorNetworkGroup(com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO)
   */
  @Override
  public SensorNetworkGroupDO saveSensorNetworkGroup(SensorNetworkGroupDO sensorNetworkGroupDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SENSOR_NETWORK_GROUP);
    sql.append(" (id, name, network_in_sensor_ids, ");
    sql.append(" deleted, create_time, update_time, description, operator_id ) ");
    sql.append(" values (:id, :name, :networkInSensorIds, ");
    sql.append(" :deleted, :createTime, :updateTime, :description, :operatorId ) ");

    sensorNetworkGroupDO.setId(StringUtils.replace(IdGenerator.generateUUID(), "-", "_"));
    sensorNetworkGroupDO.setCreateTime(DateUtils.now());
    sensorNetworkGroupDO.setUpdateTime(sensorNetworkGroupDO.getCreateTime());
    sensorNetworkGroupDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sensorNetworkGroupDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return sensorNetworkGroupDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao#updateSensorNetworkGroups(java.lang.String, com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO, java.lang.String)
   */
  @Override
  public int updateSensorNetworkGroup(String id, SensorNetworkGroupDO sensorNetworkGroupsDO,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SENSOR_NETWORK_GROUP);
    sql.append(
        " set name = :name, update_time = :updateTime, network_in_sensor_ids = :networkInSensorIds, ");
    sql.append(" description = :description, operator_id = :operatorId ");
    sql.append(" where id = :id");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("name", sensorNetworkGroupsDO.getName());
    params.put("networkInSensorIds", sensorNetworkGroupsDO.getNetworkInSensorIds());
    params.put("updateTime", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("description", sensorNetworkGroupsDO.getDescription());
    params.put("id", id);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao#deleteSensorNetworkGroups(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteSensorNetworkGroup(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SENSOR_NETWORK_GROUP);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    SensorNetworkGroupDO sensorNetworkGroupDO = new SensorNetworkGroupDO();
    sensorNetworkGroupDO.setDeleted(Constants.BOOL_YES);
    sensorNetworkGroupDO.setDeleteTime(DateUtils.now());
    sensorNetworkGroupDO.setOperatorId(operatorId);
    sensorNetworkGroupDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sensorNetworkGroupDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append(
        " select id, name, network_in_sensor_ids, description, deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SENSOR_NETWORK_GROUP);

    return sql;
  }

}
