package com.machloop.fpc.cms.center.metadata.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.data.ProtocolSshLogDO;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolSshLogDao")
public class ProtocolSshLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolSshLogDO>
    implements LogRecordDao<ProtocolSshLogDO> {

  private static final String TABLE_NAME = "d_fpc_protocol_ssh_log_record";

  private static final List<String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name",
      "client_version", "client_software", "server_version", "server_software", "server_key_type");

  private static final ProtocolSshLogDO EMPTY_DO = new ProtocolSshLogDO();

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getClickHouseJdbcTemplate()
   */
  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getTableName()
   */
  @Override
  protected String getTableName() {
    return TABLE_NAME;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#convertLogMap2LogDO(java.util.Map)
   */
  @Override
  protected ProtocolSshLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolSshLogDO protocolSshLogDO = new ProtocolSshLogDO();

    convertBaseLogMap2AbstractLogDO(protocolSshLogDO, map);

    protocolSshLogDO.setClientVersion(MapUtils.getString(map, "client_version"));
    protocolSshLogDO.setClientSoftware(MapUtils.getString(map, "client_software"));
    protocolSshLogDO.setClientComments(MapUtils.getString(map, "client_comments"));
    protocolSshLogDO.setServerVersion(MapUtils.getString(map, "server_version"));
    protocolSshLogDO.setServerSoftware(MapUtils.getString(map, "server_software"));
    protocolSshLogDO.setServerComments(MapUtils.getString(map, "server_comments"));
    protocolSshLogDO.setServerKey(MapUtils.getString(map, "server_key"));
    protocolSshLogDO.setServerKeyType(MapUtils.getString(map, "server_key_type"));
    return protocolSshLogDO;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getDslConverter()
   */
  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#buildEmptyLogDO()
   */
  @Override
  protected ProtocolSshLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" policy_name = :keyword ");
    conditionSql.append(" or client_version = :keyword ");
    conditionSql.append(" or client_software = :keyword ");
    conditionSql.append(" or server_version = :keyword ");
    conditionSql.append(" or server_software = :keyword ");
    conditionSql.append(" or server_key_type = :keyword ");
    return conditionSql.toString();
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getWildcardConditionSql(java.lang.String)
   */
  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" 1=2 ");
    return conditionSql.toString();
  }

}
