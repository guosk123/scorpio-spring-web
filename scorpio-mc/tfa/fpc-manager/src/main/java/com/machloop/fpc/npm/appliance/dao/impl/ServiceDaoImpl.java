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
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.npm.appliance.dao.ServiceDao;
import com.machloop.fpc.npm.appliance.data.ServiceDO;

/**
 * @author guosk
 *
 * create at 2020年11月12日, fpc-manager
 */

@Repository
public class ServiceDaoImpl implements ServiceDao {

  private static final String TABLE_APPLIANCE_SERVICE = "fpc_appliance_service";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceDao#queryServices(com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<ServiceDO> queryServices(Pageable page, String name) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and name like :name ");
      params.put("name", "%" + name + "%");
    }
    sql.append(whereSql);

    PageUtils.appendPage(sql, page, ServiceDO.class);

    List<ServiceDO> serviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_SERVICE);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(serviceList, page, total);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceDao#queryServices(java.lang.String)
   */
  @Override
  public List<ServiceDO> queryServices(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(name)) {
      sql.append(" and name like :name ");
      params.put("name", "%" + name + "%");
    }

    List<ServiceDO> serviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceDO.class));

    return CollectionUtils.isEmpty(serviceList) ? Lists.newArrayListWithCapacity(0) : serviceList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceDao#queryAssignServices(java.util.Date)
   */
  @Override
  public List<ServiceDO> queryAssignServiceIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, service_in_cms_id from ").append(TABLE_APPLIANCE_SERVICE);
    sql.append(" where deleted = :deleted and service_in_cms_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<ServiceDO> serviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceDO.class));

    return CollectionUtils.isEmpty(serviceList) ? Lists.newArrayListWithCapacity(0) : serviceList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceDao#queryServiceIds(java.util.Date, boolean)
   */
  @Override
  public List<String> queryServiceIds(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_SERVICE);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and service_in_cms_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceDao#queryService(java.lang.String)
   */
  @Override
  public ServiceDO queryService(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<ServiceDO> serviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceDO.class));

    return CollectionUtils.isEmpty(serviceList) ? new ServiceDO() : serviceList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceDao#queryServiceByName(java.lang.String)
   */
  @Override
  public ServiceDO queryServiceByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<ServiceDO> serviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceDO.class));

    return CollectionUtils.isEmpty(serviceList) ? new ServiceDO() : serviceList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceDao#queryServiceByCmsServiceId(java.lang.String)
   */
  @Override
  public ServiceDO queryServiceByCmsServiceId(String cmsServiceId) {

    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and service_in_cms_id = :cmsServiceId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("cmsServiceId", cmsServiceId);

    List<ServiceDO> serviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceDO.class));

    return CollectionUtils.isEmpty(serviceList) ? new ServiceDO() : serviceList.get(0);
  }

  @Override
  public ServiceDO saveOrRecoverService(ServiceDO serviceDO) {
    ServiceDO exist = queryServiceById(serviceDO.getId() == null ? "" : serviceDO.getId());
    if (StringUtils.isBlank(exist == null ? "" : exist.getId())) {
      return saveService(serviceDO);
    } else {
      recoverAndUpdateService(serviceDO);
      return queryServiceById(serviceDO.getId());
    }
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceDao#batchSaveService(java.util.List)
   */
  @Override
  public int batchSaveService(List<ServiceDO> services) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SERVICE);
    sql.append(" (id, name, application, description, service_in_cms_id, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :application, :description, :serviceInCmsId, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    services.forEach(serviceDO -> {
      serviceDO.setCreateTime(DateUtils.now());
      serviceDO.setUpdateTime(serviceDO.getCreateTime());
      serviceDO.setDeleted(Constants.BOOL_NO);
      if (StringUtils.isBlank(serviceDO.getServiceInCmsId())) {
        serviceDO.setServiceInCmsId("");
      }
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(services);
    return jdbcTemplate.batchUpdate(sql.toString(), batchSource)[0];
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceDao#updateService(com.machloop.fpc.npm.appliance.data.ServiceDO)
   */
  @Override
  public int updateService(ServiceDO serviceDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SERVICE);
    sql.append(" set name = :name, application = :application, description =:description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    serviceDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(serviceDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceDao#deleteService(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteService(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SERVICE);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ServiceDO serviceDO = new ServiceDO();
    serviceDO.setDeleted(Constants.BOOL_YES);
    serviceDO.setDeleteTime(DateUtils.now());
    serviceDO.setOperatorId(operatorId);
    serviceDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(serviceDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private int recoverAndUpdateService(ServiceDO serviceDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SERVICE);
    sql.append(" set name = :name, application = :application, description =:description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId, deleted = :deleted, ");
    sql.append(" delete_time = :deleteTime ");
    sql.append(" where id = :id ");

    serviceDO.setUpdateTime(DateUtils.now());
    serviceDO.setDeleted(Constants.BOOL_NO);
    serviceDO.setDeleteTime(null);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(serviceDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private ServiceDO saveService(ServiceDO serviceDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SERVICE);
    sql.append(" (id, name, application, description, deleted, ");
    sql.append(" create_time, update_time, operator_id, service_in_cms_id ) ");
    sql.append(" values (:id, :name, :application, :description, :deleted, ");
    sql.append(" :createTime, :updateTime, :operatorId, :serviceInCmsId ) ");

    if (StringUtils.isBlank(serviceDO.getId())) {
      serviceDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(serviceDO.getServiceInCmsId())) {
      serviceDO.setServiceInCmsId("");
    }
    serviceDO.setCreateTime(DateUtils.now());
    serviceDO.setUpdateTime(serviceDO.getCreateTime());
    serviceDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(serviceDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return serviceDO;
  }

  private ServiceDO queryServiceById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<ServiceDO> serviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceDO.class));

    return CollectionUtils.isEmpty(serviceList) ? new ServiceDO() : serviceList.get(0);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, application, description, service_in_cms_id, ");
    sql.append(" deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SERVICE);

    return sql;
  }

}
