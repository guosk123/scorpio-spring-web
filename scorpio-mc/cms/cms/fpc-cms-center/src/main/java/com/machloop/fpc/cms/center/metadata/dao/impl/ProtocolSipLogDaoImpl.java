package com.machloop.fpc.cms.center.metadata.dao.impl;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.data.ProtocolSipLogDO;
import com.machloop.fpc.cms.center.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;

@Repository("protocolSipLogDao")
public class ProtocolSipLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolSipLogDO>
    implements LogRecordDao<ProtocolSipLogDO> {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolSipLogDaoImpl.class);

  private static final String TABLE_NAME = "d_fpc_protocol_sip_log_record";


  private static final ProtocolSipLogDO EMPTY_DO = new ProtocolSipLogDO();

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;


  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }


  @Override
  protected String getTableName() {
    return TABLE_NAME;
  }


  @Override
  protected ProtocolSipLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolSipLogDO protocolSipLogDO = new ProtocolSipLogDO();

    convertBaseLogMap2AbstractLogDO(protocolSipLogDO, map);

    protocolSipLogDO.setFrom(MapUtils.getString(map, "from"));
    protocolSipLogDO.setTo(MapUtils.getString(map, "to"));
    protocolSipLogDO.setIpProtocol(MapUtils.getString(map, "ip_protocol"));
    protocolSipLogDO.setType(MapUtils.getString(map, "type"));
    protocolSipLogDO.setSeqNum(MapUtils.getIntValue(map, "seq_num"));
    protocolSipLogDO.setCallId(MapUtils.getString(map, "call_id"));
    protocolSipLogDO.setRequestUri(MapUtils.getString(map, "request_uri"));
    protocolSipLogDO.setStatusCode(MapUtils.getString(map, "status_code"));
    Map<String, String> sdpMap = new HashMap<>();
    if (map.get("sdp") != null) {
      Map<String, String> sdpMap1 = JsonHelper.deserialize(JsonHelper.serialize(map.get("sdp")),
          new TypeReference<Map<String, String>>() {
          }, false);
      if (sdpMap1 != null) {
        sdpMap.putAll(sdpMap1);
      }
    }
    protocolSipLogDO.setSdp(sdpMap);
    return protocolSipLogDO;
  }

  @Override
  protected ProtocolSipLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }


  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" policy_name = :keyword ");
    conditionSql.append(" or from = :keyword ");
    conditionSql.append(" or to = :keyword ");
    conditionSql.append(" or ip_protocol = :keyword ");
    conditionSql.append(" or type = :keyword ");
    conditionSql.append(" or seq_num = :keyword ");
    conditionSql.append(" or call_id = :keyword ");
    conditionSql.append(" or request_uri = :keyword ");
    conditionSql.append(" or status_code = :keyword ");
    conditionSql.append(" or sdp = :keyword ");
    return conditionSql.toString();
  }

  @Override
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
        if (NetworkUtils.isInetAddress(queryVO.getSrcIp(), NetworkUtils.IpVersion.V4)) {
          params.put("src_ip", CenterConstants.IPV4_TO_IPV6_PREFIX + queryVO.getSrcIp());
        } else {
          params.put("src_ip", queryVO.getSrcIp());
        }
      } else if (NetworkUtils.isCidr(queryVO.getSrcIp())) {
        whereSql.append(" and (src_ipv6 between IPv6CIDRToRange(toIPv6(:ip_address), :cidr).1 ");
        whereSql.append(" and IPv6CIDRToRange(toIPv6(:ip_address), :cidr).2)");
        String[] ipAndCidr = queryVO.getSrcIp().split("/");
        if (NetworkUtils.isCidr(queryVO.getSrcIp(), NetworkUtils.IpVersion.V4)) {
          params.put("ip_address", CenterConstants.IPV4_TO_IPV6_PREFIX + ipAndCidr[0]);
          params.put("cidr", Integer.parseInt(ipAndCidr[1]) + 96);
        } else {
          params.put("ip_address", ipAndCidr[0]);
          params.put("cidr", Integer.parseInt(ipAndCidr[1]));
        }
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
        StringUtils.substringBefore(tableName, "_log_record"), "d_fpc_protocol_"));

    Map<String, Long> result = Maps.newHashMapWithExpectedSize(1);
    result.put(protocol, count);
    return result;
  }

  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" 1=2 ");
    return conditionSql.toString();
  }


  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }

  @Override
  protected String buildSelectStatement(String columns, Set<String> sortPropertys) {
    if (StringUtils.equals(columns, "*")) {
      return "level,policy_name,flow_id,network_id,service_id,application_id,start_time,end_time,"
          + "IPv6NumToString(src_ip) as src_ip,src_port,IPv6NumToString(dest_ip) as dest_ip,dest_port,from,to,ip_protocol,type,seq_num,call_id,request_uri,"
          + "status_code,sdp";
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

      return result.replace("src_ip", "IPv6NumToString(src_ip) as src_ip").replace("dest_ip",
          "IPv6NumToString(dest_ip) as dest_ip");
    }
  }
}
