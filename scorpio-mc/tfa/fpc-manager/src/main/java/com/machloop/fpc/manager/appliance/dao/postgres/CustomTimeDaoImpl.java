package com.machloop.fpc.manager.appliance.dao.postgres;

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

import com.clickhouse.client.internal.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.appliance.dao.CustomTimeDao;
import com.machloop.fpc.manager.appliance.data.CustomTimeDO;

/**
 * @author minjiajun
 *
 * create at 2022年6月9日, fpc-manager
 */
@Repository
public class CustomTimeDaoImpl implements CustomTimeDao {

  private static final String TABLE_APPLIANCE_CUSTOM_TIME = "fpc_appliance_custom_time";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.CustomTimeDao#queryCustomTimes()
   */
  @Override
  public List<CustomTimeDO> queryCustomTimes(String type) {
    StringBuilder sql = buildSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(type)) {
      sql.append(" and type = :type ");
      params.put("type", type);
    }

    sql.append(" order by create_time desc ");
    List<CustomTimeDO> customTimeList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CustomTimeDO.class));
    return customTimeList;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.CustomTimeDao#queryCustomTime(java.lang.String)
   */
  @Override
  public CustomTimeDO queryCustomTime(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<CustomTimeDO> customTimeList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CustomTimeDO.class));
    return CollectionUtils.isEmpty(customTimeList) ? new CustomTimeDO() : customTimeList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.CustomTimeDao#queryCustomTimeByName(java.lang.String)
   */
  @Override
  public CustomTimeDO queryCustomTimeByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<CustomTimeDO> customTimeList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CustomTimeDO.class));
    return CollectionUtils.isEmpty(customTimeList) ? new CustomTimeDO() : customTimeList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.CustomTimeDao#queryCustomTimeIds(boolean)
   */
  @Override
  public List<String> queryCustomTimeIds(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_CUSTOM_TIME);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and custom_time_in_cms_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.CustomTimeDao#queryAssignCustomTimeIds(java.util.Date)
   */
  @Override
  public List<String> queryAssignCustomTimeIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select custom_time_in_cms_id from ").append(TABLE_APPLIANCE_CUSTOM_TIME);
    sql.append(" where deleted = :deleted and custom_time_in_cms_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.CustomTimeDao#saveCustomTime(com.machloop.fpc.manager.appliance.data.CustomTimeDO)
   */
  @Override
  public CustomTimeDO saveCustomTime(CustomTimeDO customTimeDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_CUSTOM_TIME);
    sql.append(" (id, custom_time_in_cms_id, name, type, period, custom_time_setting, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :customTimeInCmsId, :name, :type, :period, :customTimeSetting, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    if(StringUtils.isBlank(customTimeDO.getId())) {
      customTimeDO.setId(IdGenerator.generateUUID());
    }
    customTimeDO.setCreateTime(DateUtils.now());
    customTimeDO.setUpdateTime(customTimeDO.getCreateTime());
    customTimeDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(customTimeDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return customTimeDO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.CustomTimeDao#updateCustomTime(com.machloop.fpc.manager.appliance.data.CustomTimeDO)
   */
  @Override
  public int updateCustomTime(CustomTimeDO customTimeDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_CUSTOM_TIME);
    sql.append(" set name = :name, type = :type, period = :period, ");
    sql.append(" custom_time_setting = :customTimeSetting, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    customTimeDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(customTimeDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.CustomTimeDao#deleteCustomTime(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCustomTime(List<String> idList, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_CUSTOM_TIME);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id in (:id) ");

    List<CustomTimeDO> paramList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (String id : idList) {
      CustomTimeDO customTimeDO = new CustomTimeDO();
      customTimeDO.setDeleted(Constants.BOOL_YES);
      customTimeDO.setDeleteTime(DateUtils.now());
      customTimeDO.setOperatorId(operatorId);
      customTimeDO.setId(id);
      paramList.add(customTimeDO);
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(paramList);
    return jdbcTemplate.batchUpdate(sql.toString(), batchSource)[0];
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, custom_time_in_cms_id, name, type, period, custom_time_setting, ");
    sql.append(" deleted, create_time, update_time, delete_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_CUSTOM_TIME);

    return sql;
  }

}
