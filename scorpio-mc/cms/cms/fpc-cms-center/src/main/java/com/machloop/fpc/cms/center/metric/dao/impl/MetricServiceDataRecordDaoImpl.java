package com.machloop.fpc.cms.center.metric.dao.impl;

import java.io.IOException;
import java.util.HashMap;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metric.dao.MetricServiceDataRecordDao;
import com.machloop.fpc.cms.center.metric.data.MetricServiceDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 *
 * create at 2021年8月25日, fpc-manager
 */
@Repository
public class MetricServiceDataRecordDaoImpl extends AbstractDataRecordDaoImpl
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
  private ClickHouseJdbcTemplate jdbcTemplate;

  /**
   * 
   * @see com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl#getSpl2SqlHelper()
   */
  @Override
  protected Spl2SqlHelper getSpl2SqlHelper() {
    return spl2SqlHelper;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl#getClickHouseJdbcTemplate()
   */
  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl#getTableName()
   */
  @Override
  protected String getTableName() {
    return CenterConstants.TABLE_METRIC_SERVICE_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricServiceDataRecordDao#queryMetricServices(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.util.List, java.lang.String)
   */
  @Override
  public List<MetricServiceDataRecordDO> queryMetricServices(MetricQueryVO queryVO,
      List<String> metrics, String metricField) {

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(metrics)) {
      setAggsFields(aggsFields, metrics);
      setSpecialKpiAggs(aggsFields, metrics);
    } else {
      aggregateFields(aggsFields, metricField);
      specialAggregateFields(aggsFields, metricField);
    }

    // 附加过滤条件
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(queryVO.getServiceNetworkIds())) {
      queryVO.getServiceNetworkIds().forEach(item -> {
        Map<String, Object> temp = Maps.newHashMap();
        temp.put("service_id", item.getT1());
        temp.put("network_id", item.getT2());
        combinationConditions.add(temp);
      });
    }

    List<MetricServiceDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
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

      List<Map<String, Object>> tempResult = queryWithExceptionHandle(sql.toString(), params,
          new ColumnMapRowMapper());
      tempResult = metricDataConversion(tempResult);
      tempResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (Exception e) {
      LOGGER.warn("failed to query service metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricServiceDataRecordDao#queryMetricService(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.util.List)
   */
  @Override
  public MetricServiceDataRecordDO queryMetricService(MetricQueryVO queryVO, List<String> metrics) {
    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields, metrics);
    setSpecialKpiAggs(aggsFields, metrics);

    MetricServiceDataRecordDO metricServiceDataRecordDO = new MetricServiceDataRecordDO();
    try {
      Map<String, Object> tempResult = metricAggregate(convertTableName(queryVO.getSourceType(),
          queryVO.getInterval(), queryVO.getPacketFileId()), queryVO, null, aggsFields);
      metricServiceDataRecordDO = tranResultMapToDateRecord(tempResult);
    } catch (IOException e) {
      LOGGER.warn("failed to query service histogram", e);
    }

    return metricServiceDataRecordDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricServiceDataRecordDao#queryMetricServiceHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, boolean, java.util.List)
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
    if (StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
      if (aggsFields.containsKey("activeSessions")) {
        aggsFields.put("activeSessions", Tuples.of(AggsFunctionEnum.SUM, "active_sessions"));
      }
      if (aggsFields.containsKey("concurrentSessions")) {
        aggsFields.put("concurrentSessions",
            Tuples.of(AggsFunctionEnum.SUM, "concurrent_sessions"));
      }
      if (aggsFields.containsKey("uniqueIpCounts")) {
        aggsFields.put("uniqueIpCounts", Tuples.of(AggsFunctionEnum.SUM, "unique_ip_counts"));
      }
    }

    try {
      List<Map<String, Object>> tempResult = dateHistogramMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, null, aggsFields);
      tempResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query service histogram", e);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricServiceDataRecordDao#queryMetricServiceHistogramsWithAggsFields(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, boolean, java.util.List)
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
    if (StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
      if (selectedAggsFields.containsKey("activeSessions")) {
        selectedAggsFields.put("activeSessions",
            Tuples.of(AggsFunctionEnum.SUM, "active_sessions"));
      }
      if (selectedAggsFields.containsKey("concurrentSessions")) {
        selectedAggsFields.put("concurrentSessions",
            Tuples.of(AggsFunctionEnum.SUM, "concurrent_sessions"));
      }
      if (selectedAggsFields.containsKey("uniqueIpCounts")) {
        selectedAggsFields.put("uniqueIpCounts",
            Tuples.of(AggsFunctionEnum.SUM, "unique_ip_counts"));
      }
    }

    if (MapUtils.isEmpty(selectedAggsFields)) {
      return result;
    }

    // 附加过滤条件(DSL为空时)
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
      List<Map<String, Object>> tempResult = dateHistogramMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, selectedAggsFields);
      tempResult.forEach(item -> result.add(item));
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
    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      whereSql.append(" and network_id in (:networkIds)");
      params.put("networkIds", queryVO.getNetworkIds());
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

  private void setAggsFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields,
      List<String> metrics) {
    if (metrics.contains(CenterConstants.METRIC_NPM_FLOW) || metrics.contains("ALL")) {
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

    if (metrics.contains(CenterConstants.METRIC_NPM_FRAGMENT) || metrics.contains("ALL")) {
      aggsFields.put("fragmentTotalBytes", Tuples.of(AggsFunctionEnum.SUM, "fragment_total_bytes"));
      aggsFields.put("fragmentTotalPackets",
          Tuples.of(AggsFunctionEnum.SUM, "fragment_total_packets"));
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_TCP) || metrics.contains("ALL")) {
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

    if (metrics.contains(CenterConstants.METRIC_NPM_SESSION) || metrics.contains("ALL")) {
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

    if (metrics.contains(CenterConstants.METRIC_NPM_PERFORMANCE) || metrics.contains("ALL")) {
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

    if (metrics.contains(CenterConstants.METRIC_NPM_UNIQUE_IP_COUNTS) || metrics.contains("ALL")) {
      aggsFields.put("uniqueIpCounts", Tuples.of(AggsFunctionEnum.MAX, "unique_ip_counts"));
    }

  }

  private void setSpecialKpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields,
      List<String> metrics) {
    if (metrics.contains(CenterConstants.METRIC_NPM_PERFORMANCE) || metrics.contains("ALL")) {
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

}
