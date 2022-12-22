package com.machloop.fpc.cms.center.metric.dao.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2022年2月17日, fpc-cms-center
 */
@Repository
public class MetricInSecondDaoImpl implements MetricInSecondDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricInSecondDaoImpl.class);

  private static final String TABLE_METRIC_NETWORK_IN_SECOND = "t_fpc_metric_network_in_second";
  private static final String TABLE_METRIC_SERVICE_IN_SECOND = "t_fpc_metric_service_in_second";
  private static final String TABLE_METRIC_L3DEVICE_IN_SECOND = "t_fpc_metric_l3device_in_second";
  private static final String TABLE_METRIC_IP_CONVERSATION_IN_SECOND = "t_fpc_metric_ip_conversation_in_second";
  private static final String TABLE_METRIC_DSCP_IN_SECOND = "t_fpc_metric_dscp_in_second";
  private static final String TABLE_METRIC_PAYLOAD_IN_SECOND = "t_fpc_metric_payload_in_second";
  private static final String TABLE_METRIC_PERFORMANCE_IN_SECOND = "t_fpc_metric_performance_in_second";
  private static final String TABLE_METRIC_TCP_IN_SECOND = "t_fpc_metric_tcp_in_second";

  private static final int SCALE_COUNTS = 4;

  private static final String DIVIDE_NULL_NAN = "NaN";
  private static final String DIVIDE_NULL_INF = "Infinity";

  private static final String REALTIME_STATISTICS_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXXX";

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#queryNetworkStatistics(java.util.Date, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryNetworkStatistics(Date startTime, List<String> networkIds) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(null, startTime, FpcCmsConstants.SOURCE_TYPE_NETWORK, networkIds, null, whereSql,
        params);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggs.put("alertCounts", Tuples.of(AggsFunctionEnum.SUM, "alert_counts"));
    aggs.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
    aggs.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
    aggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    aggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    // tcp客户端网络时延
    aggs.put("tcpClientNetworkLatency",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency"));
    aggs.put("tcpClientNetworkLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_counts"));
    aggs.put("tcpClientNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
        "tcpClientNetworkLatency, tcpClientNetworkLatencyCounts"));
    // tcp服务端网络时延
    aggs.put("tcpServerNetworkLatency",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency"));
    aggs.put("tcpServerNetworkLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_counts"));
    aggs.put("tcpServerNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
        "tcpServerNetworkLatency, tcpServerNetworkLatencyCounts"));
    // tcp重传率
    aggs.put("tcpPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_packets"));
    aggs.put("tcpRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_retransmission_packets"));
    aggs.put("tcpRetransmissionRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpRetransmissionPackets, tcpPackets"));
    // tcp客户端重传率
    aggs.put("tcpClientPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets"));
    aggs.put("tcpClientRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
    aggs.put("tcpClientRetransmissionRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpClientRetransmissionPackets, tcpClientPackets"));
    // tcp服务端重传率
    aggs.put("tcpServerPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets"));
    aggs.put("tcpServerRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));
    aggs.put("tcpServerRetransmissionRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpServerRetransmissionPackets, tcpServerPackets"));
    // 包长统计
    aggs.put("tinyPackets", Tuples.of(AggsFunctionEnum.SUM, "tiny_packets"));
    aggs.put("smallPackets", Tuples.of(AggsFunctionEnum.SUM, "small_packets"));
    aggs.put("mediumPackets", Tuples.of(AggsFunctionEnum.SUM, "medium_packets"));
    aggs.put("bigPackets", Tuples.of(AggsFunctionEnum.SUM, "big_packets"));
    aggs.put("largePackets", Tuples.of(AggsFunctionEnum.SUM, "large_packets"));
    aggs.put("hugePackets", Tuples.of(AggsFunctionEnum.SUM, "huge_packets"));
    aggs.put("jumboPackets", Tuples.of(AggsFunctionEnum.SUM, "jumbo_packets"));
    aggs.put("packetLengthAvg", Tuples.of(AggsFunctionEnum.AVG, "packet_length_avg"));
    // IP协议包统计
    aggs.put("tcpTotalPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_total_packets"));
    aggs.put("udpTotalPackets", Tuples.of(AggsFunctionEnum.SUM, "udp_total_packets"));
    aggs.put("icmpTotalPackets", Tuples.of(AggsFunctionEnum.SUM, "icmp_total_packets"));
    aggs.put("icmp6TotalPackets", Tuples.of(AggsFunctionEnum.SUM, "icmp6_total_packets"));
    aggs.put("otherTotalPackets", Tuples.of(AggsFunctionEnum.SUM, "other_total_packets"));
    // 以太网类型统计
    aggs.put("ipv4Frames", Tuples.of(AggsFunctionEnum.SUM, "ipv4_frames"));
    aggs.put("ipv6Frames", Tuples.of(AggsFunctionEnum.SUM, "ipv6_frames"));
    aggs.put("arpFrames", Tuples.of(AggsFunctionEnum.SUM, "arp_frames"));
    aggs.put("ieee8021xFrames", Tuples.of(AggsFunctionEnum.SUM, "ieee8021x_frames"));
    aggs.put("ipxFrames", Tuples.of(AggsFunctionEnum.SUM, "ipx_frames"));
    aggs.put("lacpFrames", Tuples.of(AggsFunctionEnum.SUM, "lacp_frames"));
    aggs.put("mplsFrames", Tuples.of(AggsFunctionEnum.SUM, "mpls_frames"));
    aggs.put("stpFrames", Tuples.of(AggsFunctionEnum.SUM, "stp_frames"));
    aggs.put("otherFrames", Tuples.of(AggsFunctionEnum.SUM, "other_frames"));
    // 数据包类型统计
    aggs.put("unicastBytes", Tuples.of(AggsFunctionEnum.SUM, "unicast_bytes"));
    aggs.put("broadcastBytes", Tuples.of(AggsFunctionEnum.SUM, "broadcast_bytes"));
    aggs.put("multicastBytes", Tuples.of(AggsFunctionEnum.SUM, "multicast_bytes"));
    // 分片包统计
    aggs.put("fragmentTotalBytes", Tuples.of(AggsFunctionEnum.SUM, "fragment_total_bytes"));
    aggs.put("fragmentTotalPackets", Tuples.of(AggsFunctionEnum.SUM, "fragment_total_packets"));

    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp");
    aggs.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(TABLE_METRIC_NETWORK_IN_SECOND);
    sql.append(whereSql.toString());
    sql.append(" group by timestamp ");
    sql.append(" order by timestamp asc ");

    return metricDataConversion(
        queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper()));
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#queryServiceStatistics(java.util.Date, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryServiceStatistics(Date startTime,
      List<Tuple2<String, String>> serviceNetworkIds) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(null, startTime, FpcCmsConstants.SOURCE_TYPE_SERVICE, null, serviceNetworkIds,
        whereSql, params);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggs.put("alertCounts", Tuples.of(AggsFunctionEnum.SUM, "alert_counts"));
    aggs.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
    aggs.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
    aggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    aggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    // tcp客户端网络时延
    aggs.put("tcpClientNetworkLatency",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency"));
    aggs.put("tcpClientNetworkLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_counts"));
    aggs.put("tcpClientNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
        "tcpClientNetworkLatency, tcpClientNetworkLatencyCounts"));
    // tcp服务端网络时延
    aggs.put("tcpServerNetworkLatency",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency"));
    aggs.put("tcpServerNetworkLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_counts"));
    aggs.put("tcpServerNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
        "tcpServerNetworkLatency, tcpServerNetworkLatencyCounts"));
    // tcp重传率
    aggs.put("tcpPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_packets"));
    aggs.put("tcpRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_retransmission_packets"));
    aggs.put("tcpRetransmissionRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpRetransmissionPackets, tcpPackets"));
    // tcp客户端重传率
    aggs.put("tcpClientPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets"));
    aggs.put("tcpClientRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
    aggs.put("tcpClientRetransmissionRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpClientRetransmissionPackets, tcpClientPackets"));
    // tcp服务端重传率
    aggs.put("tcpServerPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets"));
    aggs.put("tcpServerRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));
    aggs.put("tcpServerRetransmissionRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpServerRetransmissionPackets, tcpServerPackets"));
    // 分片包统计
    aggs.put("fragmentTotalBytes", Tuples.of(AggsFunctionEnum.SUM, "fragment_total_bytes"));
    aggs.put("fragmentTotalPackets", Tuples.of(AggsFunctionEnum.SUM, "fragment_total_packets"));

    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp");
    aggs.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(TABLE_METRIC_SERVICE_IN_SECOND);
    sql.append(whereSql.toString());
    sql.append(" group by timestamp ");
    sql.append(" order by timestamp asc ");

    return metricDataConversion(
        queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper()));
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#queryPayloadStatistics(java.util.Date, java.lang.String, java.util.List, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryPayloadStatistics(Date startTime, String sourceType,
      List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(null, startTime, sourceType, networkIds, serviceNetworkIds, whereSql, params);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggs.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
    aggs.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
    aggs.put("establishedSessions", Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));
    aggs.put("concurrentSessions", Tuples.of(AggsFunctionEnum.SUM, "concurrent_sessions"));
    aggs.put("uniqueIpCounts", Tuples.of(AggsFunctionEnum.SUM, "unique_ip_counts"));
    aggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    aggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    aggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    aggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    aggs.put("filterDiscardBytes", Tuples.of(AggsFunctionEnum.SUM, "filter_discard_bytes"));
    aggs.put("overloadDiscardBytes", Tuples.of(AggsFunctionEnum.SUM, "overload_discard_bytes"));
    aggs.put("deduplicationBytes", Tuples.of(AggsFunctionEnum.SUM, "deduplication_bytes"));

    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp");
    aggs.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(TABLE_METRIC_PAYLOAD_IN_SECOND);
    sql.append(whereSql.toString());
    sql.append(" group by timestamp ");
    sql.append(" order by timestamp asc ");

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#queryPerformanceStatistics(java.util.Date, java.lang.String, java.util.List, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryPerformanceStatistics(Date startTime, String sourceType,
      List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(null, startTime, sourceType, networkIds, serviceNetworkIds, whereSql, params);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // tcp客户端网络时延
    aggs.put("tcpClientNetworkLatency",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency"));
    aggs.put("tcpClientNetworkLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_counts"));
    aggs.put("tcpClientNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
        "tcpClientNetworkLatency, tcpClientNetworkLatencyCounts"));
    // tcp服务端网络时延
    aggs.put("tcpServerNetworkLatency",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency"));
    aggs.put("tcpServerNetworkLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_counts"));
    aggs.put("tcpServerNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
        "tcpServerNetworkLatency, tcpServerNetworkLatencyCounts"));
    // 服务器响应时延
    aggs.put("serverResponseLatency", Tuples.of(AggsFunctionEnum.SUM, "server_response_latency"));
    aggs.put("serverResponseLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, "server_response_latency_counts"));
    aggs.put("serverResponseLatencyAvg",
        Tuples.of(AggsFunctionEnum.DIVIDE, "serverResponseLatency, serverResponseLatencyCounts"));
    aggs.put("serverResponseFastCounts",
        Tuples.of(AggsFunctionEnum.SUM, "server_response_fast_counts"));
    aggs.put("serverResponseNormalCounts",
        Tuples.of(AggsFunctionEnum.SUM, "server_response_normal_counts"));
    aggs.put("serverResponseTimeoutCounts",
        Tuples.of(AggsFunctionEnum.SUM, "server_response_timeout_counts"));
    aggs.put("tcpClientRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
    aggs.put("tcpServerRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));

    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp");
    aggs.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(TABLE_METRIC_PERFORMANCE_IN_SECOND);
    sql.append(whereSql.toString());
    sql.append(" group by timestamp ");
    sql.append(" order by timestamp asc ");

    return metricDataConversion(
        queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper()));
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#queryTcpStatistics(java.util.Date, java.lang.String, java.util.List, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryTcpStatistics(Date startTime, String sourceType,
      List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(null, startTime, sourceType, networkIds, serviceNetworkIds, whereSql, params);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggs.put("tcpEstablishedSuccessCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_established_success_counts"));
    aggs.put("tcpEstablishedFailCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_established_fail_counts"));
    aggs.put("tcpClientSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_client_syn_packets"));
    aggs.put("tcpServerSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_server_syn_packets"));
    aggs.put("tcpClientRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
    aggs.put("tcpServerRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));
    aggs.put("tcpClientZeroWindowPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_zero_window_packets"));
    aggs.put("tcpServerZeroWindowPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_zero_window_packets"));

    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp");
    aggs.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(TABLE_METRIC_TCP_IN_SECOND);
    sql.append(whereSql.toString());
    sql.append(" group by timestamp ");
    sql.append(" order by timestamp asc ");

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#queryDscpStatistics(java.util.Date, java.lang.String, java.util.List, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryDscpStatistics(Date startTime, String sourceType,
      List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(null, startTime, sourceType, networkIds, serviceNetworkIds, whereSql, params);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggs.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));

    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp, type");
    aggs.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(TABLE_METRIC_DSCP_IN_SECOND);
    sql.append(whereSql.toString());
    sql.append(" group by timestamp, type ");
    sql.append(" order by timestamp asc ");

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#queryL3DeviceStatistics(java.util.Date, java.lang.String, java.util.List, java.util.List)
   */
  @Override
  public Map<String, List<Map<String, Object>>> queryL3DeviceStatistics(Date timestamp,
      String sourceType, List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(timestamp, null, sourceType, networkIds, serviceNetworkIds, whereSql, params);

    StringBuilder sql = new StringBuilder();
    sql.append("select total_bytes_top, total_sessions_top ");
    sql.append(" from ").append(TABLE_METRIC_L3DEVICE_IN_SECOND);
    sql.append(whereSql.toString());

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    Map<String,
        Long> totalBytesTopMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String,
        Long> totalSessionsTopMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.forEach(item -> {
      String total_bytes_top = JsonHelper.serialize(item.get("total_bytes_top"), false);
      if (StringUtils.isNotBlank(total_bytes_top)) {
        Map<String, Long> totalBytesTopForSensor = JsonHelper.deserialize(total_bytes_top,
            new TypeReference<Map<String, Long>>() {
            }, false);
        totalBytesTopForSensor.forEach((ip, totalBytes) -> {
          Long total = totalBytesTopMap.getOrDefault(ip, 0L);
          totalBytesTopMap.put(ip, total + totalBytes);
        });
      }

      String total_sessions_top = JsonHelper.serialize(item.get("total_sessions_top"), false);
      if (StringUtils.isNotBlank(total_sessions_top)) {
        Map<String, Long> totalSessionsTopForSensor = JsonHelper.deserialize(total_sessions_top,
            new TypeReference<Map<String, Long>>() {
            }, false);
        totalSessionsTopForSensor.forEach((ip, totalSessions) -> {
          Long total = totalSessionsTopMap.getOrDefault(ip, 0L);
          totalSessionsTopMap.put(ip, total + totalSessions);
        });
      }
    });

    Map<String, List<Map<String, Object>>> topMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<Map<String, Object>> totalBytes = sortValue(totalBytesTopMap).entrySet().stream()
        .map(item -> {
          Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          map.put("ip", item.getKey());
          map.put("value", item.getValue());

          return map;
        }).collect(Collectors.toList());
    topMap.put("totalBytes", totalBytes);

    List<Map<String, Object>> totalSessions = sortValue(totalSessionsTopMap).entrySet().stream()
        .map(item -> {
          Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          map.put("ip", item.getKey());
          map.put("value", item.getValue());

          return map;
        }).collect(Collectors.toList());
    topMap.put("totalSessions", totalSessions);

    return topMap;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#queryIpConversationStatistics(java.util.Date, java.lang.String, java.util.List, java.util.List)
   */
  @Override
  public Map<String, List<Map<String, Object>>> queryIpConversationStatistics(Date timestamp,
      String sourceType, List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(timestamp, null, sourceType, networkIds, serviceNetworkIds, whereSql, params);

    StringBuilder sql = new StringBuilder();
    sql.append("select total_bytes_top, total_sessions_top ");
    sql.append(" from ").append(TABLE_METRIC_IP_CONVERSATION_IN_SECOND);
    sql.append(whereSql.toString());

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    Map<String,
        Long> totalBytesTopMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String,
        Long> totalSessionsTopMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.forEach(item -> {
      String total_bytes_top = JsonHelper.serialize(item.get("total_bytes_top"), false);
      if (StringUtils.isNotBlank(total_bytes_top)) {
        Map<String, Long> totalBytesTopForSensor = JsonHelper.deserialize(total_bytes_top,
            new TypeReference<Map<String, Long>>() {
            }, false);
        totalBytesTopForSensor.forEach((ip, totalBytes) -> {
          Long total = totalBytesTopMap.getOrDefault(ip, 0L);
          totalBytesTopMap.put(ip, total + totalBytes);
        });
      }

      String total_sessions_top = JsonHelper.serialize(item.get("total_sessions_top"), false);
      if (StringUtils.isNotBlank(total_sessions_top)) {
        Map<String, Long> totalSessionsTopForSensor = JsonHelper.deserialize(total_sessions_top,
            new TypeReference<Map<String, Long>>() {
            }, false);
        totalSessionsTopForSensor.forEach((ip, totalSessions) -> {
          Long total = totalSessionsTopMap.getOrDefault(ip, 0L);
          totalSessionsTopMap.put(ip, total + totalSessions);
        });
      }
    });

    Map<String, List<Map<String, Object>>> topMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<Map<String, Object>> totalBytes = sortValue(totalBytesTopMap).entrySet().stream()
        .map(item -> {
          Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          String key = item.getKey();
          map.put("ipA", StringUtils.substringBefore(key, "_"));
          map.put("ipB", StringUtils.substringAfterLast(key, "_"));
          map.put("value", item.getValue());

          return map;
        }).collect(Collectors.toList());
    topMap.put("totalBytes", totalBytes);

    List<Map<String, Object>> totalSessions = sortValue(totalSessionsTopMap).entrySet().stream()
        .map(item -> {
          Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          String key = item.getKey();
          map.put("ipA", StringUtils.substringBefore(key, "_"));
          map.put("ipB", StringUtils.substringAfterLast(key, "_"));
          map.put("value", item.getValue());

          return map;
        }).collect(Collectors.toList());
    topMap.put("totalSessions", totalSessions);

    return topMap;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#batchSaveNetworkMetrics(java.util.List, java.util.Date)
   */
  @Override
  public int batchSaveNetworkMetrics(List<Map<String, Object>> metricData, Date startTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_METRIC_NETWORK_IN_SECOND);
    sql.append("(timestamp, start_time, network_id, alert_counts, ");
    sql.append("total_bytes,total_packets,upstream_bytes,downstream_bytes,");
    sql.append(
        "tiny_packets,small_packets,medium_packets,big_packets,large_packets,huge_packets,jumbo_packets,packet_length_avg,");
    sql.append(
        "tcp_total_packets,udp_total_packets,icmp_total_packets,icmp6_total_packets,other_total_packets,");
    sql.append(
        "ipv4_frames,ipv6_frames,arp_frames,ieee8021x_frames,ipx_frames,lacp_frames,mpls_frames,stp_frames,other_frames,");
    sql.append("unicast_bytes,broadcast_bytes,multicast_bytes,");
    sql.append("fragment_total_bytes,fragment_total_packets,");
    sql.append("tcp_packets, tcp_retransmission_packets, ");
    sql.append("tcp_client_packets, tcp_client_retransmission_packets, ");
    sql.append("tcp_server_packets, tcp_server_retransmission_packets, ");
    sql.append("tcp_client_network_latency,tcp_client_network_latency_counts,");
    sql.append("tcp_server_network_latency,tcp_server_network_latency_counts) ");

    sql.append(" values (:timestamp, :startTime, :networkId, :alertCounts, ");
    sql.append(":totalBytes,:totalPackets,:upstreamBytes,:downstreamBytes,");
    sql.append(
        ":tinyPackets,:smallPackets,:mediumPackets,:bigPackets,:largePackets,:hugePackets,:jumboPackets,:packetLengthAvg,");
    sql.append(
        ":tcpTotalPackets,:udpTotalPackets,:icmpTotalPackets,:icmp6TotalPackets,:otherTotalPackets,");
    sql.append(
        ":ipv4Frames,:ipv6Frames,:arpFrames,:ieee8021xFrames,:ipxFrames,:lacpFrames,:mplsFrames,:stpFrames,:otherFrames,");
    sql.append(":unicastBytes,:broadcastBytes,:multicastBytes,");
    sql.append(":fragmentTotalBytes,:fragmentTotalPackets,");
    sql.append(":tcpPackets, :tcpRetransmissionPackets, ");
    sql.append(":tcpClientPackets, :tcpClientRetransmissionPackets, ");
    sql.append(":tcpServerPackets, :tcpServerRetransmissionPackets, ");
    sql.append(":tcpClientNetworkLatency, :tcpClientNetworkLatencyCounts,");
    sql.append(":tcpServerNetworkLatency, :tcpServerNetworkLatencyCounts)");

    metricData.forEach(item -> {
      item.put("timestamp", DateUtils.transformDateString(MapUtils.getString(item, "timestamp"),
          REALTIME_STATISTICS_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      item.put("startTime",
          DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(metricData);
    return Arrays
        .stream(clickHouseTemplate.getJdbcTemplate().batchUpdate(sql.toString(), batchSource))
        .sum();
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#batchSaveServiceMetrics(java.util.List, java.util.Date)
   */
  @Override
  public int batchSaveServiceMetrics(List<Map<String, Object>> metricData, Date startTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_METRIC_SERVICE_IN_SECOND);
    sql.append("(timestamp, start_time, network_id, service_id, alert_counts, ");
    sql.append("total_bytes,total_packets,upstream_bytes,downstream_bytes,");
    sql.append("fragment_total_bytes,fragment_total_packets,");
    sql.append("tcp_packets, tcp_retransmission_packets, ");
    sql.append("tcp_client_packets, tcp_client_retransmission_packets, ");
    sql.append("tcp_server_packets, tcp_server_retransmission_packets, ");
    sql.append("tcp_client_network_latency,tcp_client_network_latency_counts,");
    sql.append("tcp_server_network_latency,tcp_server_network_latency_counts) ");

    sql.append(" values (:timestamp, :startTime, :networkId, :serviceId, :alertCounts, ");
    sql.append(":totalBytes,:totalPackets,:upstreamBytes,:downstreamBytes,");
    sql.append(":fragmentTotalBytes,:fragmentTotalPackets,");
    sql.append(":tcpPackets, :tcpRetransmissionPackets, ");
    sql.append(":tcpClientPackets, :tcpClientRetransmissionPackets, ");
    sql.append(":tcpServerPackets, :tcpServerRetransmissionPackets, ");
    sql.append(":tcpClientNetworkLatency, :tcpClientNetworkLatencyCounts,");
    sql.append(":tcpServerNetworkLatency, :tcpServerNetworkLatencyCounts)");

    metricData.forEach(item -> {
      item.put("timestamp", DateUtils.transformDateString(MapUtils.getString(item, "timestamp"),
          REALTIME_STATISTICS_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      item.put("startTime",
          DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(metricData);
    return Arrays
        .stream(clickHouseTemplate.getJdbcTemplate().batchUpdate(sql.toString(), batchSource))
        .sum();
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#batchSavePayloadMetrics(java.util.List, java.util.Date, java.lang.String, java.lang.String)
   */
  @Override
  public int batchSavePayloadMetrics(List<Map<String, Object>> metricData, Date startTime,
      String networkId, String serviceId) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_METRIC_PAYLOAD_IN_SECOND);
    sql.append("(timestamp, start_time, network_id, service_id, ");
    sql.append(
        "total_bytes,total_packets,upstream_bytes,upstream_packets,downstream_bytes,downstream_packets,");
    sql.append("filter_discard_bytes,overload_discard_bytes,deduplication_bytes,");
    sql.append("established_sessions,concurrent_sessions,unique_ip_counts)");

    sql.append(" values (:timestamp, :startTime, :networkId, :serviceId, ");
    sql.append(
        ":totalBytes,:totalPackets,:upstreamBytes,:upstreamPackets,:downstreamBytes,:downstreamPackets,");
    sql.append(":filterDiscardBytes,:overloadDiscardBytes,:deduplicationBytes,");
    sql.append(":establishedSessions,:concurrentSessions,:uniqueIpCounts)");

    metricData.forEach(item -> {
      item.put("timestamp", DateUtils.transformDateString(MapUtils.getString(item, "timestamp"),
          REALTIME_STATISTICS_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      item.put("startTime",
          DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      item.put("networkId", networkId);
      item.put("serviceId", serviceId);
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(metricData);
    return Arrays
        .stream(clickHouseTemplate.getJdbcTemplate().batchUpdate(sql.toString(), batchSource))
        .sum();
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#batchSavePerformanceMetrics(java.util.List, java.util.Date, java.lang.String, java.lang.String)
   */
  @Override
  public int batchSavePerformanceMetrics(List<Map<String, Object>> metricData, Date startTime,
      String networkId, String serviceId) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_METRIC_PERFORMANCE_IN_SECOND);
    sql.append("(timestamp, start_time, network_id, service_id, ");
    sql.append("tcp_client_network_latency,tcp_client_network_latency_counts,");
    sql.append("tcp_server_network_latency,tcp_server_network_latency_counts,");
    sql.append("server_response_latency,server_response_latency_counts,");
    sql.append(
        "server_response_fast_counts,server_response_normal_counts,server_response_timeout_counts,");
    sql.append("tcp_client_retransmission_packets,tcp_server_retransmission_packets)");

    sql.append(" values (:timestamp, :startTime, :networkId, :serviceId, ");
    sql.append(":tcpClientNetworkLatency, :tcpClientNetworkLatencyCounts,");
    sql.append(":tcpServerNetworkLatency, :tcpServerNetworkLatencyCounts,");
    sql.append(":serverResponseLatency, :serverResponseLatencyCounts,");
    sql.append(
        ":serverResponseFastCounts,:serverResponseNormalCounts,:serverResponseTimeoutCounts,");
    sql.append(":tcpClientRetransmissionPackets,:tcpServerRetransmissionPackets)");

    metricData.forEach(item -> {
      item.put("timestamp", DateUtils.transformDateString(MapUtils.getString(item, "timestamp"),
          REALTIME_STATISTICS_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      item.put("startTime",
          DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      item.put("networkId", networkId);
      item.put("serviceId", serviceId);
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(metricData);
    return Arrays
        .stream(clickHouseTemplate.getJdbcTemplate().batchUpdate(sql.toString(), batchSource))
        .sum();
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#batchSaveTcpMetrics(java.util.List, java.util.Date, java.lang.String, java.lang.String)
   */
  @Override
  public int batchSaveTcpMetrics(List<Map<String, Object>> metricData, Date startTime,
      String networkId, String serviceId) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_METRIC_TCP_IN_SECOND);
    sql.append("(timestamp, start_time, network_id, service_id, ");
    sql.append("tcp_client_syn_packets, tcp_server_syn_packets,");
    sql.append("tcp_established_success_counts, tcp_established_fail_counts,");
    sql.append("tcp_client_retransmission_packets, tcp_server_retransmission_packets,");
    sql.append("tcp_client_zero_window_packets, tcp_server_zero_window_packets)");

    sql.append(" values (:timestamp, :startTime, :networkId, :serviceId, ");
    sql.append(":tcpClientSynPackets, :tcpServerSynPackets,");
    sql.append(":tcpEstablishedSuccessCounts, :tcpEstablishedFailCounts,");
    sql.append(":tcpClientRetransmissionPackets, :tcpServerRetransmissionPackets,");
    sql.append(":tcpClientZeroWindowPackets, :tcpServerZeroWindowPackets)");

    metricData.forEach(item -> {
      item.put("timestamp", DateUtils.transformDateString(MapUtils.getString(item, "timestamp"),
          REALTIME_STATISTICS_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      item.put("startTime",
          DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      item.put("networkId", networkId);
      item.put("serviceId", serviceId);
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(metricData);
    return Arrays
        .stream(clickHouseTemplate.getJdbcTemplate().batchUpdate(sql.toString(), batchSource))
        .sum();
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#batchSaveDscpMetrics(java.util.List, java.util.Date, java.lang.String, java.lang.String)
   */
  @Override
  public int batchSaveDscpMetrics(List<Map<String, Object>> metricData, Date startTime,
      String networkId, String serviceId) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_METRIC_DSCP_IN_SECOND);
    sql.append("(timestamp, start_time, network_id, service_id, ");
    sql.append("type, total_bytes)");

    sql.append(" values (:timestamp, :startTime, :networkId, :serviceId, ");
    sql.append(":type, :totalBytes)");

    metricData.forEach(item -> {
      item.put("timestamp", DateUtils.transformDateString(MapUtils.getString(item, "timestamp"),
          REALTIME_STATISTICS_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      item.put("startTime",
          DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      item.put("networkId", networkId);
      item.put("serviceId", serviceId);
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(metricData);
    return Arrays
        .stream(clickHouseTemplate.getJdbcTemplate().batchUpdate(sql.toString(), batchSource))
        .sum();
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#saveL3DeviceMetric(java.util.Map)
   */
  @Override
  public int saveL3DeviceMetric(Map<String, Object> metricData) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_METRIC_L3DEVICE_IN_SECOND);
    sql.append("(timestamp, network_id, service_id, ");
    sql.append(" total_bytes_top, total_sessions_top)");

    sql.append(" values (:timestamp, :networkId, :serviceId, ");
    sql.append(":totalBytesTop, :totalSessionsTop)");

    metricData.put("timestamp",
        DateUtils.transformDateString(MapUtils.getString(metricData, "timestamp"),
            REALTIME_STATISTICS_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    return clickHouseTemplate.getJdbcTemplate().update(sql.toString(), metricData);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#saveIpConversationMetric(java.util.Map)
   */
  @Override
  public int saveIpConversationMetric(Map<String, Object> metricData) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_METRIC_IP_CONVERSATION_IN_SECOND);
    sql.append("(timestamp, network_id, service_id, ");
    sql.append(" total_bytes_top, total_sessions_top)");

    sql.append(" values (:timestamp, :networkId, :serviceId, ");
    sql.append(":totalBytesTop, :totalSessionsTop)");

    metricData.put("timestamp",
        DateUtils.transformDateString(MapUtils.getString(metricData, "timestamp"),
            REALTIME_STATISTICS_TIME_FORMAT, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    return clickHouseTemplate.getJdbcTemplate().update(sql.toString(), metricData);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao#deleteExpireMetricData(java.util.Date)
   */
  @Override
  public void deleteExpireMetricData(Date expireDate) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(1);
    map.put("partName", DateUtils.toStringFormat(expireDate, "yyyyMMdd"));

    // TABLE_METRIC_NETWORK_IN_SECOND
    StringBuilder networkSql = new StringBuilder();
    networkSql.append("alter table ").append(TABLE_METRIC_NETWORK_IN_SECOND);
    networkSql.append(" drop partition :partName ");
    clickHouseTemplate.getJdbcTemplate().update(networkSql.toString(), map);

    // TABLE_METRIC_SERVICE_IN_SECOND
    StringBuilder serviceSql = new StringBuilder();
    serviceSql.append("alter table ").append(TABLE_METRIC_SERVICE_IN_SECOND);
    serviceSql.append(" drop partition :partName ");
    clickHouseTemplate.getJdbcTemplate().update(serviceSql.toString(), map);

    // TABLE_METRIC_L3DEVICE_IN_SECOND
    StringBuilder l3DeviceSql = new StringBuilder();
    l3DeviceSql.append("alter table ").append(TABLE_METRIC_L3DEVICE_IN_SECOND);
    l3DeviceSql.append(" drop partition :partName ");
    clickHouseTemplate.getJdbcTemplate().update(l3DeviceSql.toString(), map);

    // TABLE_METRIC_IP_CONVERSATION_IN_SECOND
    StringBuilder ipConversationsql = new StringBuilder();
    ipConversationsql.append("alter table ").append(TABLE_METRIC_IP_CONVERSATION_IN_SECOND);
    ipConversationsql.append(" drop partition :partName ");
    clickHouseTemplate.getJdbcTemplate().update(ipConversationsql.toString(), map);

    // TABLE_METRIC_DSCP_IN_SECOND
    StringBuilder dscpSql = new StringBuilder();
    dscpSql.append("alter table ").append(TABLE_METRIC_DSCP_IN_SECOND);
    dscpSql.append(" drop partition :partName ");
    clickHouseTemplate.getJdbcTemplate().update(dscpSql.toString(), map);

    // TABLE_METRIC_PAYLOAD_IN_SECOND
    StringBuilder payloadSql = new StringBuilder();
    payloadSql.append("alter table ").append(TABLE_METRIC_PAYLOAD_IN_SECOND);
    payloadSql.append(" drop partition :partName ");
    clickHouseTemplate.getJdbcTemplate().update(payloadSql.toString(), map);

    // TABLE_METRIC_PERFORMANCE_IN_SECOND
    StringBuilder performanceSql = new StringBuilder();
    performanceSql.append("alter table ").append(TABLE_METRIC_PERFORMANCE_IN_SECOND);
    performanceSql.append(" drop partition :partName ");
    clickHouseTemplate.getJdbcTemplate().update(performanceSql.toString(), map);

    // TABLE_METRIC_TCP_IN_SECOND
    StringBuilder tcpSql = new StringBuilder();
    tcpSql.append("alter table ").append(TABLE_METRIC_TCP_IN_SECOND);
    tcpSql.append(" drop partition :partName ");
    clickHouseTemplate.getJdbcTemplate().update(tcpSql.toString(), map);
  }

  /**
   * 完善过滤条件
   * @param timestamp 查询指定时间点数据
   * @param startTime 查询从开始时间往后所有数据
   * @param sourceType 数据源
   * @param networkIds 网络ID过滤
   * @param serviceNetworkIds 业务ID和网络ID组合过滤
   * @param whereSql 
   * @param params
   */
  private void enrichWhereSql(Date timestamp, Date startTime, String sourceType,
      List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds,
      StringBuilder whereSql, Map<String, Object> params) {
    whereSql.append(" where 1 = 1 ");

    // 过滤时间
    if (timestamp != null) {
      whereSql.append(" and timestamp = toDateTime64(:timestamp, 3, 'UTC') ");
      params.put("timestamp",
          DateUtils.toStringFormat(timestamp, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (startTime != null) {
      whereSql.append(" and start_time = toDateTime64(:startTime, 3, 'UTC') ");
      whereSql.append(" and timestamp >= toDateTime64(:startTime, 3, 'UTC') ");
      params.put("startTime",
          DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }

    // 过滤网络、业务
    if (StringUtils.equalsAny(sourceType, FpcCmsConstants.SOURCE_TYPE_NETWORK,
        FpcCmsConstants.SOURCE_TYPE_NETWORK_GROUP)) {
      if (CollectionUtils.isNotEmpty(networkIds)) {
        whereSql.append(" and network_id in (:networkIds) and service_id = '' ");
        params.put("networkIds", networkIds);
      }
    } else {
      if (CollectionUtils.isNotEmpty(serviceNetworkIds)) {
        whereSql.append(" and ( 1=2 ");
        for (int i = 0; i < serviceNetworkIds.size(); i++) {
          whereSql.append(String
              .format(" or (network_id = :networkId%s and service_id = :serviceId%s) ", i, i));
          params.put("networkId" + i, serviceNetworkIds.get(i).getT1());
          params.put("serviceId" + i, serviceNetworkIds.get(i).getT2());
        }
        whereSql.append(" ) ");
      }
    }
  }

  private <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result = clickHouseTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("queryMetricInSecond has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("queryMetricInSecond failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else if (errorCode == 60 || errorCode == 102) {
        // 60(UNKNOWN_TABLE，表不存在)：集群内没有探针节点，仅存在默认节点，默认节点会从本地查找数据表，此时就会返回表不存在的错误码
        LOGGER.info("not found sensor node.");
      } else if (errorCode == 279 || errorCode == 210 || errorCode == 209) {
        // 279 (ALL_CONNECTION_TRIES_FAILED) 探针上clickhouse端口不通
        // 210 (NETWORK_ERROR)
        // 209 (SOCKET_TIMEOUT)
        LOGGER.warn("connection sensor clikhousenode error.", e.getMessage());
      } else {
        // 其他异常重新抛出
        throw e;
      }
    }

    return result;
  }

  /**
   * 按value降序排序
   * @param metricValues
   * @return
   */
  private Map<String, Long> sortValue(Map<String, Long> metricValues) {
    return metricValues.entrySet().stream().sorted(new Comparator<Entry<String, Long>>() {

      @Override
      public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
        return o2.getValue().compareTo(o1.getValue());
      }
    }).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldVal, newVal) -> oldVal,
        LinkedHashMap::new));
  }

  private List<Map<String, Object>> metricDataConversion(List<Map<String, Object>> metricResult) {
    metricResult.forEach(metricData -> {
      if (metricData.containsKey("tcpClientNetworkLatencyAvg")) {
        metricData.put("tcpClientNetworkLatencyAvg",
            StringUtils.equalsAny(String.valueOf(metricData.get("tcpClientNetworkLatencyAvg")),
                DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                    : new BigDecimal(String.valueOf(metricData.get("tcpClientNetworkLatencyAvg")))
                        .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }
      if (metricData.containsKey("tcpServerNetworkLatencyAvg")) {
        metricData.put("tcpServerNetworkLatencyAvg",
            StringUtils.equalsAny(String.valueOf(metricData.get("tcpServerNetworkLatencyAvg")),
                DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                    : new BigDecimal(String.valueOf(metricData.get("tcpServerNetworkLatencyAvg")))
                        .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }
      if (metricData.containsKey("serverResponseLatencyAvg")) {
        metricData.put("serverResponseLatencyAvg",
            StringUtils.equalsAny(String.valueOf(metricData.get("serverResponseLatencyAvg")),
                DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                    : new BigDecimal(String.valueOf(metricData.get("serverResponseLatencyAvg")))
                        .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }
      if (metricData.containsKey("tcpRetransmissionRate")) {
        metricData.put("tcpRetransmissionRate",
            StringUtils.equalsAny(String.valueOf(metricData.get("tcpRetransmissionRate")),
                DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                    : new BigDecimal(String.valueOf(metricData.get("tcpRetransmissionRate")))
                        .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }
      if (metricData.containsKey("tcpClientRetransmissionRate")) {
        metricData.put("tcpClientRetransmissionRate",
            StringUtils.equalsAny(String.valueOf(metricData.get("tcpClientRetransmissionRate")),
                DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                    : new BigDecimal(String.valueOf(metricData.get("tcpClientRetransmissionRate")))
                        .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }
      if (metricData.containsKey("tcpServerRetransmissionRate")) {
        metricData.put("tcpServerRetransmissionRate",
            StringUtils.equalsAny(String.valueOf(metricData.get("tcpServerRetransmissionRate")),
                DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                    : new BigDecimal(String.valueOf(metricData.get("tcpServerRetransmissionRate")))
                        .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }
    });

    return metricResult;
  }

}
