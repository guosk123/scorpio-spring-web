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
import com.machloop.fpc.cms.center.metadata.data.ProtocolPostgresqlLogDO;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolPostgresqlLogDao")
public class ProtocolPostgresqlLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolPostgresqlLogDO>
    implements LogRecordDao<ProtocolPostgresqlLogDO> {

  private static final String TABLE_NAME = "d_fpc_protocol_postgresql_log_record";

  private static final List<
      String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name", "username", "database_name");

  private static final ProtocolPostgresqlLogDO EMPTY_DO = new ProtocolPostgresqlLogDO();

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
  protected ProtocolPostgresqlLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolPostgresqlLogDO protocolPostgresqlLogDO = new ProtocolPostgresqlLogDO();

    convertBaseLogMap2AbstractLogDO(protocolPostgresqlLogDO, map);

    protocolPostgresqlLogDO.setUsername(MapUtils.getString(map, "username"));
    protocolPostgresqlLogDO.setDatabaseName(MapUtils.getString(map, "database_name"));
    protocolPostgresqlLogDO.setCmd(MapUtils.getString(map, "cmd"));
    protocolPostgresqlLogDO.setError(MapUtils.getString(map, "error"));
    protocolPostgresqlLogDO.setDelaytime(MapUtils.getLongValue(map, "delaytime"));
    return protocolPostgresqlLogDO;
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
  protected ProtocolPostgresqlLogDO buildEmptyLogDO() {
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
    conditionSql.append(" or database_name = :keyword ");
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
