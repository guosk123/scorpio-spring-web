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
import com.machloop.fpc.cms.center.metadata.data.ProtocolTelnetLogDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年9月24日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolTelnetLogDao")
public class ProtocolTelnetLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolTelnetLogDO>
    implements LogRecordDao<ProtocolTelnetLogDO> {

  private static final String TABLE_NAME = "d_fpc_protocol_telnet_log_record";

  private static List<
      String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name", "username", "cmd");

  private static final ProtocolTelnetLogDO EMPTY_DO = new ProtocolTelnetLogDO();

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
  protected ProtocolTelnetLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolTelnetLogDO protocolTelnetLogDO = new ProtocolTelnetLogDO();

    convertBaseLogMap2AbstractLogDO(protocolTelnetLogDO, map);

    protocolTelnetLogDO.setUsername(MapUtils.getString(map, "username"));
    protocolTelnetLogDO.setPassword(MapUtils.getString(map, "password"));
    protocolTelnetLogDO.setCmd(MapUtils.getString(map, "cmd"));
    protocolTelnetLogDO.setReply(MapUtils.getString(map, "reply"));

    return protocolTelnetLogDO;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#buildEmptyLogDO()
   */
  @Override
  protected ProtocolTelnetLogDO buildEmptyLogDO() {
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
    conditionSql.append(" or username = :keyword ");
    conditionSql.append(" or cmd = :keyword ");
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

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getDslConverter()
   */
  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }
}