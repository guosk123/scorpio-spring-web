package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolTnsLogDO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolTnsLogDao")
public class ProtocolTnsLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolTnsLogDO>
    implements LogRecordDao<ProtocolTnsLogDO> {

  private static final String TABLE_NAME = "t_fpc_protocol_tns_log_record";

  private static final List<
      String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name", "version");

  private static final ProtocolTnsLogDO EMPTY_DO = new ProtocolTnsLogDO();

  private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]*");

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
  protected ProtocolTnsLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolTnsLogDO protocolTnsLogDO = new ProtocolTnsLogDO();

    convertBaseLogMap2AbstractLogDO(protocolTnsLogDO, map);

    protocolTnsLogDO.setVersion(MapUtils.getLongValue(map, "version"));
    protocolTnsLogDO.setConnectData(MapUtils.getString(map, "connect_data"));
    protocolTnsLogDO.setConnectResult(MapUtils.getString(map, "connect_result"));
    protocolTnsLogDO.setCmd(MapUtils.getString(map, "cmd"));
    protocolTnsLogDO.setError(MapUtils.getString(map, "error"));
    protocolTnsLogDO.setDelaytime(MapUtils.getLongValue(map, "delaytime"));
    return protocolTnsLogDO;
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
  protected ProtocolTnsLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" policy_name = :keyword ");
    if (NUMBER_PATTERN.matcher(keyword).matches()) {
      conditionSql.append(" or version = :keyword ");
    }
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
