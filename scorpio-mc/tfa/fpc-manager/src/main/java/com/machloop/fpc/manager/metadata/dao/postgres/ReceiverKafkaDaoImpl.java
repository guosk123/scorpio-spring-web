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
import com.machloop.fpc.manager.metadata.dao.ReceiverKafkaDao;
import com.machloop.fpc.manager.metadata.data.ReceiverKafkaDO;

@Repository
public class ReceiverKafkaDaoImpl implements ReceiverKafkaDao {

  private static final String TABLE_RECEIVER_KAFKA = "fpc_appliance_receiver_kafka";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.metadata.dao.ReceiverKafkaDao#queryReceiverKafka()
   */
  @Override
  public ReceiverKafkaDO queryReceiverKafka() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and state = :state ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("state", Constants.BOOL_YES);

    List<ReceiverKafkaDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ReceiverKafkaDO.class));

    return CollectionUtils.isEmpty(list) ? new ReceiverKafkaDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.ReceiverKafkaDao#saveOrUpdateReceiverKafka(com.machloop.fpc.manager.metadata.data.ReceiverKafkaDO)
   */
  @Override
  public ReceiverKafkaDO saveOrUpdateReceiverKafka(ReceiverKafkaDO receiverKafkaDO) {
    int update = updateReceiverKafka(receiverKafkaDO);

    if (update == 0) {
      saveReceiverKafka(receiverKafkaDO);
    }

    return queryReceiverKafka();
  }

  private int saveReceiverKafka(ReceiverKafkaDO receiverKafkaDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_RECEIVER_KAFKA);
    sql.append(" (id, name, receiver_address, kerberos_certification, keytab_file_path, ");
    sql.append(" key_restore_time, sasl_kerberos_service_name, sasl_kerberos_principal,");
    sql.append(" security_protocol, authentication_mechanism, state, ");
    sql.append(" deleted, create_time, update_time, operator_id)");
    sql.append(" values(:id, :name, :receiverAddress, :kerberosCertification, :keytabFilePath, ");
    sql.append(" :keyRestoreTime, :saslKerberosServiceName,");
    sql.append("  :saslKerberosPrincipal, :securityProtocol, :authenticationMechanism, ");
    sql.append(" :state, :deleted, :createTime, :updateTime, :operatorId)");

    receiverKafkaDO.setId(IdGenerator.generateUUID());
    receiverKafkaDO.setState(Constants.BOOL_YES);
    receiverKafkaDO.setDeleted(Constants.BOOL_NO);
    receiverKafkaDO.setCreateTime(DateUtils.now());
    receiverKafkaDO.setUpdateTime(receiverKafkaDO.getCreateTime());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(receiverKafkaDO);

    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private int updateReceiverKafka(ReceiverKafkaDO receiverKafkaDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_RECEIVER_KAFKA);
    sql.append(" set name = :name, receiver_address = :receiverAddress, ");
    sql.append(" kerberos_certification = :kerberosCertification, ");
    sql.append(" keytab_file_path = :keytabFilePath, key_restore_time = :keyRestoreTime, ");
    sql.append(" sasl_kerberos_service_name = :saslKerberosServiceName, ");
    sql.append(" sasl_kerberos_principal = :saslKerberosPrincipal, ");
    sql.append(" security_protocol = :securityProtocol, ");
    sql.append(" authentication_mechanism = :authenticationMechanism, ");
    sql.append(" state = :state, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId");

    receiverKafkaDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(receiverKafkaDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, receiver_address, kerberos_certification, keytab_file_path, ");
    sql.append(" key_restore_time, sasl_kerberos_service_name, sasl_kerberos_principal,");
    sql.append(" security_protocol, authentication_mechanism, state");
    sql.append(" deleted, create_time, update_time, delete_time, operator_id ");
    sql.append(" from ").append(TABLE_RECEIVER_KAFKA);
    return sql;
  }

}
