package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolSocks5LogDO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolSocks5LogDao")
public class ProtocolSocks5LogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolSocks5LogDO>
    implements LogRecordDao<ProtocolSocks5LogDO> {

  private static final String TABLE_NAME = "t_fpc_protocol_socks5_log_record";

  private static final List<String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name", "atyp",
      "bind_addr", "username", "auth_method", "auth_result", "cmd", "cmd_result");

  private static final ProtocolSocks5LogDO EMPTY_DO = new ProtocolSocks5LogDO();

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  @Autowired
  private PacketAnalysisSubTaskDao offlineAnalysisSubTaskDao;

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getClickHouseJdbcTemplate()
   */
  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getOfflineAnalysisSubTaskDao()
   */
  @Override
  protected PacketAnalysisSubTaskDao getOfflineAnalysisSubTaskDao() {
    return offlineAnalysisSubTaskDao;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getTableName()
   */
  @Override
  protected String getTableName() {
    return TABLE_NAME;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#convertLogMap2LogDO(java.util.Map)
   */
  @Override
  protected ProtocolSocks5LogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolSocks5LogDO protocolSocks5LogDO = new ProtocolSocks5LogDO();

    convertBaseLogMap2AbstractLogDO(protocolSocks5LogDO, map);

    protocolSocks5LogDO.setAtyp(MapUtils.getString(map, "atyp"));
    protocolSocks5LogDO.setBindAddr(MapUtils.getString(map, "bind_addr"));
    protocolSocks5LogDO.setBindPort(MapUtils.getIntValue(map, "bind_port"));
    protocolSocks5LogDO.setUsername(MapUtils.getString(map, "username"));
    protocolSocks5LogDO.setPassword(MapUtils.getString(map, "password"));
    protocolSocks5LogDO.setAuthMethod(MapUtils.getString(map, "auth_method"));
    protocolSocks5LogDO.setAuthResult(MapUtils.getString(map, "auth_result"));
    protocolSocks5LogDO.setCmd(MapUtils.getString(map, "cmd"));
    protocolSocks5LogDO.setCmdResult(MapUtils.getString(map, "cmd_result"));
    protocolSocks5LogDO.setChannelState(MapUtils.getIntValue(map, "channel_state"));
    return protocolSocks5LogDO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getDslConverter()
   */
  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#buildEmptyLogDO()
   */
  @Override
  protected ProtocolSocks5LogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" policy_name = :keyword ");
    conditionSql.append(" or atyp = :keyword ");
    conditionSql.append(" or bind_addr = :keyword ");
    conditionSql.append(" or username = :keyword ");
    conditionSql.append(" or auth_method = :keyword ");
    conditionSql.append(" or auth_result = :keyword ");
    conditionSql.append(" or cmd = :keyword ");
    conditionSql.append(" or cmd_result = :keyword ");
    return conditionSql.toString();
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getWildcardConditionSql(java.lang.String)
   */
  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" 1=2 ");
    return conditionSql.toString();
  }

}
