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
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.common.helper.AggsFunctionEnum;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metric.dao.MetricServiceDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricServiceDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 *
 * create at 2021年8月25日, fpc-manager
 */
@Repository
public class MetricServiceDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
    implements MetricServiceDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricServiceDataRecordDaoImpl.class);

  private static final Map<String, String> TERM_KEY;
  static {
    TERM_KEY = new HashMap<String, String>();
    TERM_KEY.put("network_id", "networkId");
    TERM_KEY.put("service_id", "serviceId");
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
    return ManagerConstants.TABLE_METRIC_SERVICE_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricServiceDataRecordDao#queryMetricServices(com.machloop.fpc.manager.metric.vo.MetricQueryVO, com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.util.List, java.util.List)
   */
  @Override
  public Page<MetricServiceDataRecordDO> queryMetricServices(MetricQueryVO queryVO, Pageable page,
      String sortProperty, String sortDirection, List<String> metrics,
      List<Tuple2<String, String>> serviceNetworks) {

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields, metrics);
    setSpecialKpiAggs(aggsFields, metrics);

    // 附加过滤条件
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(serviceNetworks)) {
      serviceNetworks.forEach(item -> {
        Map<String, Object> temp = Maps.newHashMap();
        temp.put("service_id", item.getT1());
        temp.put("network_id", item.getT2());
        combinationConditions.add(temp);
      });
    }

    List<MetricServiceDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Integer totalSize = 0;
    try {
      StringBuilder sql = new StringBuilder();

      // 分组字段
      List<String> terms = TERM_KEY.entrySet().stream()
          .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
      sql.append("select ").append(StringUtils.join(terms, ","));

      // 聚合字段
      aggsFields.entrySet().forEach(entry -> {
        sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
      });
      sql.append(" from ").append(getTableName());

      // 过滤
      Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      StringBuilder wheresql = new StringBuilder();
      enrichWhereSql(queryVO, wheresql, params);
      fillAdditionalConditions(combinationConditions, wheresql, params);
      sql.append(wheresql);

      sql.append(" group by ").append(StringUtils.join(TERM_KEY.values(), ","));
      sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
          .append(sortDirection);

      List<Map<String, Object>> tempResult = getClickHouseJdbcTemplate().getJdbcTemplate()
          .query(sql.toString(), params, new ColumnMapRowMapper());
      totalSize = tempResult.size();

      if (page != null) {
        int currentPageStart = page.getOffset();
        int currentPageEnd = page.getOffset() + page.getPageSize();
        if (currentPageStart < totalSize) {
          tempResult = tempResult.subList(currentPageStart,
              currentPageEnd > totalSize ? totalSize : currentPageEnd);
        } else {
          tempResult = Lists.newArrayListWithCapacity(0);
        }
      }

      tempResult = metricDataConversion(tempResult);
      tempResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (Exception e) {
      LOGGER.warn("failed to query service metric.", e);
    }

    return new PageImpl<>(result, page, totalSize);
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricServiceDataRecordDao#queryMetricServiceHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, boolean, java.util.List)
   */
  @Override
  public List<MetricServiceDataRecordDO> queryMetricServiceHistograms(MetricQueryVO queryVO,
      boolean extendedBound, List<String> metrics) {
    List<MetricServiceDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields, metrics);
    setSpecialKpiAggs(aggsFields, metrics);

    // 附件过滤条件
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(queryVO.getDsl()) && StringUtils.isNotBlank(queryVO.getNetworkId())
        && StringUtils.isNotBlank(queryVO.getServiceId())) {
      Map<String, Object> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      termKey.put("network_id", queryVO.getNetworkId());
      termKey.put("service_id", queryVO.getServiceId());
      combinationConditions.add(termKey);
    }

    try {
      List<Map<String, Object>> tempResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, TERM_KEY, aggsFields);
      tempResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query service histogram", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricServiceDataRecordDao#queryMetricServiceHistogramsWithAggsFields(com.machloop.fpc.manager.metric.vo.MetricQueryVO, boolean, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricServiceHistogramsWithAggsFields(MetricQueryVO queryVO,
      boolean extendedBound, List<String> aggsFields) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(allAggsFields, Lists.newArrayList("ALL"));
    setSpecialKpiAggs(allAggsFields, Lists.newArrayList("ALL"));
    Map<String, Tuple2<AggsFunctionEnum, String>> selectedAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggsFields.forEach(aggsField -> {
      getCombinationAggsFields(aggsField).forEach(item -> {
        if (allAggsFields.containsKey(item)) {
          selectedAggsFields.put(item, allAggsFields.get(item));
        }
      });
    });
    if (MapUtils.isEmpty(selectedAggsFields)) {
      return result;
    }

    // 附加过滤条件
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(queryVO.getDsl()) && StringUtils.isNotBlank(queryVO.getNetworkId())
        && StringUtils.isNotBlank(queryVO.getServiceId())) {
      Map<String, Object> termKey = Maps.newHashMap();
      termKey.put("network_id", queryVO.getNetworkId());
      termKey.put("service_id", queryVO.getServiceId());
      combinationConditions.add(termKey);
    }

    try {
      List<Map<String, Object>> tempResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, TERM_KEY, selectedAggsFields);
      result.addAll(tempResult);
    } catch (IOException e) {
      LOGGER.warn("failed to query service histogram", e);
    }

    return result;
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationServices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
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
    queryVO.setCount(0);

    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields, Lists.newArrayList("ALL"));

    int totalSize = 0;
    int offset = 0;
    int currentSize = 0;
    List<Map<String, Object>> batchList = null;
    do {
      batchList = termMetricAggregate(inputTableName, queryVO, null, TERM_KEY, aggsFields,
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
      item.put("tcpEstablishedTimeAvg", MapUtils.getLongValue(item, "tcpEstablishedTimeAvg"));
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(batchList);
    return Arrays.stream(
        jdbcTemplate.getJdbcTemplate().batchUpdate(batchUpdatesql(outputTableName), batchSource))
        .sum();
  }

  private void setAggsFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields,
      List<String> metrics) {
    if (metrics.contains(ManagerConstants.METRIC_NPM_FLOW) || metrics.contains("ALL")) {
      aggsFields.put("bytepsPeak", Tuples.of(AggsFunctionEnum.MAX, "byteps_peak"));
      aggsFields.put("packetpsPeak", Tuples.of(AggsFunctionEnum.MAX, "packetps_peak"));
      aggsFields.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
      aggsFields.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
      aggsFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
      aggsFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
      aggsFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
      aggsFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
      aggsFields.put("filterDiscardBytes", Tuples.of(AggsFunctionEnum.SUM, "filter_discard_bytes"));
      aggsFields.put("filterDiscardPackets",
          Tuples.of(AggsFunctionEnum.SUM, "filter_discard_packets"));
      aggsFields.put("overloadDiscardBytes",
          Tuples.of(AggsFunctionEnum.SUM, "overload_discard_bytes"));
      aggsFields.put("overloadDiscardPackets",
          Tuples.of(AggsFunctionEnum.SUM, "overload_discard_packets"));
      aggsFields.put("deduplicationBytes", Tuples.of(AggsFunctionEnum.SUM, "deduplication_bytes"));
      aggsFields.put("deduplicationPackets",
          Tuples.of(AggsFunctionEnum.SUM, "deduplication_packets"));
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_FRAGMENT) || metrics.contains("ALL")) {
      aggsFields.put("fragmentTotalBytes", Tuples.of(AggsFunctionEnum.SUM, "fragment_total_bytes"));
      aggsFields.put("fragmentTotalPackets",
          Tuples.of(AggsFunctionEnum.SUM, "fragment_total_packets"));
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_TCP) || metrics.contains("ALL")) {
      aggsFields.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
      aggsFields.put("tcpClientSynPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_syn_packets"));
      aggsFields.put("tcpServerSynPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_syn_packets"));
      aggsFields.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
      aggsFields.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));
      aggsFields.put("tcpEstablishedFailCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_fail_counts"));
      aggsFields.put("tcpEstablishedSuccessCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_success_counts"));
      aggsFields.put("tcpEstablishedTimeAvg",
          Tuples.of(AggsFunctionEnum.AVG, "tcp_established_time_avg"));
      aggsFields.put("tcpZeroWindowPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_zero_window_packets"));

      aggsFields.put("tcpEstablishedSuccessCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_success_counts_inside_service"));
      aggsFields.put("tcpEstablishedFailCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_fail_counts_inside_service"));
      aggsFields.put("tcpClientSynPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_syn_packets_inside_service"));
      aggsFields.put("tcpServerSynPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_syn_packets_inside_service"));
      aggsFields.put("tcpClientZeroWindowPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_zero_window_packets_inside_service"));
      aggsFields.put("tcpServerZeroWindowPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_zero_window_packets_inside_service"));


      aggsFields.put("tcpEstablishedSuccessCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_success_counts_outside_service"));
      aggsFields.put("tcpEstablishedFailCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_fail_counts_outside_service"));
      aggsFields.put("tcpClientSynPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_syn_packets_outside_service"));
      aggsFields.put("tcpServerSynPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_syn_packets_outside_service"));
      aggsFields.put("tcpClientZeroWindowPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_zero_window_packets_outside_service"));
      aggsFields.put("tcpServerZeroWindowPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_zero_window_packets_outside_service"));

    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_SESSION) || metrics.contains("ALL")) {
      aggsFields.put("activeSessions", Tuples.of(AggsFunctionEnum.MAX, "active_sessions"));
      aggsFields.put("concurrentSessions", Tuples.of(AggsFunctionEnum.MAX, "concurrent_sessions"));
      aggsFields.put("concurrentTcpSessions",
          Tuples.of(AggsFunctionEnum.MAX, "concurrent_tcp_sessions"));
      aggsFields.put("concurrentUdpSessions",
          Tuples.of(AggsFunctionEnum.MAX, "concurrent_udp_sessions"));
      aggsFields.put("concurrentArpSessions",
          Tuples.of(AggsFunctionEnum.MAX, "concurrent_arp_sessions"));
      aggsFields.put("concurrentIcmpSessions",
          Tuples.of(AggsFunctionEnum.MAX, "concurrent_icmp_sessions"));
      aggsFields.put("establishedSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));
      aggsFields.put("destroyedSessions", Tuples.of(AggsFunctionEnum.SUM, "destroyed_sessions"));
      aggsFields.put("establishedTcpSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_tcp_sessions"));
      aggsFields.put("establishedUdpSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_udp_sessions"));
      aggsFields.put("establishedIcmpSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_icmp_sessions"));
      aggsFields.put("establishedOtherSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_other_sessions"));
      aggsFields.put("establishedUpstreamSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_upstream_sessions"));
      aggsFields.put("establishedDownstreamSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_downstream_sessions"));
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_PERFORMANCE) || metrics.contains("ALL")) {
      aggsFields.put("tcpClientNetworkLatency",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency"));
      aggsFields.put("tcpClientNetworkLatencyCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_counts"));
      aggsFields.put("tcpServerNetworkLatency",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency"));
      aggsFields.put("tcpServerNetworkLatencyCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_counts"));
      aggsFields.put("serverResponseLatency",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency"));
      aggsFields.put("serverResponseLatencyCounts",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency_counts"));
      aggsFields.put("serverResponseLatencyPeak",
          Tuples.of(AggsFunctionEnum.MAX, "server_response_latency_peak"));
      aggsFields.put("tcpClientRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
      aggsFields.put("tcpClientPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets"));
      aggsFields.put("tcpServerRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));
      aggsFields.put("tcpServerPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets"));
      aggsFields.put("tcpClientZeroWindowPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_zero_window_packets"));
      aggsFields.put("tcpServerZeroWindowPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_zero_window_packets"));
      aggsFields.put("serverResponseFastCounts",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_fast_counts"));
      aggsFields.put("serverResponseNormalCounts",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_normal_counts"));
      aggsFields.put("serverResponseTimeoutCounts",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_timeout_counts"));

      aggsFields.put("serverResponseLatencyInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency_inside_service"));
      aggsFields.put("serverResponseLatencyCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency_counts_inside_service"));
      aggsFields.put("serverResponseLatencyPeakInsideService",
          Tuples.of(AggsFunctionEnum.MAX, "server_response_latency_peak_inside_service"));
      aggsFields.put("serverResponseFastCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_fast_counts_inside_service"));
      aggsFields.put("serverResponseNormalCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_normal_counts_inside_service"));
      aggsFields.put("serverResponseTimeoutCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_timeout_counts_inside_service"));
      aggsFields.put("tcpClientNetworkLatencyInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_inside_service"));
      aggsFields.put("tcpClientNetworkLatencyCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_counts_inside_service"));
      aggsFields.put("tcpServerNetworkLatencyInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_inside_service"));
      aggsFields.put("tcpServerNetworkLatencyCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_counts_inside_service"));
      aggsFields.put("tcpClientRetransmissionPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets_inside_service"));
      aggsFields.put("tcpServerRetransmissionPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets_inside_service"));
      aggsFields.put("tcpClientPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets_inside_service"));
      aggsFields.put("tcpServerPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets_inside_service"));


      aggsFields.put("serverResponseLatencyOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency_outside_service"));
      aggsFields.put("serverResponseLatencyCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency_counts_outside_service"));
      aggsFields.put("serverResponseLatencyPeakOutsideService",
          Tuples.of(AggsFunctionEnum.MAX, "server_response_latency_peak_outside_service"));
      aggsFields.put("serverResponseFastCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_fast_counts_outside_service"));
      aggsFields.put("serverResponseNormalCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_normal_counts_outside_service"));
      aggsFields.put("serverResponseTimeoutCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_timeout_counts_outside_service"));
      aggsFields.put("tcpClientNetworkLatencyOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_outside_service"));
      aggsFields.put("tcpClientNetworkLatencyCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_counts_outside_service"));
      aggsFields.put("tcpServerNetworkLatencyOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_outside_service"));
      aggsFields.put("tcpServerNetworkLatencyCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_counts_outside_service"));
      aggsFields.put("tcpClientRetransmissionPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets_outside_service"));
      aggsFields.put("tcpServerRetransmissionPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets_outside_service"));
      aggsFields.put("tcpClientPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets_outside_service"));
      aggsFields.put("tcpServerPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets_outside_service"));
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_UNIQUE_IP_COUNTS) || metrics.contains("ALL")) {
      aggsFields.put("uniqueIpCounts", Tuples.of(AggsFunctionEnum.MAX, "unique_ip_counts"));
    }

  }

  private void setSpecialKpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields,
      List<String> metrics) {
    if (metrics.contains(ManagerConstants.METRIC_NPM_PERFORMANCE) || metrics.contains("ALL")) {
      specialKpiAggs(aggsFields);

      // TCP客户端时延均值-内网服务
      aggsFields.put("tcpClientNetworkLatencyAvgInsideService", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpClientNetworkLatencyInsideService, tcpClientNetworkLatencyCountsInsideService"));
      // TCP服务端时延均值-内网服务
      aggsFields.put("tcpServerNetworkLatencyAvgInsideService", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpServerNetworkLatencyInsideService, tcpServerNetworkLatencyCountsInsideService"));
      // 服务端响应时延均值-内网服务
      aggsFields.put("serverResponseLatencyAvgInsideService", Tuples.of(AggsFunctionEnum.DIVIDE,
          "serverResponseLatencyInsideService, serverResponseLatencyCountsInsideService"));
      // TCP客户端时延均值-外网服务
      aggsFields.put("tcpClientNetworkLatencyAvgOutsideService", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpClientNetworkLatencyOutsideService, tcpClientNetworkLatencyCountsOutsideService"));
      // TCP服务端时延均值-外网服务
      aggsFields.put("tcpServerNetworkLatencyAvgOutsideService", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpServerNetworkLatencyOutsideService, tcpServerNetworkLatencyCountsOutsideService"));
      // 服务端响应时延均值-外网服务
      aggsFields.put("serverResponseLatencyAvgOutsideService", Tuples.of(AggsFunctionEnum.DIVIDE,
          "serverResponseLatencyOutsideService, serverResponseLatencyCountsOutsideService"));

      // TCP客户端重传率-内网服务
      aggsFields.put("tcpClientRetransmissionRateInsideService", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpClientRetransmissionPacketsInsideService, tcpClientPacketsInsideService"));
      // TCP服务端重传率-内网服务
      aggsFields.put("tcpServerRetransmissionRateInsideService", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpServerRetransmissionPacketsInsideService, tcpServerPacketsInsideService"));
      // TCP客户端重传率-外网服务
      aggsFields.put("tcpClientRetransmissionRateOutsideService", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpClientRetransmissionPacketsOutsideService, tcpClientPacketsOutsideService"));
      // TCP服务端重传率-外网服务
      aggsFields.put("tcpServerRetransmissionRateOutsideService", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpServerRetransmissionPacketsOutsideService, tcpServerPacketsOutsideService"));
    }
  }

  private MetricServiceDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricServiceDataRecordDO recordDO = new MetricServiceDataRecordDO();
    tranKPIMapToDateRecord(item, recordDO);

    recordDO.setBytepsPeak(MapUtils.getLongValue(item, "bytepsPeak"));
    recordDO.setPacketpsPeak(MapUtils.getLongValue(item, "packetpsPeak"));
    recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "downstreamBytes"));
    recordDO.setDownstreamPackets(MapUtils.getLongValue(item, "downstreamPackets"));
    recordDO.setUpstreamBytes(MapUtils.getLongValue(item, "upstreamBytes"));
    recordDO.setUpstreamPackets(MapUtils.getLongValue(item, "upstreamPackets"));
    recordDO.setFilterDiscardBytes(MapUtils.getLongValue(item, "filterDiscardBytes"));
    recordDO.setFilterDiscardPackets(MapUtils.getLongValue(item, "filterDiscardPackets"));
    recordDO.setOverloadDiscardBytes(MapUtils.getLongValue(item, "overloadDiscardBytes"));
    recordDO.setOverloadDiscardPackets(MapUtils.getLongValue(item, "overloadDiscardPackets"));
    recordDO.setDeduplicationBytes(MapUtils.getLongValue(item, "deduplicationBytes"));
    recordDO.setDeduplicationPackets(MapUtils.getLongValue(item, "deduplicationPackets"));


    recordDO.setFragmentTotalBytes(MapUtils.getLongValue(item, "fragmentTotalBytes"));
    recordDO.setFragmentTotalPackets(MapUtils.getLongValue(item, "fragmentTotalPackets"));

    recordDO.setTcpSynPackets(MapUtils.getLongValue(item, "tcpSynPackets"));
    recordDO.setTcpServerSynPackets(MapUtils.getLongValue(item, "tcpServerSynPackets"));
    recordDO.setTcpClientSynPackets(MapUtils.getLongValue(item, "tcpClientSynPackets"));
    recordDO.setTcpSynAckPackets(MapUtils.getLongValue(item, "tcpSynAckPackets"));
    recordDO.setTcpSynRstPackets(MapUtils.getLongValue(item, "tcpSynRstPackets"));
    recordDO.setTcpEstablishedTimeAvg(MapUtils.getLongValue(item, "tcpEstablishedTimeAvg"));
    recordDO.setTcpZeroWindowPackets(MapUtils.getLongValue(item, "tcpZeroWindowPackets"));

    recordDO.setActiveSessions(MapUtils.getLongValue(item, "activeSessions"));
    recordDO.setConcurrentSessions(MapUtils.getLongValue(item, "concurrentSessions"));
    recordDO.setConcurrentTcpSessions(MapUtils.getLongValue(item, "concurrentTcpSessions"));
    recordDO.setConcurrentUdpSessions(MapUtils.getLongValue(item, "concurrentUdpSessions"));
    recordDO.setConcurrentArpSessions(MapUtils.getLongValue(item, "concurrentArpSessions"));
    recordDO.setConcurrentIcmpSessions(MapUtils.getLongValue(item, "concurrentIcmpSessions"));
    recordDO.setDestroyedSessions(MapUtils.getLongValue(item, "destroyedSessions"));
    recordDO.setEstablishedTcpSessions(MapUtils.getLongValue(item, "establishedTcpSessions"));
    recordDO.setEstablishedUdpSessions(MapUtils.getLongValue(item, "establishedUdpSessions"));
    recordDO.setEstablishedIcmpSessions(MapUtils.getLongValue(item, "establishedIcmpSessions"));
    recordDO.setEstablishedOtherSessions(MapUtils.getLongValue(item, "establishedOtherSessions"));
    recordDO
        .setEstablishedUpstreamSessions(MapUtils.getLongValue(item, "establishedUpstreamSessions"));
    recordDO.setEstablishedDownstreamSessions(
        MapUtils.getLongValue(item, "establishedDownstreamSessions"));

    recordDO.setServerResponseFastCounts(MapUtils.getLongValue(item, "serverResponseFastCounts"));
    recordDO
        .setServerResponseNormalCounts(MapUtils.getLongValue(item, "serverResponseNormalCounts"));
    recordDO
        .setServerResponseTimeoutCounts(MapUtils.getLongValue(item, "serverResponseTimeoutCounts"));
    recordDO.setServerResponseLatencyPeak(MapUtils.getLongValue(item, "serverResponseLatencyPeak"));

    recordDO.setUniqueIpCounts(MapUtils.getLongValue(item, "uniqueIpCounts"));


    recordDO.setServerResponseLatencyInsideService(
        MapUtils.getLongValue(item, "serverResponseLatencyInsideService"));
    recordDO.setServerResponseLatencyCountsInsideService(
        MapUtils.getLongValue(item, "serverResponseLatencyCountsInsideService"));
    recordDO.setServerResponseLatencyPeakInsideService(
        MapUtils.getLongValue(item, "serverResponseLatencyPeakInsideService"));
    recordDO.setServerResponseFastCountsInsideService(
        MapUtils.getLongValue(item, "serverResponseFastCountsInsideService"));
    recordDO.setServerResponseNormalCountsInsideService(
        MapUtils.getLongValue(item, "serverResponseNormalCountsInsideService"));
    recordDO.setServerResponseTimeoutCountsInsideService(
        MapUtils.getLongValue(item, "serverResponseTimeoutCountsInsideService"));
    recordDO.setTcpClientNetworkLatencyInsideService(
        MapUtils.getLongValue(item, "tcpClientNetworkLatencyInsideService"));
    recordDO.setTcpClientNetworkLatencyCountsInsideService(
        MapUtils.getLongValue(item, "tcpClientNetworkLatencyCountsInsideService"));
    recordDO.setTcpServerNetworkLatencyInsideService(
        MapUtils.getLongValue(item, "tcpServerNetworkLatencyInsideService"));
    recordDO.setTcpServerNetworkLatencyCountsInsideService(
        MapUtils.getLongValue(item, "tcpServerNetworkLatencyCountsInsideService"));
    recordDO.setTcpClientRetransmissionPacketsInsideService(
        MapUtils.getLongValue(item, "tcpClientRetransmissionPacketsInsideService"));
    recordDO.setTcpServerRetransmissionPacketsInsideService(
        MapUtils.getLongValue(item, "tcpServerRetransmissionPacketsInsideService"));


    recordDO.setServerResponseLatencyOutsideService(
        MapUtils.getLongValue(item, "serverResponseLatencyOutsideService"));
    recordDO.setServerResponseLatencyCountsOutsideService(
        MapUtils.getLongValue(item, "serverResponseLatencyCountsOutsideService"));
    recordDO.setServerResponseLatencyPeakOutsideService(
        MapUtils.getLongValue(item, "serverResponseLatencyPeakOutsideService"));
    recordDO.setServerResponseFastCountsOutsideService(
        MapUtils.getLongValue(item, "serverResponseFastCountsOutsideService"));
    recordDO.setServerResponseNormalCountsOutsideService(
        MapUtils.getLongValue(item, "serverResponseNormalCountsOutsideService"));
    recordDO.setServerResponseTimeoutCountsOutsideService(
        MapUtils.getLongValue(item, "serverResponseTimeoutCountsOutsideService"));
    recordDO.setTcpClientNetworkLatencyOutsideService(
        MapUtils.getLongValue(item, "tcpClientNetworkLatencyOutsideService"));
    recordDO.setTcpClientNetworkLatencyCountsOutsideService(
        MapUtils.getLongValue(item, "tcpClientNetworkLatencyCountsOutsideService"));
    recordDO.setTcpServerNetworkLatencyOutsideService(
        MapUtils.getLongValue(item, "tcpServerNetworkLatencyOutsideService"));
    recordDO.setTcpServerNetworkLatencyCountsOutsideService(
        MapUtils.getLongValue(item, "tcpServerNetworkLatencyCountsOutsideService"));
    recordDO.setTcpClientRetransmissionPacketsOutsideService(
        MapUtils.getLongValue(item, "tcpClientRetransmissionPacketsOutsideService"));
    recordDO.setTcpServerRetransmissionPacketsOutsideService(
        MapUtils.getLongValue(item, "tcpServerRetransmissionPacketsOutsideService"));


    recordDO.setTcpClientRetransmissionRateInsideService(
        MapUtils.getLongValue(item, "tcpClientRetransmissionRateInsideService"));
    recordDO.setTcpServerRetransmissionRateInsideService(
        MapUtils.getLongValue(item, "tcpServerRetransmissionRateInsideService"));
    recordDO.setTcpClientNetworkLatencyAvgInsideService(
        MapUtils.getLongValue(item, "tcpClientNetworkLatencyAvgInsideService"));
    recordDO.setTcpServerNetworkLatencyAvgInsideService(
        MapUtils.getLongValue(item, "tcpServerNetworkLatencyAvgInsideService"));
    recordDO.setServerResponseLatencyAvgInsideService(
        MapUtils.getLongValue(item, "serverResponseLatencyAvgInsideService"));
    recordDO.setTcpClientRetransmissionRateOutsideService(
        MapUtils.getLongValue(item, "tcpClientRetransmissionRateOutsideService"));
    recordDO.setTcpServerRetransmissionRateOutsideService(
        MapUtils.getLongValue(item, "tcpServerRetransmissionRateOutsideService"));
    recordDO.setTcpClientNetworkLatencyAvgOutsideService(
        MapUtils.getLongValue(item, "tcpClientNetworkLatencyAvgOutsideService"));
    recordDO.setTcpServerNetworkLatencyAvgOutsideService(
        MapUtils.getLongValue(item, "tcpServerNetworkLatencyAvgOutsideService"));
    recordDO.setServerResponseLatencyAvgOutsideService(
        MapUtils.getLongValue(item, "serverResponseLatencyAvgOutsideService"));

    return recordDO;
  }


  private String batchUpdatesql(String tableName) {
    StringBuilder sql = new StringBuilder();

    sql.append("insert into ");
    sql.append(tableName);
    sql.append("(");
    sql.append("network_id,");
    sql.append("service_id,");

    sql.append("byteps_peak,");
    sql.append("packetps_peak,");
    sql.append("total_bytes,");
    sql.append("total_packets,");
    sql.append("downstream_bytes,");
    sql.append("downstream_packets,");
    sql.append("upstream_bytes,");
    sql.append("upstream_packets,");
    sql.append("filter_discard_bytes,");
    sql.append("filter_discard_packets,");
    sql.append("overload_discard_bytes,");
    sql.append("overload_discard_packets,");
    sql.append("deduplication_bytes,");
    sql.append("deduplication_packets,");

    sql.append("fragment_total_bytes,");
    sql.append("fragment_total_packets,");

    sql.append("tcp_syn_packets,");
    sql.append("tcp_client_syn_packets,");
    sql.append("tcp_server_syn_packets,");
    sql.append("tcp_syn_ack_packets,");
    sql.append("tcp_syn_rst_packets,");
    sql.append("tcp_established_fail_counts,");
    sql.append("tcp_established_success_counts,");
    sql.append("tcp_established_time_avg,");
    sql.append("tcp_zero_window_packets,");

    sql.append("active_sessions,");
    sql.append("concurrent_sessions,");
    sql.append("concurrent_tcp_sessions,");
    sql.append("concurrent_udp_sessions,");
    sql.append("concurrent_arp_sessions,");
    sql.append("concurrent_icmp_sessions,");
    sql.append("established_sessions,");
    sql.append("destroyed_sessions,");
    sql.append("established_tcp_sessions,");
    sql.append("established_udp_sessions,");
    sql.append("established_icmp_sessions,");
    sql.append("established_other_sessions,");
    sql.append("established_upstream_sessions,");
    sql.append("established_downstream_sessions,");


    sql.append("tcp_client_network_latency,");
    sql.append("tcp_client_network_latency_counts,");
    sql.append("tcp_server_network_latency,");
    sql.append("tcp_server_network_latency_counts,");
    sql.append("server_response_latency,");
    sql.append("server_response_latency_counts,");
    sql.append("server_response_latency_peak,");
    sql.append("tcp_client_retransmission_packets,");
    sql.append("tcp_client_packets,");
    sql.append("tcp_server_retransmission_packets,");
    sql.append("tcp_server_packets,");
    sql.append("tcp_client_zero_window_packets,");
    sql.append("tcp_server_zero_window_packets,");
    sql.append("server_response_fast_counts,");
    sql.append("server_response_normal_counts,");
    sql.append("server_response_timeout_counts,");

    sql.append("unique_ip_counts,");
    // 性能和TCP区分内外网服务的新增字段
    sql.append("server_response_latency_inside_service,");
    sql.append("server_response_latency_counts_inside_service,");
    sql.append("server_response_latency_peak_inside_service,");
    sql.append("server_response_fast_counts_inside_service,");
    sql.append("server_response_normal_counts_inside_service,");
    sql.append("server_response_timeout_counts_inside_service,");
    sql.append("tcp_client_network_latency_inside_service,");
    sql.append("tcp_client_network_latency_counts_inside_service,");
    sql.append("tcp_server_network_latency_inside_service,");
    sql.append("tcp_server_network_latency_counts_inside_service,");
    sql.append("tcp_established_success_counts_inside_service,");
    sql.append("tcp_established_fail_counts_inside_service,");
    sql.append("tcp_client_syn_packets_inside_service,");
    sql.append("tcp_server_syn_packets_inside_service,");
    sql.append("tcp_client_retransmission_packets_inside_service,");
    sql.append("tcp_client_packets_inside_service,");
    sql.append("tcp_server_retransmission_packets_inside_service,");
    sql.append("tcp_server_packets_inside_service,");
    sql.append("tcp_client_zero_window_packets_inside_service,");
    sql.append("tcp_server_zero_window_packets_inside_service,");
    sql.append("server_response_latency_outside_service,");
    sql.append("server_response_latency_counts_outside_service,");
    sql.append("server_response_latency_peak_outside_service,");
    sql.append("server_response_fast_counts_outside_service,");
    sql.append("server_response_normal_counts_outside_service,");
    sql.append("server_response_timeout_counts_outside_service,");
    sql.append("tcp_client_network_latency_outside_service,");
    sql.append("tcp_client_network_latency_counts_outside_service,");
    sql.append("tcp_server_network_latency_outside_service,");
    sql.append("tcp_server_network_latency_counts_outside_service,");
    sql.append("tcp_established_success_counts_outside_service,");
    sql.append("tcp_established_fail_counts_outside_service,");
    sql.append("tcp_client_syn_packets_outside_service,");
    sql.append("tcp_server_syn_packets_outside_service,");
    sql.append("tcp_client_retransmission_packets_outside_service,");
    sql.append("tcp_client_packets_outside_service,");
    sql.append("tcp_server_retransmission_packets_outside_service,");
    sql.append("tcp_server_packets_outside_service,");
    sql.append("tcp_client_zero_window_packets_outside_service,");
    sql.append("tcp_server_zero_window_packets_outside_service,");
    sql.append("timestamp)");
    // values
    sql.append(" values (");
    sql.append(":networkId,");
    sql.append(":serviceId,");

    sql.append(":bytepsPeak,");
    sql.append(":packetpsPeak,");
    sql.append(":totalBytes,");
    sql.append(":totalPackets,");
    sql.append(":downstreamBytes,");
    sql.append(":downstreamPackets,");
    sql.append(":upstreamBytes,");
    sql.append(":upstreamPackets,");
    sql.append(":filterDiscardBytes,");
    sql.append(":filterDiscardPackets,");
    sql.append(":overloadDiscardBytes,");
    sql.append(":overloadDiscardPackets,");
    sql.append(":deduplicationBytes,");
    sql.append(":deduplicationPackets,");

    sql.append(":fragmentTotalBytes,");
    sql.append(":fragmentTotalPackets,");

    sql.append(":tcpSynPackets,");
    sql.append(":tcpClientSynPackets,");
    sql.append(":tcpServerSynPackets,");
    sql.append(":tcpSynAckPackets,");
    sql.append(":tcpSynRstPackets,");
    sql.append(":tcpEstablishedFailCounts,");
    sql.append(":tcpEstablishedSuccessCounts,");
    sql.append(":tcpEstablishedTimeAvg,");
    sql.append(":tcpZeroWindowPackets,");

    sql.append(":activeSessions,");
    sql.append(":concurrentSessions,");
    sql.append(":concurrentTcpSessions,");
    sql.append(":concurrentUdpSessions,");
    sql.append(":concurrentArpSessions,");
    sql.append(":concurrentIcmpSessions,");
    sql.append(":establishedSessions,");
    sql.append(":destroyedSessions,");
    sql.append(":establishedTcpSessions,");
    sql.append(":establishedUdpSessions,");
    sql.append(":establishedIcmpSessions,");
    sql.append(":establishedOtherSessions,");
    sql.append(":establishedUpstreamSessions,");
    sql.append(":establishedDownstreamSessions,");

    sql.append(":tcpClientNetworkLatency,");
    sql.append(":tcpClientNetworkLatencyCounts,");
    sql.append(":tcpServerNetworkLatency,");
    sql.append(":tcpServerNetworkLatencyCounts,");
    sql.append(":serverResponseLatency,");
    sql.append(":serverResponseLatencyCounts,");
    sql.append(":serverResponseLatencyPeak,");
    sql.append(":tcpClientRetransmissionPackets,");
    sql.append(":tcpClientPackets,");
    sql.append(":tcpServerRetransmissionPackets,");
    sql.append(":tcpServerPackets,");
    sql.append(":tcpClientZeroWindowPackets,");
    sql.append(":tcpServerZeroWindowPackets,");
    sql.append(":serverResponseFastCounts,");
    sql.append(":serverResponseNormalCounts,");
    sql.append(":serverResponseTimeoutCounts,");

    sql.append(":uniqueIpCounts,");
    // 性能和TCP区分内外网服务的新增字段
    sql.append(":serverResponseLatencyInsideService,");
    sql.append(":serverResponseLatencyCountsInsideService,");
    sql.append(":serverResponseLatencyPeakInsideService,");
    sql.append(":serverResponseFastCountsInsideService,");
    sql.append(":serverResponseNormalCountsInsideService,");
    sql.append(":serverResponseTimeoutCountsInsideService,");
    sql.append(":tcpClientNetworkLatencyInsideService,");
    sql.append(":tcpClientNetworkLatencyCountsInsideService,");
    sql.append(":tcpServerNetworkLatencyInsideService,");
    sql.append(":tcpServerNetworkLatencyCountsInsideService,");
    sql.append(":tcpEstablishedSuccessCountsInsideService,");
    sql.append(":tcpEstablishedFailCountsInsideService,");
    sql.append(":tcpClientSynPacketsInsideService,");
    sql.append(":tcpServerSynPacketsInsideService,");
    sql.append(":tcpClientRetransmissionPacketsInsideService,");
    sql.append(":tcpClientPacketsInsideService,");
    sql.append(":tcpServerRetransmissionPacketsInsideService,");
    sql.append(":tcpServerPacketsInsideService,");
    sql.append(":tcpClientZeroWindowPacketsInsideService,");
    sql.append(":tcpServerZeroWindowPacketsInsideService,");
    sql.append(":serverResponseLatencyOutsideService,");
    sql.append(":serverResponseLatencyCountsOutsideService,");
    sql.append(":serverResponseLatencyPeakOutsideService,");
    sql.append(":serverResponseFastCountsOutsideService,");
    sql.append(":serverResponseNormalCountsOutsideService,");
    sql.append(":serverResponseTimeoutCountsOutsideService,");
    sql.append(":tcpClientNetworkLatencyOutsideService,");
    sql.append(":tcpClientNetworkLatencyCountsOutsideService,");
    sql.append(":tcpServerNetworkLatencyOutsideService,");
    sql.append(":tcpServerNetworkLatencyCountsOutsideService,");
    sql.append(":tcpEstablishedSuccessCountsOutsideService,");
    sql.append(":tcpEstablishedFailCountsOutsideService,");
    sql.append(":tcpClientSynPacketsOutsideService,");
    sql.append(":tcpServerSynPacketsOutsideService,");
    sql.append(":tcpClientRetransmissionPacketsOutsideService,");
    sql.append(":tcpClientPacketsOutsideService,");
    sql.append(":tcpServerRetransmissionPacketsOutsideService,");
    sql.append(":tcpServerPacketsOutsideService,");
    sql.append(":tcpClientZeroWindowPacketsOutsideService,");
    sql.append(":tcpServerZeroWindowPacketsOutsideService,");
    sql.append(":timestamp)");

    return sql.toString();
  }

}
