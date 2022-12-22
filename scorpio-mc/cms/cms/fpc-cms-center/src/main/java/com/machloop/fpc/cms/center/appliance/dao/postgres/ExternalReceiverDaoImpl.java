package com.machloop.fpc.cms.center.appliance.dao.postgres;

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
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.ExternalReceiverDao;
import com.machloop.fpc.cms.center.appliance.data.ExternalReceiverDO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
@Repository
public class ExternalReceiverDaoImpl implements ExternalReceiverDao {

  private static final String TABLE_APPLIANCE_EXTERNAL_RECEIVER = "fpccms_appliance_external_receiver";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;


  @Override
  public List<ExternalReceiverDO> queryExternalReceivers() {

    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ExternalReceiverDO.class));

  }

  @Override
  public List<ExternalReceiverDO> queryExternalReceiversByType(String receiverType) {

    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where receiver_type = :receiverType and deleted = :deleted ");
    sql.append(whereSql);

    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("receiverType", receiverType);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ExternalReceiverDO.class));
  }

  @Override
  public ExternalReceiverDO queryExternalReceiver(String id) {
    StringBuilder sql = buildSelectStatement();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where id = :id and deleted = :deleted ");
    params.put("id", id);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    sql.append(whereSql);

    List<ExternalReceiverDO> externalReceiverDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ExternalReceiverDO.class));

    return CollectionUtils.isEmpty(externalReceiverDOList) ? new ExternalReceiverDO()
        : externalReceiverDOList.get(0);
  }

  @Override
  public ExternalReceiverDO queryExternalReceiverByName(String name) {
    StringBuilder sql = buildSelectStatement();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where name = :name and deleted = :deleted ");
    params.put("name", name);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    sql.append(whereSql);

    List<ExternalReceiverDO> externalReceiverDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ExternalReceiverDO.class));

    return CollectionUtils.isEmpty(externalReceiverDOList) ? new ExternalReceiverDO()
        : externalReceiverDOList.get(0);
  }

  @Override
  public List<ExternalReceiverDO> queryMailExternalReceivers() {
    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where receiver_type = :receiverType and deleted = :deleted ");
    sql.append(whereSql);

    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("receiverType", FpcCmsConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_MAIL);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ExternalReceiverDO.class));
  }

  @Override
  public List<String> queryExternalReceiverIds(boolean onlyLocal) {

    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_EXTERNAL_RECEIVER);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and assign_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public List<ExternalReceiverDO> queryAssignExternalReceiverIds(Date beforeTime) {

    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_EXTERNAL_RECEIVER);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<ExternalReceiverDO> externalReceiverDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ExternalReceiverDO.class));

    return CollectionUtils.isEmpty(externalReceiverDOList) ? Lists.newArrayListWithCapacity(0)
        : externalReceiverDOList;
  }

  @Override
  public ExternalReceiverDO queryExternalReceiverByAssignId(String assignId) {

    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and assign_id = :assignId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignId", assignId);

    List<ExternalReceiverDO> externalReceiverDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ExternalReceiverDO.class));

    return CollectionUtils.isEmpty(externalReceiverDOList) ? new ExternalReceiverDO()
        : externalReceiverDOList.get(0);
  }

  @Override
  public void saveExternalReceiver(ExternalReceiverDO externalReceiverDO) {


    StringBuilder sql = new StringBuilder();
    sql.append(" insert into ").append(TABLE_APPLIANCE_EXTERNAL_RECEIVER);
    sql.append(" (id, name, receiver_content, receiver_type, assign_id, ");
    sql.append(" update_time, create_time, operator_id) ");
    sql.append(" values (:id, :name, :receiverContent, :receiverType, :assignId, ");
    sql.append(" :updateTime, :createTime, :operatorId) ");

    if (StringUtils.isBlank(externalReceiverDO.getId())) {
      externalReceiverDO.setId(IdGenerator.generateUUID());
    }

    externalReceiverDO.setCreateTime(DateUtils.now());
    externalReceiverDO.setUpdateTime(externalReceiverDO.getCreateTime());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(externalReceiverDO);
    jdbcTemplate.update(sql.toString(), paramSource);

  }

  @Override
  public void updateExternalReceiver(ExternalReceiverDO externalReceiverDO) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_EXTERNAL_RECEIVER);
    sql.append(
        " set name = :name, receiver_content = :receiverContent, receiver_type = :receiverType, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    externalReceiverDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(externalReceiverDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public void deleteExternalReceiver(String id, String operatorId) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_EXTERNAL_RECEIVER);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ExternalReceiverDO externalReceiverDO = new ExternalReceiverDO();
    externalReceiverDO.setDeleted(Constants.BOOL_YES);
    externalReceiverDO.setDeleteTime(DateUtils.now());
    externalReceiverDO.setOperatorId(operatorId);
    externalReceiverDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(externalReceiverDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  private StringBuilder buildSelectStatement() {

    StringBuilder sql = new StringBuilder();
    sql.append(" select id, name, receiver_content, receiver_type, assign_id, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_EXTERNAL_RECEIVER);

    return sql;
  }
}
