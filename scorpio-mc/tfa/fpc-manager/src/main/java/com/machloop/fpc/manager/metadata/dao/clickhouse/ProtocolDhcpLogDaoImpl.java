package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolDhcpLogDO;
import com.machloop.fpc.manager.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

/**
 * @author guosk
 *
 * create at 2020年12月11日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolDhcpLogDao")
public class ProtocolDhcpLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolDhcpLogDO>
    implements LogRecordDao<ProtocolDhcpLogDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolDhcpLogDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_protocol_dhcp_log_record";

  private static final List<String> OTHER_FIELD_NAMES = Lists.newArrayList("version", "src_mac",
      "dest_mac", "message_type", "transaction_id", "parameters");

  private static final ProtocolDhcpLogDO EMPTY_DO = new ProtocolDhcpLogDO();

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private PacketAnalysisSubTaskDao offlineAnalysisSubTaskDao;

  @Override
  public Map<String, Long> countLogRecords(LogCountQueryVO queryVO) {
    LogRecordQueryVO logRecordQueryVO = new LogRecordQueryVO();
    logRecordQueryVO.setSourceType(queryVO.getSourceType());
    logRecordQueryVO.setPacketFileId(queryVO.getPacketFileId());
    StringBuilder countSql = new StringBuilder();
    countSql.append("select version, count(1) as count from ");
    String tableName = convertTableName(logRecordQueryVO);
    countSql.append(tableName);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where 1=1 ");

    if (queryVO.getStartTimeDate() != null && queryVO.getEndTimeDate() != null) {
      whereSql.append(" and start_time >= toDateTime64(:start_time, 9, 'UTC') ");
      whereSql.append(" and start_time < toDateTime64(:end_time, 9, 'UTC') ");
      params.put("start_time", DateUtils.toStringFormat(queryVO.getStartTimeDate(),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      params.put("end_time", DateUtils.toStringFormat(queryVO.getEndTimeDate(),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      whereSql.append(" and has(network_id, :network_id)=1 ");
      params.put("network_id", queryVO.getNetworkId());
    }
    if (StringUtils.isNotBlank(queryVO.getPacketFileId())) {
      whereSql.append(" and has(network_id, :network_id)=1 ");
      params.put("network_id", queryVO.getPacketFileId());
    }
    if (StringUtils.isNotBlank(queryVO.getServiceId())) {
      whereSql.append(" and has(service_id, :service_id)=1 ");
      params.put("service_id", queryVO.getServiceId());
    }
    if (StringUtils.isNotBlank(queryVO.getSrcIp())) {
      if (NetworkUtils.isInetAddress(queryVO.getSrcIp())) {
        if (NetworkUtils.isInetAddress(queryVO.getSrcIp(), IpVersion.V4)) {
          whereSql.append(" and src_ipv4 = toIPv4(:src_ip) ");
        } else {
          whereSql.append(" and src_ipv6 = toIPv6(:src_ip) ");
        }
        params.put("src_ip", queryVO.getSrcIp());
      } else if (NetworkUtils.isCidr(queryVO.getSrcIp())) {
        if (NetworkUtils.isCidr(queryVO.getSrcIp(), IpVersion.V4)) {
          whereSql.append(" and (src_ipv4 between IPv4CIDRToRange(toIPv4(:ip_address), :cidr).1 ");
          whereSql.append(" and IPv4CIDRToRange(toIPv4(:ip_address), :cidr).2)");
        } else {
          whereSql.append(" and (src_ipv6 between IPv6CIDRToRange(toIPv6(:ip_address), :cidr).1 ");
          whereSql.append(" and IPv6CIDRToRange(toIPv6(:ip_address), :cidr).2)");
        }
        String[] ipAndCidr = queryVO.getSrcIp().split("/");
        params.put("ip_address", ipAndCidr[0]);
        params.put("cidr", Integer.parseInt(ipAndCidr[1]));
      } else {
        whereSql.append(" and 1=2 ");
      }
    }
    countSql.append(whereSql);

    countSql.append(" group by version ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("count {} logs, sql: {}, params: {}", convertTableName(logRecordQueryVO),
          countSql.toString(), params);
    }

    List<Map<String, Object>> countMap = getClickHouseJdbcTemplate().getJdbcTemplate()
        .query(countSql.toString(), params, new ColumnMapRowMapper());

    Map<String, String> versionDict = dictManager.getBaseDict().getItemMap("protocol_dhcp_version");

    Map<String, Long> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    countMap.stream().forEach(item -> {
      String version = MapUtils.getString(item, "version", "");
      long count = MapUtils.getLongValue(item, "count", 0);
      if (StringUtils.isNotBlank(version)) {
        result.put(versionDict.get(version), count);
      }
    });

    if (MapUtils.isEmpty(result)) {
      result.putAll(
          versionDict.entrySet().stream().collect(Collectors.toMap(Entry::getValue, item -> 0L)));
    }
    return result;
  }

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
  protected ProtocolDhcpLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolDhcpLogDO protocolDhcpLogDO = new ProtocolDhcpLogDO();

    convertBaseLogMap2AbstractLogDO(protocolDhcpLogDO, map);

    protocolDhcpLogDO.setFlowId(String.valueOf(MapUtils.getLong(map, "flow_id")));
    protocolDhcpLogDO.setStartTime(MapUtils.getString(map, "start_time"));
    Inet4Address srcIpv4 = (Inet4Address) map.get("src_ipv4");
    protocolDhcpLogDO.setSrcIpv4(srcIpv4 != null ? srcIpv4.getHostAddress() : null);
    Inet6Address srcIpv6 = (Inet6Address) map.get("src_ipv6");
    protocolDhcpLogDO.setSrcIpv6(srcIpv6 != null ? srcIpv6.getHostAddress() : null);
    Inet4Address destIpv4 = (Inet4Address) map.get("dest_ipv4");
    protocolDhcpLogDO.setDestIpv4(destIpv4 != null ? destIpv4.getHostAddress() : null);
    Inet6Address destIpv6 = (Inet6Address) map.get("dest_ipv6");
    protocolDhcpLogDO.setDestIpv6(destIpv6 != null ? destIpv6.getHostAddress() : null);
    protocolDhcpLogDO.setSrcPort(MapUtils.getIntValue(map, "src_port"));
    protocolDhcpLogDO.setDestPort(MapUtils.getIntValue(map, "dest_port"));
    protocolDhcpLogDO.setVersion(MapUtils.getIntValue(map, "version"));
    protocolDhcpLogDO.setSrcMac(MapUtils.getString(map, "src_mac"));
    protocolDhcpLogDO.setDestMac(MapUtils.getString(map, "dest_mac"));
    protocolDhcpLogDO.setMessageType(MapUtils.getIntValue(map, "message_type"));
    protocolDhcpLogDO.setTransactionId(MapUtils.getString(map, "transaction_id"));
    List<Integer> parameterList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("parameters") != null) {
      Integer[] parameterArray = JsonHelper.deserialize(JsonHelper.serialize(map.get("parameters")),
          new TypeReference<Integer[]>() {
          }, false);
      if (parameterArray != null) {
        parameterList = Lists.newArrayList(parameterArray);
      }
    }
    protocolDhcpLogDO.setParameters(parameterList);
    Inet4Address offeredIpv4 = (Inet4Address) map.get("offered_ipv4_address");
    protocolDhcpLogDO
        .setOfferedIpv4Address(offeredIpv4 != null ? offeredIpv4.getHostAddress() : null);
    Inet6Address offeredIpv6 = (Inet6Address) map.get("offered_ipv6_address");
    protocolDhcpLogDO
        .setOfferedIpv6Address(offeredIpv6 != null ? offeredIpv6.getHostAddress() : null);
    protocolDhcpLogDO.setUpstreamBytes(MapUtils.getLongValue(map, "upstream_bytes"));
    protocolDhcpLogDO.setDownstreamBytes(MapUtils.getLongValue(map, "downstream_bytes"));
    return protocolDhcpLogDO;
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
  protected ProtocolDhcpLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" version = :keyword ");
    conditionSql.append(" or src_mac = :keyword ");
    conditionSql.append(" or dest_mac = :keyword");
    conditionSql.append(" or message_type = :keyword ");
    conditionSql.append(" or transaction_id = :keyword ");
    conditionSql.append(" or has(parameters, :keyword)=1 ");
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
