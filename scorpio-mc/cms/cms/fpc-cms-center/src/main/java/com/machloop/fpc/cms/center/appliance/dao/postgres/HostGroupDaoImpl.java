package com.machloop.fpc.cms.center.appliance.dao.postgres;

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
import com.machloop.fpc.cms.center.appliance.dao.HostGroupDao;
import com.machloop.fpc.cms.center.appliance.data.HostGroupDO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月5日, fpc-manager
 */
@Repository
public class HostGroupDaoImpl implements HostGroupDao {

  private static final String TABLE_APPLIANCE_HOST_GROUP = "fpccms_appliance_host_group";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#queryHostGroups(Pageable, String, String)
   */
  @Override
  public Page<HostGroupDO> queryHostGroups(Pageable page, String name, String description) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and name like :name ");
    }
    if (StringUtils.isNotBlank(description)) {
      whereSql.append(" and description like :description");
    }
    sql.append(whereSql);


    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", "%" + name + "%");
    params.put("description", "%" + description + "%");

    PageUtils.appendPage(sql, page, HostGroupDO.class);

    List<HostGroupDO> hostGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(HostGroupDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_HOST_GROUP);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(hostGroupList, page, total);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#queryHostGroupIds(java.util.Date)
   */
  @Override
  public List<String> queryHostGroupIds(boolean onlyLocal) {

    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_HOST_GROUP);
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
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#queryHostGroups()
   */
  @Override
  public List<HostGroupDO> queryHostGroups() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");

    sql.append(whereSql);
    sql.append(" order by name");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<HostGroupDO> hostGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(HostGroupDO.class));
    return CollectionUtils.isEmpty(hostGroupList) ? Lists.newArrayListWithCapacity(0)
        : hostGroupList;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#queryHostGroup(String)
   */
  @Override
  public HostGroupDO queryHostGroup(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<HostGroupDO> hostGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(HostGroupDO.class));
    return CollectionUtils.isEmpty(hostGroupList) ? new HostGroupDO() : hostGroupList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#queryHostGroupByName(String)
   */
  @Override
  public HostGroupDO queryHostGroupByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<HostGroupDO> hostGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(HostGroupDO.class));
    return CollectionUtils.isEmpty(hostGroupList) ? new HostGroupDO() : hostGroupList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#queryHostGroupByAssignId(java.lang.String)
   */
  @Override
  public HostGroupDO queryHostGroupByAssignId(String assignId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and assign_id = :assignId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignId", assignId);

    List<HostGroupDO> hostGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(HostGroupDO.class));

    return CollectionUtils.isEmpty(hostGroupList) ? new HostGroupDO() : hostGroupList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#queryAssignHostGroupIds(java.lang.String)
   */
  @Override
  public List<HostGroupDO> queryAssignHostGroupIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<HostGroupDO> hostGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(HostGroupDO.class));

    return CollectionUtils.isEmpty(hostGroupList) ? Lists.newArrayListWithCapacity(0)
        : hostGroupList;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#saveHostGroups(List)
   */
  @Override
  public void saveHostGroups(List<HostGroupDO> hostGroupDOList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" (id, name, ip_address, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :ipAddress, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    for (HostGroupDO hostGroupDO : hostGroupDOList) {
      hostGroupDO.setId(IdGenerator.generateUUID());
      hostGroupDO.setCreateTime(DateUtils.now());
      hostGroupDO.setUpdateTime(hostGroupDO.getCreateTime());
      hostGroupDO.setDeleted(Constants.BOOL_NO);
      if (StringUtils.isBlank(hostGroupDO.getAssignId())) {
        hostGroupDO.setAssignId("");
      }
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(hostGroupDOList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#saveHostGroup(com.machloop.fpc.cms.center.appliance.data.HostGroupDO)
   */
  @Override
  public HostGroupDO saveHostGroup(HostGroupDO hostGroupDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" (id, assign_id, name, ip_address, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :assignId, :name, :ipAddress, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(hostGroupDO.getId())) {
      hostGroupDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(hostGroupDO.getAssignId())) {
      hostGroupDO.setAssignId("");
    }
    hostGroupDO.setCreateTime(DateUtils.now());
    hostGroupDO.setUpdateTime(hostGroupDO.getCreateTime());
    hostGroupDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(hostGroupDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return hostGroupDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#batchSaveHostGroups(java.util.List)
   */
  @Override
  public int batchSaveHostGroups(List<HostGroupDO> hostGroupList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" (id, name, ip_address, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :ipAddress, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    hostGroupList.forEach(hostGroupDO -> {
      hostGroupDO.setId(IdGenerator.generateUUID());
      hostGroupDO.setCreateTime(DateUtils.now());
      hostGroupDO.setUpdateTime(hostGroupDO.getCreateTime());
      hostGroupDO.setDeleted(Constants.BOOL_NO);
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(hostGroupList);
    return jdbcTemplate.batchUpdate(sql.toString(), batchSource)[0];
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#updateHostGroup(com.machloop.fpc.cms.center.appliance.data.HostGroupDO)
   */
  @Override
  public int updateHostGroup(HostGroupDO hostGroupDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" set name = :name, ip_address = :ipAddress, ");
    sql.append(" description = :description, update_time = :updateTime, ");
    sql.append(" operator_id = :operatorId ");
    sql.append(" where id = :id ");

    hostGroupDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(hostGroupDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#refreshHostGroups(List)
   */
  @Override
  @Transactional
  public void refreshHostGroups(List<HostGroupDO> hostGroupDOList) {
    // 删除所有内部IP
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_HOST_GROUP);

    jdbcTemplate.update(sql.toString(),
        Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE));

    // 批量插入内部IP
    saveHostGroups(hostGroupDOList);

  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.HostGroupDao#deleteHostGroup(String,String)
   */
  @Override
  public int deleteHostGroup(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    HostGroupDO hostGroupDO = new HostGroupDO();
    hostGroupDO.setDeleted(Constants.BOOL_YES);
    hostGroupDO.setDeleteTime(DateUtils.now());
    hostGroupDO.setOperatorId(operatorId);
    hostGroupDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(hostGroupDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id, name, ip_address, description, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_HOST_GROUP);
    return sql;
  }
}
