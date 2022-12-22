package com.machloop.fpc.cms.center.broker.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.broker.dao.SendupMessageDao;
import com.machloop.fpc.cms.center.broker.data.SendupMessageDO;

/**
 * @author guosk
 *
 * create at 2021年12月16日, fpc-cms-center
 */
@Repository
public class SendupMessageDaoImpl implements SendupMessageDao {

  private static final String TABLE_CENTRAL_SENDUP_MESSAGE = "fpccms_central_sendup_message";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.SendupMessageDao#querySendupMessages(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public List<SendupMessageDO> querySendupMessages(String deviceType, String deviceSerialNumber,
      String type, String result, Date startTime, Date endTime) {

    StringBuilder sql = buildSelectStatement();
    sql.append(" where device_type = :deviceType ");
    sql.append(" and device_serial_number = :deviceSerialNumber ");
    sql.append(" and type = :type and result = :result ");
    sql.append(" and start_time >= :startTime ");
    sql.append(" and start_time < :endTime ");
    sql.append(" order by start_time asc ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("deviceSerialNumber", deviceSerialNumber);
    params.put("type", type);
    params.put("result", result);
    params.put("startTime", startTime);
    params.put("endTime", endTime);

    List<SendupMessageDO> sendupMessageList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendupMessageDO.class));

    return CollectionUtils.isEmpty(sendupMessageList)
        ? Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE)
        : sendupMessageList;
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.SendupMessageDao#saveSendupMessage(com.machloop.fpc.manager.system.data.SendupMessageDO)
   */
  @Override
  public void saveSendupMessage(SendupMessageDO sendupMessageDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_CENTRAL_SENDUP_MESSAGE);
    sql.append(" (id, message_id, device_type, device_serial_number, start_time, ");
    sql.append(" end_time, type, content, result, create_time, update_time ) ");
    sql.append(" values (:id, :messageId, :deviceType, :deviceSerialNumber, :startTime, ");
    sql.append(" :endTime, :type, :content, :result, :createTime, :updateTime ) ");

    sendupMessageDO.setId(IdGenerator.generateUUID());
    sendupMessageDO.setCreateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sendupMessageDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.SendupMessageDao#updateSendupMessageResults(java.util.List, java.lang.String)
   */
  @Override
  public void updateSendupMessageResults(List<String> messageIdList, String result) {

    if (CollectionUtils.isEmpty(messageIdList)) {
      return;
    }

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_CENTRAL_SENDUP_MESSAGE);
    sql.append(" set result = :result, update_time = :updateTime ");
    sql.append(" where message_id in (:messageIdList) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("result", result);
    params.put("updateTime", DateUtils.now());
    params.put("messageIdList", messageIdList);

    jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.SendupMessageDao#updateSendupMessageResult(java.lang.String, java.lang.String)
   */
  @Override
  public void updateSendupMessageResult(String messageId, String result) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_CENTRAL_SENDUP_MESSAGE);
    sql.append(" set result = :result, update_time = :updateTime ");
    sql.append(" where message_id = :messageId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("messageId", messageId);
    params.put("result", result);
    params.put("updateTime", DateUtils.now());

    jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.SendupMessageDao#deleteExpireMessage(java.util.Date)
   */
  @Override
  public int deleteExpireMessage(Date expireTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_CENTRAL_SENDUP_MESSAGE);
    sql.append(" where start_time < :expireTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("expireTime", expireTime);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, message_id, device_type, device_serial_number, start_time, ");
    sql.append(" end_time, type, content, result, create_time, update_time ");
    sql.append(" from ").append(TABLE_CENTRAL_SENDUP_MESSAGE);
    return sql;
  }
}
