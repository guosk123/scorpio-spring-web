package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolTdsLogDO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;
import com.machloop.fpc.manager.metadata.data.ProtocolTdsLogDO;

/**
 * @author guosk
 *
 * create at 2020年12月11日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolTdsLogDao")
public class ProtocolTdsLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolTdsLogDO>
    implements LogRecordDao<ProtocolTdsLogDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolTdsLogDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_protocol_tds_log_record";

  private static final List<
      String> OTHER_FIELD_NAMES = Lists.newArrayList("cmd", "error", "delaytime");

  private static final ProtocolTdsLogDO EMPTY_DO = new ProtocolTdsLogDO();

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
  protected ProtocolTdsLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolTdsLogDO protocolTdsLogDO = new ProtocolTdsLogDO();

    convertBaseLogMap2AbstractLogDO(protocolTdsLogDO, map);

    protocolTdsLogDO.setCmd(MapUtils.getString(map, "cmd"));
    protocolTdsLogDO.setError(MapUtils.getString(map, "error"));
    protocolTdsLogDO.setDelaytime(MapUtils.getLongValue(map, "delaytime"));

    return protocolTdsLogDO;
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
  protected ProtocolTdsLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" policy_name = :keyword ");

    return conditionSql.toString();
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getWildcardConditionSql(java.lang.String)
   */
  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" cmd like :keyword_like ");
    conditionSql.append(" or error like :keyword_like ");
    return conditionSql.toString();
  }

}
