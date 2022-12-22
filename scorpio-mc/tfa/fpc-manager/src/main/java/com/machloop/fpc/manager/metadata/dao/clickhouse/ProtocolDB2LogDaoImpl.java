package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.AbstractLogRecordDO;
import com.machloop.fpc.manager.metadata.data.ProtocolDB2LogDO;
import com.machloop.fpc.manager.metadata.data.ProtocolSocks4LogDO;
import com.machloop.fpc.manager.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

/**
 * @author minjiajun
 *
 * create at 2022年8月19日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolDB2LogDao")
public class ProtocolDB2LogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolDB2LogDO>
    implements LogRecordDao<ProtocolDB2LogDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolDB2LogDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_protocol_db2_log_record";

  private static final List<String> OTHER_FIELD_NAMES = Lists.newArrayList("code_point", "data");

  private static final ProtocolDB2LogDO EMPTY_DO = new ProtocolDB2LogDO();

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
  protected ProtocolDB2LogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolDB2LogDO protocolDB2LogDO = new ProtocolDB2LogDO();

    convertBaseLogMap2AbstractLogDO(protocolDB2LogDO, map);

    protocolDB2LogDO.setCodePoint(MapUtils.getString(map, "code_point"));
    Map<String, String> data = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (map.get("data") != null) {
      Map<String, String> dataMap = JsonHelper.deserialize(JsonHelper.serialize(map.get("data")),
          new TypeReference<Map<String, String>>() {
          }, false);
      if (dataMap != null) {
        data.putAll(dataMap);
      }
    }
    protocolDB2LogDO.setData(data);
    return protocolDB2LogDO;
  }

  protected void convertBaseLogMap2AbstractLogDO(AbstractLogRecordDO abstractLogRecordDO,
      Map<String, Object> map) {
    if (map.containsKey("start_time")) {
      abstractLogRecordDO.setStartTime(DateUtils
          .toStringNanoISO8601((OffsetDateTime) map.get("start_time"), ZoneId.systemDefault()));
    }
    if (map.containsKey("end_time")) {
      abstractLogRecordDO.setEndTime(DateUtils
          .toStringNanoISO8601((OffsetDateTime) map.get("end_time"), ZoneId.systemDefault()));
    }
    abstractLogRecordDO.setPolicyName(MapUtils.getString(map, "policy_name"));
    abstractLogRecordDO.setLevel(MapUtils.getString(map, "level"));
    abstractLogRecordDO.setFlowId(String.valueOf(MapUtils.getLong(map, "flow_id")));
    abstractLogRecordDO.setApplicationId(MapUtils.getString(map, "application_id"));

    List<String> networkIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("network_id") != null) {
      String[] networkIdArray = JsonHelper.deserialize(JsonHelper.serialize(map.get("network_id")),
          new TypeReference<String[]>() {
          }, false);
      if (networkIdArray != null) {
        networkIdList = Lists.newArrayList(networkIdArray);
      }
    }
    abstractLogRecordDO.setNetworkId(networkIdList);
    List<String> serviceIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("service_id") != null) {
      String[] serviceIdArray = JsonHelper.deserialize(JsonHelper.serialize(map.get("service_id")),
          new TypeReference<String[]>() {
          }, false);
      if (serviceIdArray != null) {
        serviceIdList = Lists.newArrayList(serviceIdArray);
      }
    }
    abstractLogRecordDO.setServiceId(serviceIdList);

    if (map.containsKey("srcIp") || map.containsKey("src_ip")) {
      String srcIp = map.containsKey("srcIp") ? MapUtils.getString(map, "srcIp")
          : MapUtils.getString(map, "src_ip");
      if (StringUtils.isNotBlank(srcIp)
          && StringUtils.startsWith(srcIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
        srcIp = StringUtils.substringAfter(srcIp, ManagerConstants.IPV4_TO_IPV6_PREFIX);
      }
      abstractLogRecordDO.setSrcIp(srcIp);
    }

    if (map.containsKey("destIp") || map.containsKey("dest_ip")) {
      String destIp = map.containsKey("destIp") ? MapUtils.getString(map, "destIp")
          : MapUtils.getString(map, "dest_ip");
      if (StringUtils.isNotBlank(destIp)
          && StringUtils.startsWith(destIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
        destIp = StringUtils.substringAfter(destIp, ManagerConstants.IPV4_TO_IPV6_PREFIX);
      }
      abstractLogRecordDO.setDestIp(destIp);
    }

    abstractLogRecordDO.setSrcPort(MapUtils.getIntValue(map, "src_port"));
    abstractLogRecordDO.setDestPort(MapUtils.getIntValue(map, "dest_port"));
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
  protected ProtocolDB2LogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" code_point = :keyword ");
    conditionSql.append(" or data = :keyword ");
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

  public Page<ProtocolDB2LogDO> queryLogRecords(LogRecordQueryVO queryVO, List<String> ids,
      Pageable page) {

    // 数据源
    String tableName = convertTableName(queryVO);

    // 排序字段
    Set<String> sortProperties = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    Iterator<Order> iterator = page.getSort().iterator();
    while (iterator.hasNext()) {
      sortProperties.add(iterator.next().getProperty());
    }

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    enrichWhereSql(queryVO, whereSql, innerParams);

    boolean isFilterSrcIp = whereSql.toString().contains("src_ip");
    boolean isFilterDestIp = whereSql.toString().contains("dest_ip");

    boolean isFilterIp = true;
    if (!isFilterSrcIp && !isFilterDestIp) {
      isFilterIp = false;
    }

    StringBuilder innerSelectSql = new StringBuilder();
    innerSelectSql.append(" select ")
        .append(addConditions(isFilterIp, sortProperties, queryVO.getColumns()));
    innerSelectSql.append(" from ").append(tableName);

    innerSelectSql.append(whereSql);
    PageUtils.appendPage(innerSelectSql, page, Lists.newArrayList("start_time", "flow_id"));

    List<Map<String, Object>> allResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());
    List<ProtocolDB2LogDO> resultDOList = allResult.stream().map(item -> convertLogMap2LogDO(item))
        .collect(Collectors.toList());

    return new PageImpl<ProtocolDB2LogDO>(resultDOList, page, 0);
  }

  private String addConditions(boolean isFilterIp, Set<String> sortPropertys, String columns) {
    String resultString = "";
    if (StringUtils.equals(columns, "*")) {
      resultString = "level,policy_name,flow_id,network_id,service_id,application_id,start_time,end_time,"
          + "IPv6NumToString(src_ip) as src_ip,src_port,IPv6NumToString(dest_ip) as dest_ip,dest_port,"
          + "code_point, data";
    }

    // 必须查询排序字段
    sortPropertys = sortPropertys.stream()
        .filter(sortProperty -> !StringUtils.contains(columns, sortProperty))
        .collect(Collectors.toSet());
    String selectFields = CsvUtils.convertCollectionToCSV(sortPropertys);

    if (StringUtils.isBlank(columns)) {
      return selectFields;
    } else {
      resultString = (StringUtils.isNotBlank(selectFields) ? selectFields + "," : "") + columns;
      resultString = StringUtils.replace(resultString, "src_ip",
          "IPv6NumToString(src_ip) as src_ip");
      resultString = StringUtils.replace(resultString, "dest_ip",
          "IPv6NumToString(dest_ip) as dest_ip");
    }

    if (isFilterIp) {
      resultString = StringUtils.replace(resultString, "as src_ip", "as srcIp");
      resultString = StringUtils.replace(resultString, "as dest_ip", "as destIp");
    }

    return resultString;
  }

  @Override
  protected String buildSelectStatement(String columns, Set<String> sortPropertys) {
    return addConditions(false, sortPropertys, columns);
  }
}
