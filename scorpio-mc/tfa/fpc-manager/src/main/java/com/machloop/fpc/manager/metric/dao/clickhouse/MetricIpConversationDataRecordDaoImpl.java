package com.machloop.fpc.manager.metric.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.common.helper.AggsFunctionEnum;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metric.dao.MetricIpConversationDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricIpConversationDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 *
 * create at 2021年8月17日, fpc-manager
 */
@Repository
public class MetricIpConversationDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
    implements MetricIpConversationDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricIpConversationDataRecordDaoImpl.class);

  private static final Map<String, String> PRIMARY_TERM_KEY;
  private static final Map<String, String> AGGS_TERM_KEY;
  static {
    PRIMARY_TERM_KEY = new HashMap<String, String>();
    AGGS_TERM_KEY = new HashMap<String, String>();
    PRIMARY_TERM_KEY.put("ip_a_address", "ipAAddress");
    PRIMARY_TERM_KEY.put("ip_b_address", "ipBAddress");
    AGGS_TERM_KEY.put("network_id", "networkId");
    AGGS_TERM_KEY.put("service_id", "serviceId");
    AGGS_TERM_KEY.put("ip_a_address", "ipAAddress");
    AGGS_TERM_KEY.put("ip_b_address", "ipBAddress");
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
    return ManagerConstants.TABLE_METRIC_IP_CONVERSATION_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricIpConversationDataRecordDao#queryMetricIpConversationRawdatas(com.machloop.fpc.manager.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversationRawdatas(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      result = queryMetricDataRecord(queryVO);
    } catch (IOException e) {
      LOGGER.warn("failed to query ipConversation metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricIpConversationDataRecordDao#queryMetricIpConversations(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricIpConversationDataRecordDO> queryMetricIpConversations(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<MetricIpConversationDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields);
    setSpecialKpiAggs(aggsFields);

    try {
      List<Map<String, Object>> batchResult = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, null, PRIMARY_TERM_KEY, aggsFields, sortProperty, sortDirection);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query ipConversation metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricIpConversationDataRecordDao#graphMetricIpConversations(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> graphMetricIpConversations(MetricQueryVO queryVO,
      Integer minEstablishedSessions, Integer minTotalBytes, String sortProperty,
      String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      // 标识查询，用于取消查询
      String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
          ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
          : "";

      // 构造过滤条件
      StringBuilder whereSql = new StringBuilder();
      Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      enrichWhereSql(queryVO, whereSql, params);

      // 聚合字段
      Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
          .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      aggsFields.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
      aggsFields.put("establishedSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));

      // 构造查询语句
      StringBuilder sql = new StringBuilder(securityQueryId);
      // 分组字段
      List<String> terms = PRIMARY_TERM_KEY.entrySet().stream()
          .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
      sql.append(" select ").append(StringUtils.join(terms, ","));
      // 聚合字段
      aggsFields.entrySet().forEach(entry -> {
        sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
      });
      sql.append(" from ").append(convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
          queryVO.getPacketFileId()));
      sql.append(whereSql.toString());
      sql.append(" group by ").append(StringUtils.join(PRIMARY_TERM_KEY.values(), ","));
      sql.append(" having 1=1 ");
      if (minEstablishedSessions != null) {
        sql.append(" and establishedSessions >= :minEstablishedSessions ");
        params.put("minEstablishedSessions", minEstablishedSessions);
      }
      if (minTotalBytes != null) {
        sql.append(" and totalBytes >= :minTotalBytes ");
        params.put("minTotalBytes", minTotalBytes);
      }
      sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
          .append(sortDirection);
      if (queryVO.getCount() > 0) {
        sql.append(" limit ").append(queryVO.getCount());
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("graphMetricIpConversations sql : [{}], param: [{}] ", sql.toString(), params);
      }

      result = queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
    } catch (BusinessException e) {
      throw e;
    } catch (RuntimeException e) {
      LOGGER.warn("failed to graph ipConversation metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricIpConversationDataRecordDao#countMetricIpConversations(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> countMetricIpConversations(MetricQueryVO queryVO,
      String aggsField, String sortProperty, String sortDirection, List<String> networkIds) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(allAggsFields);
    setSpecialKpiAggs(allAggsFields);
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    getCombinationAggsFields(aggsField).forEach(item -> {
      if (allAggsFields.containsKey(item)) {
        aggsFields.put(item, allAggsFields.get(item));
      }
    });
    aggsFields.put("upstreamBytes", allAggsFields.get("upstreamBytes"));
    aggsFields.put("downstreamBytes", allAggsFields.get("downstreamBytes"));

    // 附加过滤条件
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(networkIds)) {
      for (String item : networkIds) {
        Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        itemMap.put("network_id", item);
        itemMap.put("service_id", "");
        combinationConditions.add(itemMap);
      }
    } else {
      Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      String networkId = StringUtils.equals(queryVO.getSourceType(),
          FpcConstants.SOURCE_TYPE_PACKET_FILE) ? queryVO.getPacketFileId()
              : queryVO.getNetworkId();
      itemMap.put("network_id", networkId);
      itemMap.put("service_id", StringUtils.defaultIfBlank(queryVO.getServiceId(), ""));
      combinationConditions.add(itemMap);
    }

    try {
      result = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, PRIMARY_TERM_KEY, aggsFields, sortProperty,
          sortDirection);
    } catch (IOException e) {
      LOGGER.warn("failed to count ipConversation metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricIpConversationDataRecordDao#queryMetricIpConversationHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversationHistograms(MetricQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(allAggsFields);
    setSpecialKpiAggs(allAggsFields);
    final Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (aggsField.contains(ManagerConstants.METRIC_NPM_ALL_AGGSFILED)) {
      aggsFields.putAll(allAggsFields);
    } else {
      getCombinationAggsFields(aggsField).forEach(item -> {
        if (allAggsFields.containsKey(item)) {
          aggsFields.put(item, allAggsFields.get(item));
        }
      });
    }
    if (MapUtils.isEmpty(aggsFields)) {
      return result;
    }

    try {
      List<Map<String, Object>> batchResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, PRIMARY_TERM_KEY, aggsFields);
      result.addAll(batchResult);
    } catch (IOException e) {
      LOGGER.warn("failed to query ipConversation histograms.", e);
    }

    return result;
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationIpConversations(
      MetricQueryVO queryVO, String sortProperty, String sortDirection) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(" select network_id as networkId ");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    kpiAggs(aggsFields);
    if (!StringUtils.equals(queryVO.getColumns(), "*")) {
      // 过滤要查询的列
      Set<String> fields = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      CsvUtils.convertCSVToList(queryVO.getColumns()).forEach(
          field -> fields.addAll(getCombinationAggsFields(TextUtils.camelToUnderLine(field))));
      fields.addAll(getCombinationAggsFields(sortProperty));

      aggsFields = aggsFields.entrySet().stream()
          .filter(aggsField -> fields.contains(aggsField.getKey()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    aggsFields.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
        queryVO.getPacketFileId()));
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);
    List<String> networkIdList = CsvUtils.convertCSVToList(queryVO.getNetworkId());
    if (CollectionUtils.isNotEmpty(networkIdList)) {
      whereSql.append(" and network_id in (:networkIds)");
      params.put("networkIds", networkIdList);
    }
    sql.append(whereSql);
    sql.append(" group by networkId ");
    sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
        .append(sortDirection);
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    sortMetricResult(result, sortProperty, sortDirection);

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
    kpiAggs(aggsFields);
    aggsFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    aggsFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    aggsFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    aggsFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    aggsFields.put("activeEstablishedSessions",
        Tuples.of(AggsFunctionEnum.SUM, "active_established_sessions"));
    aggsFields.put("passiveEstablishedSessions",
        Tuples.of(AggsFunctionEnum.SUM, "passive_established_sessions"));
  }

  private void setSpecialKpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {
    specialKpiAggs(aggsFields);
  }

  private MetricIpConversationDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricIpConversationDataRecordDO recordDO = new MetricIpConversationDataRecordDO();
    String IpAAddress = MapUtils.getString(item, "ipAAddress");
    recordDO.setIpAAddress(StringUtils.equals(IpAAddress, "null") ? null : IpAAddress);
    String IpBAddress = MapUtils.getString(item, "ipBAddress");
    recordDO.setIpBAddress(StringUtils.equals(IpBAddress, "null") ? null : IpBAddress);

    tranKPIMapToDateRecord(item, recordDO);
    recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "downstreamBytes"));
    recordDO.setDownstreamPackets(MapUtils.getLongValue(item, "downstreamPackets"));
    recordDO.setUpstreamBytes(MapUtils.getLongValue(item, "upstreamBytes"));
    recordDO.setUpstreamPackets(MapUtils.getLongValue(item, "upstreamPackets"));
    recordDO.setActiveEstablishedSessions(MapUtils.getLongValue(item, "activeEstablishedSessions"));
    recordDO
        .setPassiveEstablishedSessions(MapUtils.getLongValue(item, "passiveEstablishedSessions"));

    return recordDO;
  }

  private String batchUpdatesql(String tableName) {
    StringBuilder sql = new StringBuilder();

    sql.append("insert into ");
    sql.append(tableName);
    sql.append("(");
    sql.append("ip_a_address,");
    sql.append("ip_b_address,");
    sql.append("network_id,");
    sql.append("service_id,");

    sql.append("downstream_bytes,");
    sql.append("downstream_packets,");
    sql.append("upstream_bytes,");
    sql.append("upstream_packets,");
    sql.append("total_bytes,");
    sql.append("total_packets,");
    sql.append("established_sessions,");
    sql.append("active_established_sessions,");
    sql.append("passive_established_sessions,");
    sql.append("tcp_client_network_latency,");
    sql.append("tcp_client_network_latency_counts,");
    sql.append("tcp_server_network_latency,");
    sql.append("tcp_server_network_latency_counts,");
    sql.append("server_response_latency,");
    sql.append("server_response_latency_counts,");
    sql.append("tcp_client_retransmission_packets,");
    sql.append("tcp_client_packets,");
    sql.append("tcp_server_retransmission_packets,");
    sql.append("tcp_server_packets,");
    sql.append("tcp_client_zero_window_packets,");
    sql.append("tcp_server_zero_window_packets,");
    sql.append("tcp_established_fail_counts,");
    sql.append("tcp_established_success_counts,");
    sql.append("timestamp)");
    sql.append(" values (");
    sql.append(":ipAAddress,");
    sql.append(":ipBAddress,");
    sql.append(":networkId,");
    sql.append(":serviceId,");

    sql.append(":downstreamBytes,");
    sql.append(":downstreamPackets,");
    sql.append(":upstreamBytes,");
    sql.append(":upstreamPackets,");
    sql.append(":totalBytes,");
    sql.append(":totalPackets,");
    sql.append(":establishedSessions,");
    sql.append(":activeEstablishedSessions,");
    sql.append(":passiveEstablishedSessions,");
    sql.append(":tcpClientNetworkLatency,");
    sql.append(":tcpClientNetworkLatencyCounts,");
    sql.append(":tcpServerNetworkLatency,");
    sql.append(":tcpServerNetworkLatencyCounts,");
    sql.append(":serverResponseLatency,");
    sql.append(":serverResponseLatencyCounts,");
    sql.append(":tcpClientRetransmissionPackets,");
    sql.append(":tcpClientPackets,");
    sql.append(":tcpServerRetransmissionPackets,");
    sql.append(":tcpServerPackets,");
    sql.append(":tcpClientZeroWindowPackets,");
    sql.append(":tcpServerZeroWindowPackets,");
    sql.append(":tcpEstablishedFailCounts,");
    sql.append(":tcpEstablishedSuccessCounts,");
    sql.append(":timestamp)");

    return sql.toString();
  }

}
