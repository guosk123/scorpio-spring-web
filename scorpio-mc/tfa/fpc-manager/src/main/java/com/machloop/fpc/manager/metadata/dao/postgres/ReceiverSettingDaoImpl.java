package com.machloop.fpc.manager.metadata.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.metadata.dao.ReceiverSettingDao;
import com.machloop.fpc.manager.metadata.data.ReceiverSettingDO;

@Repository
public class ReceiverSettingDaoImpl implements ReceiverSettingDao {

  private static final String TABLE_RECEIVER_SETTING = "fpc_appliance_receiver_setting";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.metadata.dao.ReceiverSettingDao#queryReceiverSetting()
   */
  @Override
  public ReceiverSettingDO queryReceiverSetting() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<ReceiverSettingDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ReceiverSettingDO.class));

    return CollectionUtils.isEmpty(list) ? new ReceiverSettingDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.ReceiverSettingDao#saveOrUpdateReceiverSetting(com.machloop.fpc.manager.metadata.data.ReceiverSettingDO)
   */
  @Override
  public int saveOrUpdateReceiverSetting(ReceiverSettingDO receiverSettingDO) {
    int update = updateReceiverSetting(receiverSettingDO);
    return update > 0 ? update : saveReceiverSetting(receiverSettingDO);
  }

  private int saveReceiverSetting(ReceiverSettingDO receiverSettingDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_RECEIVER_SETTING);
    sql.append(" (id, name, protocol_topic, http_action, ");
    sql.append(" http_action_exculde_uri_suffix, receiver_id, receiver_type,");
    sql.append(" state, deleted, create_time, update_time, operator_id)");
    sql.append(" values (:id, :name, :protocolTopic, :httpAction, ");
    sql.append(" :httpActionExculdeUriSuffix, :receiverId, :receiverType,");
    sql.append(" :state, :deleted, :createTime, :updateTime, :operatorId)");

    receiverSettingDO.setId(IdGenerator.generateUUID());
    receiverSettingDO.setState(Constants.BOOL_YES);
    receiverSettingDO.setDeleted(Constants.BOOL_NO);
    receiverSettingDO.setCreateTime(DateUtils.now());
    receiverSettingDO.setUpdateTime(receiverSettingDO.getCreateTime());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(receiverSettingDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private int updateReceiverSetting(ReceiverSettingDO receiverSettingDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_RECEIVER_SETTING);
    sql.append(" set name = :name, protocol_topic = :protocolTopic, ");
    sql.append(" http_action = :httpAction, ");
    sql.append(" http_action_exculde_uri_suffix = :httpActionExculdeUriSuffix, ");
    sql.append(" state = :state, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId");

    receiverSettingDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(receiverSettingDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, protocol_topic, http_action, ");
    sql.append(" http_action_exculde_uri_suffix, receiver_id, receiver_type, state,");
    sql.append(" deleted, create_time, update_time, delete_time, operator_id ");
    sql.append(" from ").append(TABLE_RECEIVER_SETTING);
    return sql;
  }

}
