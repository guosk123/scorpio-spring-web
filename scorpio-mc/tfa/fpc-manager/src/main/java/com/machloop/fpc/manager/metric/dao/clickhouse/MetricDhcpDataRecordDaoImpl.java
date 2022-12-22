package com.machloop.fpc.manager.metric.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.common.helper.AggsFunctionEnum;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metric.dao.MetricDhcpDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricDhcpDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author  fengtianyou
 *
 * create at 2021年8月25日, fpc-manager
 */
@Repository
public class MetricDhcpDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
    implements MetricDhcpDataRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricDhcpDataRecordDaoImpl.class);

  private static final Map<String, String> SERVER_TERM_KEY;
  private static final Map<String, String> CLIENT_TERM_KEY;
  private static final Map<String, String> MESSAGE_TERM_KEY;
  private static final Map<String, String> AGGS_TERM_KEY;
  static {
    SERVER_TERM_KEY = new HashMap<String, String>();
    CLIENT_TERM_KEY = new HashMap<String, String>();
    MESSAGE_TERM_KEY = new HashMap<String, String>();
    AGGS_TERM_KEY = new HashMap<String, String>();
    SERVER_TERM_KEY.put("dhcp_version", "dhcpVersion");
    SERVER_TERM_KEY.put("server_ip_address", "serverIpAddress");
    SERVER_TERM_KEY.put("server_mac_address", "serverMacAddress");
    CLIENT_TERM_KEY.put("dhcp_version", "dhcpVersion");
    CLIENT_TERM_KEY.put("client_ip_address", "clientIpAddress");
    CLIENT_TERM_KEY.put("client_mac_address", "clientMacAddress");
    MESSAGE_TERM_KEY.put("dhcp_version", "dhcpVersion");
    MESSAGE_TERM_KEY.put("message_type", "messageType");
    AGGS_TERM_KEY.put("network_id", "networkId");
    AGGS_TERM_KEY.put("dhcp_version", "dhcpVersion");
    AGGS_TERM_KEY.put("server_ip_address", "serverIpAddress");
    AGGS_TERM_KEY.put("server_mac_address", "serverMacAddress");
    AGGS_TERM_KEY.put("client_ip_address", "clientIpAddress");
    AGGS_TERM_KEY.put("client_mac_address", "clientMacAddress");
    AGGS_TERM_KEY.put("message_type", "messageType");
  }

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  @Autowired
  private ClickHouseStatsJdbcTemplate jdbcTemplate;

  @Autowired
  private PacketAnalysisSubTaskDao offlineAnalysisSubTaskDao;

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#getSpl2SqlHelper()
   */
  @Override
  protected Spl2SqlHelper getSpl2SqlHelper() {
    return spl2SqlHelper;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#getClickHouseJdbcTemplate()
   */
  @Override
  protected ClickHouseStatsJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#getOfflineAnalysisSubTaskDao()
   */
  @Override
  protected PacketAnalysisSubTaskDao getOfflineAnalysisSubTaskDao() {
    return offlineAnalysisSubTaskDao;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#getTableName()
   */
  @Override
  protected String getTableName() {
    return ManagerConstants.TABLE_METRIC_DHCP_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricDhcpDataRecordDao#queryMetricDhcpRawdatas(com.machloop.fpc.manager.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricDhcpRawdatas(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      // 构造过滤条件
      StringBuilder whereSql = new StringBuilder();
      Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      enrichWhereSql(queryVO, whereSql, params);

      StringBuilder sql = new StringBuilder();
      sql.append("select * from ").append(convertTableName(queryVO.getSourceType(),
          queryVO.getInterval(), queryVO.getPacketFileId()));
      sql.append(whereSql.toString());
      sql.append(" order by timestamp ");
      sql.append(" limit ").append(
          Integer.parseInt(HotPropertiesHelper.getProperty("rest.metric.result.query.max.count")));

      List<Map<String, Object>> tempResult = getClickHouseJdbcTemplate().getJdbcTemplate()
          .query(sql.toString(), params, new ColumnMapRowMapper());

      result = tempResult.stream().map(item -> {
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(item.size());
        item.forEach((k, v) -> {
          map.put(TextUtils.underLineToCamel(k), v);
        });
        map.put("timestamp",
            DateUtils.transformDateString(MapUtils.getString(item, "timestamp"),
                QUERY_RESULT_DATE_PATTERN_OLD, ZoneId.of("UTC"), QUERY_RESULT_DATE_PATTERN_NEW,
                ZoneId.systemDefault()));

        return map;
      }).collect(Collectors.toList());
    } catch (Exception e) {
      LOGGER.warn("failed to query dhcp metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricDhcpDataRecordDao#queryMetricDhcps(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricDhcpDataRecordDO> queryMetricDhcps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String type) {
    List<
        MetricDhcpDataRecordDO> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      // 分组字段
      Map<String, String> termKeys = Maps.newHashMap();
      switch (type) {
        case FpcConstants.METRIC_TYPE_DHCP_SERVER:
          termKeys.putAll(SERVER_TERM_KEY);
          break;
        case FpcConstants.METRIC_TYPE_DHCP_CLIENT:
          termKeys.putAll(CLIENT_TERM_KEY);
          break;
        case FpcConstants.METRIC_TYPE_DHCP_MESSAGE_TYPE:
          termKeys.putAll(MESSAGE_TERM_KEY);
          break;
        default:
          return result;
      }

      // 聚合字段
      Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
          .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      setAggsFields(aggsFields);

      List<Map<String, Object>> batchResult = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, null, termKeys, aggsFields, sortProperty, sortDirection);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query dhcp metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricDhcpDataRecordDao#queryMetricDhcpHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.util.List, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricDhcpHistograms(MetricQueryVO queryVO,
      List<Tuple2<String, Boolean>> termFields, String aggsField,
      List<Map<String, Object>> combinationConditions) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 分组字段
    Map<String, String> termKeys = termFields.stream()
        .collect(Collectors.toMap(Tuple2::getT1, item -> TextUtils.underLineToCamel(item.getT1())));

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(allAggsFields);
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    getCombinationAggsFields(aggsField).forEach(item -> {
      if (allAggsFields.containsKey(item)) {
        aggsFields.put(item, allAggsFields.get(item));
      }
    });
    if (MapUtils.isEmpty(aggsFields)) {
      return result;
    }

    try {
      List<Map<String, Object>> batchResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, termKeys, aggsFields);
      result.addAll(batchResult);
    } catch (IOException e) {
      LOGGER.warn("failed to query dhcp histograms.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#aggregate(java.util.Date, java.util.Date, int, java.lang.String, java.lang.String)
   */
  @Override
  protected int aggregate(Date startTime, Date endTime, int interval, String inputTableName,
      String outputTableName) throws IOException {
    MetricQueryVO queryVO = new MetricQueryVO();
    queryVO.setStartTimeDate(startTime);
    queryVO.setEndTimeDate(endTime);
    queryVO.setInterval(interval);

    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields);

    int totalSize = 0;
    int offset = 0;
    int currentSize = 0;
    List<Map<String, Object>> batchList = null;
    do {
      batchList = termMetricAggregate(inputTableName, queryVO, null, AGGS_TERM_KEY, aggsFields,
          DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION, COMPOSITE_BATCH_SIZE, offset);
      batchList.forEach(item -> item.put("timestamp", endTime));
      totalSize += saveMetricDataRecord(batchList, outputTableName);

      currentSize = batchList.size();
      offset += currentSize;
      batchList = null;
    } while (currentSize == COMPOSITE_BATCH_SIZE && !GracefulShutdownHelper.isShutdownNow());

    return totalSize;
  }

  private int saveMetricDataRecord(List<Map<String, Object>> batchList, String outputTableName) {
    if (CollectionUtils.isEmpty(batchList)) {
      return 0;
    }

    batchList.forEach(item -> {
      item.put("timestamp", DateUtils.toStringFormat((Date) item.get("timestamp"),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(batchList);
    return Arrays.stream(
        jdbcTemplate.getJdbcTemplate().batchUpdate(batchUpdatesql(outputTableName), batchSource))
        .sum();
  }

  private void setAggsFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {
    aggsFields.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
    aggsFields.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
    aggsFields.put("sendBytes", Tuples.of(AggsFunctionEnum.SUM, "send_bytes"));
    aggsFields.put("sendPackets", Tuples.of(AggsFunctionEnum.SUM, "send_packets"));
    aggsFields.put("receiveBytes", Tuples.of(AggsFunctionEnum.SUM, "receive_bytes"));
    aggsFields.put("receivePackets", Tuples.of(AggsFunctionEnum.SUM, "receive_packets"));
  }

  private MetricDhcpDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricDhcpDataRecordDO recordDO = new MetricDhcpDataRecordDO();
    recordDO.setTimestamp((Date) item.get("timestamp"));
    recordDO.setNetworkId(MapUtils.getString(item, "networkId"));
    recordDO.setClientIpAddress(MapUtils.getString(item, "clientIpAddress"));
    recordDO.setServerIpAddress(MapUtils.getString(item, "serverIpAddress"));
    recordDO.setClientMacAddress(MapUtils.getString(item, "clientMacAddress"));
    recordDO.setServerMacAddress(MapUtils.getString(item, "serverMacAddress"));
    recordDO.setMessageType(MapUtils.getIntValue(item, "messageType"));
    recordDO.setDhcpVersion(MapUtils.getIntValue(item, "dhcpVersion"));
    recordDO.setTotalBytes(MapUtils.getLongValue(item, "totalBytes"));
    recordDO.setTotalPackets(MapUtils.getLongValue(item, "totalPackets"));
    recordDO.setSendBytes(MapUtils.getLongValue(item, "sendBytes"));
    recordDO.setSendPackets(MapUtils.getLongValue(item, "sendPackets"));
    recordDO.setReceiveBytes(MapUtils.getLongValue(item, "receiveBytes"));
    recordDO.setReceivePackets(MapUtils.getLongValue(item, "receivePackets"));

    return recordDO;
  }

  private String batchUpdatesql(String tableName) {
    StringBuilder sql = new StringBuilder();

    sql.append("insert into ");
    sql.append(tableName);
    sql.append("(");
    sql.append("server_mac_address,");
    sql.append("server_ip_address,");
    sql.append("dhcp_version,");
    sql.append("network_id,");
    sql.append("client_ip_address,");
    sql.append("client_mac_address,");
    sql.append("message_type,");
    sql.append("total_bytes,");
    sql.append("total_packets,");
    sql.append("send_bytes,");
    sql.append("send_packets,");
    sql.append("receive_bytes,");
    sql.append("receive_packets,");
    sql.append("timestamp)");
    sql.append(" values (");
    sql.append(":serverMacAddress,");
    sql.append(":serverIpAddress,");
    sql.append(":dhcpVersion,");
    sql.append(":networkId,");
    sql.append(":clientIpAddress,");
    sql.append(":clientMacAddress,");
    sql.append(":messageType,");
    sql.append(":totalBytes,");
    sql.append(":totalPackets,");
    sql.append(":sendBytes,");
    sql.append(":sendPackets,");
    sql.append(":receiveBytes,");
    sql.append(":receivePackets,");
    sql.append(":timestamp)");

    return sql.toString();
  }

}
