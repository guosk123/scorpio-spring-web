package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolSocks4LogDO;
import com.machloop.fpc.manager.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

/**
 * @author minjiajun
 *
 * create at 2022年5月30日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolSocks4LogDao")
public class ProtocolSocks4LogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolSocks4LogDO>
    implements LogRecordDao<ProtocolSocks4LogDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolSocks4LogDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_protocol_socks4_log_record";

  private static final List<String> OTHER_FIELD_NAMES = Lists.newArrayList("cmd",
      "request_remote_port", "request_remote_ip", "user_id", "domain_name", "cmd_result",
      "response_remote_ip", "response_remote_port");

  private static final ProtocolSocks4LogDO EMPTY_DO = new ProtocolSocks4LogDO();

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
  protected ProtocolSocks4LogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolSocks4LogDO protocolSocks4LogDO = new ProtocolSocks4LogDO();

    convertBaseLogMap2AbstractLogDO(protocolSocks4LogDO, map);

    protocolSocks4LogDO.setCmd(MapUtils.getString(map, "cmd"));
    protocolSocks4LogDO.setRequestRemotePort(MapUtils.getString(map, "request_remote_port"));
    String requestIp = MapUtils.getString(map, "request_remote_ip");
    if (StringUtils.isNotBlank(requestIp)
        && StringUtils.startsWith(requestIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
      requestIp = StringUtils.substringAfter(requestIp, ManagerConstants.IPV4_TO_IPV6_PREFIX);
    }
    protocolSocks4LogDO.setRequestRemoteIp(requestIp);
    protocolSocks4LogDO.setUserId(MapUtils.getString(map, "user_id"));
    protocolSocks4LogDO.setDomainName(MapUtils.getString(map, "domain_name"));
    protocolSocks4LogDO.setCmdResult(MapUtils.getString(map, "cmd_result"));
    String responseIp = MapUtils.getString(map, "response_remote_ip");
    if (StringUtils.isNotBlank(responseIp)
        && StringUtils.startsWith(responseIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
      responseIp = StringUtils.substringAfter(responseIp, ManagerConstants.IPV4_TO_IPV6_PREFIX);
    }
    protocolSocks4LogDO.setResponseRemoteIp(responseIp);
    protocolSocks4LogDO.setResponseRemotePort(MapUtils.getString(map, "response_remote_port"));
    protocolSocks4LogDO.setChannelState(MapUtils.getIntValue(map, "channel_state"));
    return protocolSocks4LogDO;
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
  protected ProtocolSocks4LogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" cmd = :keyword ");
    conditionSql.append(" or request_remote_port = :keyword ");
    conditionSql.append(" or IPv6NumToString(request_remote_ip) = :keyword ");
    conditionSql.append(" or user_id = :keyword ");
    conditionSql.append(" or domain_name = :keyword ");
    conditionSql.append(" or cmd_result = :keyword ");
    conditionSql.append(" or IPv6NumToString(response_remote_ip) = :keyword ");
    conditionSql.append(" or response_remote_port = :keyword ");
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

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#buildSelectStatement(java.lang.String, java.util.Set)
   */
  @Override
  protected String buildSelectStatement(String columns, Set<String> sortPropertys) {
    if (StringUtils.equals(columns, "*")) {
      return "level,policy_name,flow_id,network_id,service_id,application_id,start_time,end_time,"
          + "IPv6NumToString(src_ip) as src_ip,src_port,IPv6NumToString(dest_ip) as dest_ip,dest_port,"
          + "cmd,request_remote_port,IPv6NumToString(request_remote_ip) as request_remote_ip,user_id,"
          + "domain_name,cmd_result,IPv6NumToString(response_remote_ip) as response_remote_ip,response_remote_port,"
          + "channel_state";
    }

    // 必须查询排序字段
    sortPropertys = sortPropertys.stream()
        .filter(sortProperty -> !StringUtils.contains(columns, sortProperty))
        .collect(Collectors.toSet());
    String selectFields = CsvUtils.convertCollectionToCSV(sortPropertys);

    if (StringUtils.isBlank(columns)) {
      return selectFields;
    } else {
      String result = (StringUtils.isNotBlank(selectFields) ? selectFields + "," : "") + columns;
      result = StringUtils.replace(result, "src_ip", "IPv6NumToString(src_ip) as src_ip");
      result = StringUtils.replace(result, "dest_ip", "IPv6NumToString(dest_ip) as dest_ip");
      result = StringUtils.replace(result, "request_remote_ip",
          "IPv6NumToString(request_remote_ip) as request_remote_ip");
      result = StringUtils.replace(result, "response_remote_ip",
          "IPv6NumToString(response_remote_ip) as response_remote_ip");
      return result;
    }
  }

  public Map<String, Long> countLogRecords(LogCountQueryVO queryVO) {
    LogRecordQueryVO logRecordQueryVO = new LogRecordQueryVO();
    logRecordQueryVO.setSourceType(queryVO.getSourceType());
    logRecordQueryVO.setPacketFileId(queryVO.getPacketFileId());
    StringBuilder countSql = new StringBuilder();
    countSql.append("select count(1) from ");
    String tableName = convertTableName(logRecordQueryVO);
    countSql.append(tableName);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where 1=1 ");

    if (queryVO.getStartTimeDate() != null && queryVO.getEndTimeDate() != null) {
      whereSql.append(String.format(" and start_time %s toDateTime64(:start_time, 9, 'UTC') ",
          logRecordQueryVO.getIncludeStartTime() ? ">=" : ">"));
      whereSql.append(String.format(" and start_time %s toDateTime64(:end_time, 9, 'UTC') ",
          logRecordQueryVO.getIncludeEndTime() ? "<=" : "<"));
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
        whereSql.append(" and src_ip = toIPv6(:src_ip) ");
        params.put("src_ip",
            NetworkUtils.isInetAddress(queryVO.getSrcIp(), IpVersion.V4)
                ? ManagerConstants.IPV4_TO_IPV6_PREFIX + queryVO.getSrcIp()
                : queryVO.getSrcIp());
      } else if (NetworkUtils.isCidr(queryVO.getSrcIp())) {

        whereSql.append(" and (src_ip between IPv6CIDRToRange(toIPv6(:ip_address), :cidr).1 ");
        whereSql.append(" and IPv6CIDRToRange(toIPv6(:ip_address), :cidr).2)");
        String[] ipAndCidr = queryVO.getSrcIp().split("/");
        params.put("ip_address",
            NetworkUtils.isInetAddress(queryVO.getSrcIp(), IpVersion.V4)
                ? ManagerConstants.IPV4_TO_IPV6_PREFIX + ipAndCidr[0]
                : ipAndCidr[0]);
        params.put("cidr",
            NetworkUtils.isCidr(queryVO.getSrcIp(), IpVersion.V4)
                ? Integer.parseInt(ipAndCidr[1]) + 96
                : Integer.parseInt(ipAndCidr[1]));
      } else {
        whereSql.append(" and 1=2 ");
      }
    }
    countSql.append(whereSql);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("count {} logs, sql: {}, params: {}", convertTableName(logRecordQueryVO),
          countSql.toString(), params);
    }

    Long count = queryForLongWithExceptionHandle(countSql.toString(), params);

    String protocol = StringUtils.upperCase(StringUtils.substringAfterLast(
        StringUtils.substringBefore(tableName, "_log_record"), "t_fpc_protocol_"));

    Map<String, Long> result = Maps.newHashMapWithExpectedSize(1);
    result.put(protocol, count);
    return result;
  }

}
