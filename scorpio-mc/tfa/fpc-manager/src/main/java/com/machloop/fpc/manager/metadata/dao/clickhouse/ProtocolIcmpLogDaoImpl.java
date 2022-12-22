package com.machloop.fpc.manager.metadata.dao.clickhouse;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolIcmpLogDO;
import com.machloop.fpc.manager.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolIcmpLogDao")
public class ProtocolIcmpLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolIcmpLogDO>
    implements LogRecordDao<ProtocolIcmpLogDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolIcmpLogDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_protocol_icmp_log_record";

  private static final List<String> OTHER_FIELD_NAMES = Lists.newArrayList("policy_name");

  private static final ProtocolIcmpLogDO EMPTY_DO = new ProtocolIcmpLogDO();

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  @Autowired
  private DictManager dictManager;

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

    Map<String, String> versionDict = dictManager.getBaseDict().getItemMap("protocol_icmp_version");

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
   * @see com.machloop.fpc.manager.metadata.dao.clickhouse.AbstractLogRecordDaoImpl#convertLogMap2LogDO(java.util.Map)
   */
  @Override
  protected ProtocolIcmpLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolIcmpLogDO protocolIcmpLogDO = new ProtocolIcmpLogDO();

    convertBaseLogMap2AbstractLogDO(protocolIcmpLogDO, map);

    protocolIcmpLogDO.setVersion(MapUtils.getIntValue(map, "version"));
    protocolIcmpLogDO.setResult(MapUtils.getString(map, "result"));
    protocolIcmpLogDO.setRequestDataLen(MapUtils.getLongValue(map, "request_data_len"));
    protocolIcmpLogDO.setResponseDataLen(MapUtils.getLongValue(map, "response_data_len"));
    protocolIcmpLogDO.setOnlyRequest(MapUtils.getIntValue(map, "only_request"));
    protocolIcmpLogDO.setOnlyResponse(MapUtils.getIntValue(map, "only_response"));
    protocolIcmpLogDO
        .setPayloadHashInconsistent(MapUtils.getInteger(map, "payload_hash_inconsistent"));
    return protocolIcmpLogDO;
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
  protected ProtocolIcmpLogDO buildEmptyLogDO() {
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
    conditionSql.append(" 1=2 ");
    return conditionSql.toString();
  }

}
