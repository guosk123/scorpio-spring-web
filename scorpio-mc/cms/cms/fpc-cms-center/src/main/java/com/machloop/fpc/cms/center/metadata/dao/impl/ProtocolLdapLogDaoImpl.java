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
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.data.ProtocolLdapLogDO;
import com.machloop.fpc.cms.center.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;

/**
 * @author chenshimiao
 *
 * create at 2022/8/1 10:57 AM,cms
 * @version 1.0
 */
@SuppressWarnings("unused")
@Repository("protocolLdapLogDao")
public class ProtocolLdapLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolLdapLogDO>
    implements LogRecordDao<ProtocolLdapLogDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolLdapLogDaoImpl.class);

  private static final String TABLE_NAME = "d_fpc_protocol_ldap_log_record";

  private static final ProtocolLdapLogDO EMPTY_DO = new ProtocolLdapLogDO();

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  @Autowired
  private DictManager dictManager;

  ProtocolLdapLogDO protocolLdapLogDo = new ProtocolLdapLogDO();

  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  @Override
  protected String getTableName() {
    return TABLE_NAME;
  }

  @Override
  protected ProtocolLdapLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolLdapLogDO protocolLdapLogDO = new ProtocolLdapLogDO();

    convertBaseLogMap2AbstractLogDO(protocolLdapLogDO, map);

    protocolLdapLogDO.setOpType(MapUtils.getIntValue(map, "op_type"));
    protocolLdapLogDO.setResStatus(MapUtils.getIntValue(map, "res_status"));
    Map<String, String> reqContentMap = new HashMap<>();
    if (map.get("req_content") != null) {
      Map<String, String> reqContentMap1 = JsonHelper.deserialize(
          JsonHelper.serialize(map.get("req_content")), new TypeReference<Map<String, String>>() {
          }, false);
      if (reqContentMap1 != null) {
        reqContentMap.putAll(reqContentMap1);
      }
    }
    protocolLdapLogDO.setReqContent(reqContentMap);
    HashMap<String, String> resContentMap = new HashMap<>();
    if (map.get("res_content") != null) {
      Map<String, String> resContentMap1 = JsonHelper.deserialize(
          JsonHelper.serialize(map.get("res_content")), new TypeReference<Map<String, String>>() {
          }, false);
      if (resContentMap1 != null) {
        resContentMap.putAll(resContentMap1);
      }
    }
    protocolLdapLogDO.setResContent(resContentMap);
    return protocolLdapLogDO;
  }

  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }

  @Override
  protected ProtocolLdapLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" policy_name = :keyword");
    return conditionSql.toString();
  }

  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" 1=2 ");
    return conditionSql.toString();
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
            NetworkUtils.isInetAddress(queryVO.getSrcIp(), NetworkUtils.IpVersion.V4)
                ? CenterConstants.IPV4_TO_IPV6_PREFIX + queryVO.getSrcIp()
                : queryVO.getSrcIp());
      } else if (NetworkUtils.isCidr(queryVO.getSrcIp())) {

        whereSql.append(" and (src_ip between IPv6CIDRToRange(toIPv6(:ip_address), :cidr).1 ");
        whereSql.append(" and IPv6CIDRToRange(toIPv6(:ip_address), :cidr).2)");
        String[] ipAndCidr = queryVO.getSrcIp().split("/");
        params.put("ip_address",
            NetworkUtils.isInetAddress(queryVO.getSrcIp(), NetworkUtils.IpVersion.V4)
                ? CenterConstants.IPV4_TO_IPV6_PREFIX + ipAndCidr[0]
                : ipAndCidr[0]);
        params.put("cidr",
            NetworkUtils.isCidr(queryVO.getSrcIp(), NetworkUtils.IpVersion.V4)
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
        StringUtils.substringBefore(tableName, "_log_record"), "d_fpc_protocol_"));

    Map<String, Long> result = Maps.newHashMapWithExpectedSize(1);
    result.put(protocol, count);
    return result;
  }

  @Override
  protected String buildSelectStatement(String columns, Set<String> sortPropertys) {
    if (StringUtils.equals(columns, "*")) {
      return "level,policy_name,flow_id,network_id,service_id,application_id,start_time,end_time,"
          + "IPv6NumToString(src_ip) as src_ip,src_port,IPv6NumToString(dest_ip) as dest_ip,dest_port,"
          + "op_type, res_status, req_content, res_content";
    }

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
      return result;
    }
  }
}
