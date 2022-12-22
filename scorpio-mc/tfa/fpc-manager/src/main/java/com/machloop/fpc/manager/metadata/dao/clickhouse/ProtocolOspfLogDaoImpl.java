package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolOspfLogDO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

/**
 * @author guosk
 *
 * create at 2021年5月12日, fpc-manager
 */
@Repository("protocolOspfLogDao")
public class ProtocolOspfLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolOspfLogDO>
    implements LogRecordDao<ProtocolOspfLogDO> {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolOspfLogDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_protocol_ospf_log_record";

  private static final ProtocolOspfLogDO EMPTY_DO = new ProtocolOspfLogDO();

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
  protected ProtocolOspfLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolOspfLogDO protocolOspfLogDO = new ProtocolOspfLogDO();

    convertBaseLogMap2AbstractLogDO(protocolOspfLogDO, map);

    protocolOspfLogDO.setVersion(MapUtils.getIntValue(map, "version"));
    protocolOspfLogDO.setMessageType(MapUtils.getIntValue(map, "message_type"));
    protocolOspfLogDO.setPacketLength(MapUtils.getIntValue(map, "packet_length"));
    protocolOspfLogDO.setSourceOspfRouter(MapUtils.getLongValue(map, "source_ospf_router"));
    protocolOspfLogDO.setAreaId(MapUtils.getLongValue(map, "area_id"));
    // ipv4
    List<String> linkStateIpv4AddressList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("link_state_ipv4_address") != null) {
      String[] linkStateIpv4AddressArray = JsonHelper.deserialize(
          JsonHelper.serialize(map.get("link_state_ipv4_address")), new TypeReference<String[]>() {
          }, false);
      if (linkStateIpv4AddressArray != null) {
        for (String item : linkStateIpv4AddressArray) {
          linkStateIpv4AddressList.add(item);
        }
      }
    }
    protocolOspfLogDO.setLinkStateIpv4Address(linkStateIpv4AddressList);
    // ipv6
    List<String> linkStateIpv6AddressList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("link_state_ipv6_address") != null) {
      String[] linkStateIpv6AddressArray = JsonHelper.deserialize(
          JsonHelper.serialize(map.get("link_state_ipv6_address")), new TypeReference<String[]>() {
          }, false);
      if (linkStateIpv6AddressArray != null) {
        for (String item : linkStateIpv6AddressArray) {
          linkStateIpv6AddressList.add(item);
        }
      }
    }
    protocolOspfLogDO.setLinkStateIpv6Address(linkStateIpv6AddressList);
    protocolOspfLogDO.setMessage(MapUtils.getString(map, "message"));

    return protocolOspfLogDO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#buildEmptyLogDO()
   */
  @Override
  protected ProtocolOspfLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" policy_name = :keyword ");
    conditionSql.append(" or version = :keyword ");
    conditionSql.append(" or message_type = :keyword ");
    conditionSql.append(" or source_ospf_router = :keyword ");

    if (NetworkUtils.isInetAddress(keyword, IpVersion.V4)) {
      conditionSql.append(" or notEmpty(arrayFilter(y -> (toIPv4(:keyword) BETWEEN y.1 AND y.2), "
          + "arrayMap(x -> (IPv4CIDRToRange(toIPv4(splitByChar('/', x)[1]), toUInt8(splitByChar('/', x)[2]))), link_state_ipv4_address))) ");
    }

    if (NetworkUtils.isInetAddress(keyword, IpVersion.V6)) {
      conditionSql.append(" or notEmpty(arrayFilter(y -> (toIPv6(:keyword) BETWEEN y.1 AND y.2), "
          + "arrayMap(x -> (IPv6CIDRToRange(toIPv6(splitByChar('/', x)[1]), toUInt8(splitByChar('/', x)[2]))), link_state_ipv6_address))) ");
    }

    return conditionSql.toString();
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getWildcardConditionSql(java.lang.String)
   */
  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" source_ospf_router like :keyword_like ");
    return conditionSql.toString();
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getDslConverter()
   */
  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }
}
