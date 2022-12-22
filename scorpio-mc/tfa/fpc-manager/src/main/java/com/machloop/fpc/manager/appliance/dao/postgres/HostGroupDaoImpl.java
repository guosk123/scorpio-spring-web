package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.machloop.fpc.manager.appliance.dao.HostGroupDao;
import com.machloop.fpc.manager.appliance.data.HostGroupDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月5日, fpc-manager
 */
@Repository
public class HostGroupDaoImpl implements HostGroupDao {

  private static final String TABLE_APPLIANCE_HOST_GROUP = "fpc_appliance_host_group";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#queryHostGroups(com.machloop.alpha.common.base.page.Pageable, long, java.lang.String)
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
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#queryHostGroups(long)
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
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#queryHostGroup(java.lang.String)
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
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#queryHostGroupByName(java.lang.String)
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
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#queryAssignHostGroups(java.util.Date)
   */
  @Override
  public List<String> queryAssignHostGroups(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select host_group_in_cms_id from ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" where deleted = :deleted and host_group_in_cms_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#queryHostGroupIds(java.util.Date, boolean)
   */
  @Override
  public List<String> queryHostGroupIds(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and host_group_in_cms_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#saveHostGroups(java.util.List)
   */
  @Override
  public void saveHostGroups(List<HostGroupDO> hostGroupDOList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" (id, name, ip_address, host_group_in_cms_id, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :ipAddress, :hostGroupInCmsId, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    for (HostGroupDO hostGroupDO : hostGroupDOList) {
      hostGroupDO.setId(IdGenerator.generateUUID());
      hostGroupDO.setCreateTime(DateUtils.now());
      hostGroupDO.setUpdateTime(hostGroupDO.getCreateTime());
      hostGroupDO.setDeleted(Constants.BOOL_NO);
      if (StringUtils.isBlank(hostGroupDO.getHostGroupInCmsId())) {
        hostGroupDO.setHostGroupInCmsId("");
      }
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(hostGroupDOList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  @Override
  public int saveHostGroups(List<HostGroupDO> hostInsideDOList, String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" (id, name, ip_address, host_group_in_cms_id, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :ipAddress, :hostGroupInCmsId, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    for (HostGroupDO hostGroupDO : hostInsideDOList) {
      hostGroupDO.setId(IdGenerator.generateUUID());
      hostGroupDO.setCreateTime(DateUtils.now());
      hostGroupDO.setUpdateTime(hostGroupDO.getCreateTime());
      hostGroupDO.setDeleted(Constants.BOOL_NO);
      hostGroupDO.setOperatorId(id);
      if (StringUtils.isBlank(hostGroupDO.getHostGroupInCmsId())) {
        hostGroupDO.setHostGroupInCmsId("");
      }
      if (StringUtils.isBlank(hostGroupDO.getDescription())) {
        hostGroupDO.setDescription("");
      }
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(hostInsideDOList);
    return Arrays.stream(jdbcTemplate.batchUpdate(sql.toString(), batchSource)).sum();
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#saveHostGroup(com.machloop.fpc.manager.appliance.data.HostGroupDO)
   */
  @Override
  public HostGroupDO saveOrRecoverHostGroup(HostGroupDO hostGroupDO) {
    HostGroupDO exist = queryHostGroupById(hostGroupDO.getId() == null ? "" : hostGroupDO.getId());
    if (StringUtils.isBlank(exist == null ? "" : exist.getId())) {
      return saveHostGroup(hostGroupDO);
    } else {
      recoverAndUpdateHostGroup(hostGroupDO);
      return queryHostGroupById(hostGroupDO.getId());
    }
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#updateHostGroup(com.machloop.fpc.manager.appliance.data.HostGroupDO)
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
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#refreshHostGroups(java.util.List)
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
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#deleteHostGroup(java.lang.String)
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

  @Override
  public List<HostGroupDO> queryHostGroupByNameList(String name) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted");
    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and name like :name");
    }
    sql.append(whereSql);
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", "%" + name + "%");
    List<HostGroupDO> hostGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(HostGroupDO.class));

    return hostGroupList;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.HostGroupDao#queryHostGroupByCmsHostGroupId(java.lang.String)
   */
  @Override
  public HostGroupDO queryHostGroupByCmsHostGroupId(String cmsHostGroupId) {

    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and host_group_in_cms_id = :cmsHostGroupId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("cmsHostGroupId", cmsHostGroupId);

    List<HostGroupDO> hostGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(HostGroupDO.class));

    return CollectionUtils.isEmpty(hostGroupList) ? new HostGroupDO() : hostGroupList.get(0);
  }

  private HostGroupDO queryHostGroupById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<HostGroupDO> hostGroupList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(HostGroupDO.class));

    return CollectionUtils.isEmpty(hostGroupList) ? new HostGroupDO() : hostGroupList.get(0);
  }

  /**
   * 为了避免主键冲突的情况，此方法会将deleted=1的数据更新为deleted=0，并对数据做相应的更新
   */
  private int recoverAndUpdateHostGroup(HostGroupDO hostGroupDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" set name = :name, ip_address = :ipAddress, ");
    sql.append(" description = :description, update_time = :updateTime, ");
    sql.append(" operator_id = :operatorId, deleted = :deleted ");
    sql.append(" where id = :id ");

    hostGroupDO.setUpdateTime(DateUtils.now());
    hostGroupDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(hostGroupDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private HostGroupDO saveHostGroup(HostGroupDO hostGroupDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_HOST_GROUP);
    sql.append(" (id, name, ip_address, host_group_in_cms_id, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :ipAddress, :hostGroupInCmsId, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(hostGroupDO.getId())) {
      hostGroupDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(hostGroupDO.getHostGroupInCmsId())) {
      hostGroupDO.setHostGroupInCmsId("");
    }
    hostGroupDO.setCreateTime(DateUtils.now());
    hostGroupDO.setUpdateTime(hostGroupDO.getCreateTime());
    hostGroupDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(hostGroupDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return hostGroupDO;
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, ip_address, host_group_in_cms_id, description, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_HOST_GROUP);
    return sql;
  }
}
