package com.machloop.fpc.npm.appliance.dao.impl;

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
import com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao;
import com.machloop.fpc.npm.appliance.data.LogicalSubnetDO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
@Repository
public class LogicalSubnetDaoImpl implements LogicalSubnetDao {

  private static final String TABLE_APPLIANCE_LOGICAL_SUBNET = "fpc_appliance_logical_subnet";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#queryLogicalSubnets()
   */
  @Override
  public List<LogicalSubnetDO> queryLogicalSubnets() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<LogicalSubnetDO> logicalSubnetList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(LogicalSubnetDO.class));

    return CollectionUtils.isEmpty(logicalSubnetList) ? Lists.newArrayListWithCapacity(0)
        : logicalSubnetList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#queryAssignLogicalSubnets(java.util.Date)
   */
  @Override
  public List<LogicalSubnetDO> queryAssignLogicalSubnets(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, network_id, subnet_in_cms_id from ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" where deleted = :deleted and subnet_in_cms_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<LogicalSubnetDO> subnetList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(LogicalSubnetDO.class));
    return CollectionUtils.isEmpty(subnetList) ? Lists.newArrayListWithCapacity(0) : subnetList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#queryLogicalSubnetIds(java.util.Date, boolean)
   */
  @Override
  public List<String> queryLogicalSubnetIds(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and subnet_in_cms_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#queryLogicalSubnet(java.lang.String)
   */
  @Override
  public LogicalSubnetDO queryLogicalSubnet(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<LogicalSubnetDO> logicalSubnetList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(LogicalSubnetDO.class));

    return CollectionUtils.isEmpty(logicalSubnetList) ? new LogicalSubnetDO()
        : logicalSubnetList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#queryLogicalSubnetByCmsSubnetId(java.lang.String, java.lang.String)
   */
  @Override
  public LogicalSubnetDO queryLogicalSubnetByCmsSubnetId(String cmsSubnetId, String networkId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and network_id = :networkId ");
    sql.append(" and subnet_in_cms_id = :cmsSubnetId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("networkId", networkId);
    params.put("cmsSubnetId", cmsSubnetId);

    List<LogicalSubnetDO> logicalSubnetList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(LogicalSubnetDO.class));

    return CollectionUtils.isEmpty(logicalSubnetList) ? new LogicalSubnetDO()
        : logicalSubnetList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#queryLogicalSubnetByName(java.lang.String)
   */
  @Override
  public LogicalSubnetDO queryLogicalSubnetByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<LogicalSubnetDO> logicalSubnetList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(LogicalSubnetDO.class));

    return CollectionUtils.isEmpty(logicalSubnetList) ? new LogicalSubnetDO()
        : logicalSubnetList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#queryLogicalSubnetByNetworkId(java.lang.String)
   */
  @Override
  public List<LogicalSubnetDO> queryLogicalSubnetByNetworkId(String networkId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and network_id = :networkId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("networkId", networkId);

    List<LogicalSubnetDO> logicalSubnetList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(LogicalSubnetDO.class));

    return CollectionUtils.isEmpty(logicalSubnetList) ? Lists.newArrayListWithCapacity(0)
        : logicalSubnetList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#saveOrRecoverLogicalSubnet(com.machloop.fpc.npm.appliance.data.LogicalSubnetDO)
   */
  @Override
  public LogicalSubnetDO saveOrRecoverLogicalSubnet(LogicalSubnetDO logicalSubnetDO) {
    LogicalSubnetDO exist = queryLogicalSubnetById(
        logicalSubnetDO.getId() == null ? "" : logicalSubnetDO.getId());
    if (StringUtils.isBlank(exist == null ? "" : exist.getId())) {
      return saveLogicalSubnet(logicalSubnetDO);
    } else {
      recoverAndUpdateLogicalSubnet(logicalSubnetDO);
      return queryLogicalSubnetById(logicalSubnetDO.getId());
    }
  }

  private LogicalSubnetDO queryLogicalSubnetById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<LogicalSubnetDO> logicalSubnetList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(LogicalSubnetDO.class));

    return CollectionUtils.isEmpty(logicalSubnetList) ? new LogicalSubnetDO()
        : logicalSubnetList.get(0);
  }

  private int recoverAndUpdateLogicalSubnet(LogicalSubnetDO logicalSubnetDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" set name = :name, network_id = :networkId, bandwidth = :bandwidth, ");
    sql.append(" type = :type, configuration = :configuration, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId, deleted = :deleted, ");
    sql.append(" delete_time = :deleteTime ");
    sql.append(" where id = :id ");

    logicalSubnetDO.setUpdateTime(DateUtils.now());
    logicalSubnetDO.setDeleted(Constants.BOOL_NO);
    logicalSubnetDO.setDeleteTime(null);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(logicalSubnetDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private LogicalSubnetDO saveLogicalSubnet(LogicalSubnetDO logicalSubnetDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" (id, name, network_id, bandwidth, type, configuration, subnet_in_cms_id, ");
    sql.append(" description, deleted, create_time, update_time, operator_id ) ");
    sql.append(
        " values (:id, :name, :networkId, :bandwidth, :type, :configuration, :subnetInCmsId, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(logicalSubnetDO.getId())) {
      logicalSubnetDO.setId(StringUtils.replace(IdGenerator.generateUUID(), "-", "_"));
    }
    if (StringUtils.isBlank(logicalSubnetDO.getSubnetInCmsId())) {
      logicalSubnetDO.setSubnetInCmsId("");
    }

    logicalSubnetDO.setCreateTime(DateUtils.now());
    logicalSubnetDO.setUpdateTime(logicalSubnetDO.getCreateTime());
    logicalSubnetDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(logicalSubnetDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return logicalSubnetDO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#updateLogicalSubnet(com.machloop.fpc.npm.appliance.data.LogicalSubnetDO)
   */
  @Override
  public int updateLogicalSubnet(LogicalSubnetDO logicalSubnetDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" set name = :name, network_id = :networkId, bandwidth = :bandwidth, ");
    sql.append(" type = :type, configuration = :configuration, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    logicalSubnetDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(logicalSubnetDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#deleteLogicalSubnet(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteLogicalSubnet(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    LogicalSubnetDO logicalSubnetDO = new LogicalSubnetDO();
    logicalSubnetDO.setDeleted(Constants.BOOL_YES);
    logicalSubnetDO.setDeleteTime(DateUtils.now());
    logicalSubnetDO.setOperatorId(operatorId);
    logicalSubnetDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(logicalSubnetDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao#deleteLogicalSubnetByNetworkId(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteLogicalSubnetByNetworkId(String networkId, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where network_id = :networkId ");

    LogicalSubnetDO logicalSubnetDO = new LogicalSubnetDO();
    logicalSubnetDO.setDeleted(Constants.BOOL_YES);
    logicalSubnetDO.setDeleteTime(DateUtils.now());
    logicalSubnetDO.setOperatorId(operatorId);
    logicalSubnetDO.setNetworkId(networkId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(logicalSubnetDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, network_id, bandwidth, type, configuration, subnet_in_cms_id, ");
    sql.append(" description, deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_LOGICAL_SUBNET);

    return sql;
  }

}
