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
import com.machloop.fpc.manager.metadata.data.ProtocolMysqlLogDO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolMysqlLogDao")
public class ProtocolMysqlLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolMysqlLogDO>
    implements LogRecordDao<ProtocolMysqlLogDO> {

  private static final String TABLE_NAME = "t_fpc_protocol_mysql_log_record";

  private static final List<
      String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name", "username", "database_name");

  private static final ProtocolMysqlLogDO EMPTY_DO = new ProtocolMysqlLogDO();

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
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getTableName(com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO)
   */
  @Override
  protected String getTableName() {
    return TABLE_NAME;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#convertLogMap2LogDO(java.util.Map)
   */
  @Override
  protected ProtocolMysqlLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolMysqlLogDO protocolMysqlLogDO = new ProtocolMysqlLogDO();

    convertBaseLogMap2AbstractLogDO(protocolMysqlLogDO, map);

    protocolMysqlLogDO.setUsername(MapUtils.getString(map, "username"));
    protocolMysqlLogDO.setDatabaseName(MapUtils.getString(map, "database_name"));
    protocolMysqlLogDO.setCmd(MapUtils.getString(map, "cmd"));
    protocolMysqlLogDO.setError(MapUtils.getString(map, "error"));
    protocolMysqlLogDO.setDelaytime(MapUtils.getLongValue(map, "delaytime"));
    return protocolMysqlLogDO;
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
  protected ProtocolMysqlLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
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
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getWildcardConditionSql(java.lang.String)
   */
  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" 1=2 ");
    return conditionSql.toString();
  }

}
