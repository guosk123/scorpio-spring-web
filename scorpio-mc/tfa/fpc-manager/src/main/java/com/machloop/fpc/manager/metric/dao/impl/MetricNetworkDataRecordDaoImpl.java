package com.machloop.fpc.manager.metric.dao.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.pipeline.BucketScriptPipelineAggregationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.global.dao.ElasticIndexDao;
import com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl;
import com.machloop.fpc.manager.helper.Spl2DslHelper;
import com.machloop.fpc.manager.metric.dao.MetricNetworkDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricNetworkDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
// @Repository
public class MetricNetworkDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricNetworkDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricNetworkDataRecordDaoImpl.class);

  private static final String TERM_FIELD = "network_id";

  private static final Tuple2<String, Boolean> AGGS_TERM_FIELD = Tuples.of("network_id", false);

  @Autowired
  private RestHighLevelClient restHighLevelClient;

  @Autowired
  private ElasticIndexDao elasticIndexDao;

  @Autowired
  private Spl2DslHelper spl2DslHelper;

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricNetworkDataRecordDao#queryMetricNetworks(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.util.List, java.util.List)
   */
  @Override
  public List<MetricNetworkDataRecordDO> queryMetricNetworks(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, List<String> metrics, List<String> networkIds) {
    Map<String, String> aggsFields = getAggsFields(metrics);
    List<BucketScriptPipelineAggregationBuilder> bucketScriptAggsFields = getBucketScriptAggsFields(
        metrics);

    List<MetricNetworkDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      QueryBuilder termQuery = null;
      if (CollectionUtils.isNotEmpty(networkIds)) {
        termQuery = QueryBuilders.termsQuery(TERM_FIELD, networkIds);
      }

      List<Map<String, Object>> aggsResult = termMetricAggregate(
          Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
          Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()),
          convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          TERM_FIELD, queryVO.getDsl(), termQuery, aggsFields, bucketScriptAggsFields, sortProperty,
          sortDirection);

      aggsResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query network metric.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricNetworkDataRecordDao#queryMetricNetworkHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, boolean, java.util.List)
   */
  @Override
  public List<MetricNetworkDataRecordDO> queryMetricNetworkHistograms(MetricQueryVO queryVO,
      boolean extendedBound, List<String> metrics) {
    Map<String, String> aggsFields = getAggsFields(metrics);
    List<BucketScriptPipelineAggregationBuilder> bucketScriptAggsFields = getBucketScriptAggsFields(
        metrics);

    List<MetricNetworkDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      // DSL为空时增加网络过滤条件
      QueryBuilder queryBuilder = null;
      if (StringUtils.isBlank(queryVO.getDsl()) && StringUtils.isNotBlank(queryVO.getNetworkId())) {
        queryBuilder = QueryBuilders.termQuery(TERM_FIELD, queryVO.getNetworkId());
      }

      List<Map<String, Object>> aggsResult = dateHistogramTermMetricAggregate(
          Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
          Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()), queryVO.getInterval(),
          convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          TERM_FIELD, queryVO.getDsl(), queryBuilder, aggsFields, bucketScriptAggsFields,
          extendedBound);

      aggsResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query network histogram.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricNetworkDataRecordDao#queryMetricNetworkHistogramsWithAggsFields(com.machloop.fpc.manager.metric.vo.MetricQueryVO, boolean, java.util.List)
   */
  public List<Map<String, Object>> queryMetricNetworkHistogramsWithAggsFields(MetricQueryVO queryVO,
      boolean extendedBound, List<String> aggsFields) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      // 获取聚合字段的聚合方式
      Map<String, String> allAggsFields = getAggsFields(Lists.newArrayList("ALL"));
      Map<String,
          BucketScriptPipelineAggregationBuilder> allBucketScriptAggsFields = getBucketScriptAggsFields(
              Lists.newArrayList("ALL")).stream().collect(
                  Collectors.toMap(BucketScriptPipelineAggregationBuilder::getName, agg -> agg));
      Map<String,
          String> selectedAggsFields = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      List<BucketScriptPipelineAggregationBuilder> selectedBucketScriptAggsFields = Lists
          .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      aggsFields.forEach(aggsField -> {
        getCombinationAggsFields(aggsField).forEach(item -> {
          if (allAggsFields.containsKey(item)) {
            selectedAggsFields.put(item, allAggsFields.get(item));
          } else if (allBucketScriptAggsFields.containsKey(item)) {
            selectedBucketScriptAggsFields.add(allBucketScriptAggsFields.get(item));
          }
        });
      });
      if (MapUtils.isEmpty(selectedAggsFields)
          && CollectionUtils.isEmpty(selectedBucketScriptAggsFields)) {
        return result;
      }

      // DSL为空时增加网络过滤条件
      QueryBuilder queryBuilder = null;
      if (StringUtils.isBlank(queryVO.getDsl()) && StringUtils.isNotBlank(queryVO.getNetworkId())) {
        queryBuilder = QueryBuilders.termQuery(TERM_FIELD, queryVO.getNetworkId());
      }

      result = dateHistogramTermMetricAggregate(
          Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
          Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()), queryVO.getInterval(),
          convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          TERM_FIELD, queryVO.getDsl(), queryBuilder, selectedAggsFields,
          selectedBucketScriptAggsFields, extendedBound);
    } catch (IOException e) {
      LOGGER.warn("failed to query network histogram.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricNetworkDataRecordDao#queryAllNetworkHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.util.List, boolean, java.util.List)
   */
  @Override
  public List<MetricNetworkDataRecordDO> queryAllNetworkHistograms(MetricQueryVO queryVO,
      List<String> networkIds, boolean extendedBound, List<String> metrics) {
    List<MetricNetworkDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      Map<String, String> aggsFields = getAggsFields(metrics);
      List<
          BucketScriptPipelineAggregationBuilder> bucketScriptAggsFields = getBucketScriptAggsFields(
              metrics);

      QueryBuilder queryBuilder = QueryBuilders.termsQuery("network_id", networkIds);
      List<Map<String, Object>> aggsResult = dateHistogramMetricAggregate(
          Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
          Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()), queryVO.getInterval(),
          convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO.getDsl(), queryBuilder, aggsFields, bucketScriptAggsFields, extendedBound);

      aggsResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query all main network histogram.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getElasticIndexDao()
   */
  @Override
  protected ElasticIndexDao getElasticIndexDao() {
    return elasticIndexDao;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getRestHighLevelClient()
   */
  @Override
  protected RestHighLevelClient getRestHighLevelClient() {
    return restHighLevelClient;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getSpl2DslHelper()
   */
  @Override
  protected Spl2DslHelper getSpl2DslHelper() {
    return spl2DslHelper;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getIndexName()
   */
  @Override
  protected String getIndexName() {
    return ManagerConstants.INDEX_METRIC_NETWORK_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getIndexAliasName()
   */
  @Override
  protected String getIndexAliasName() {
    return ManagerConstants.ALIAS_METRIC_NETWORK_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#aggregate(java.util.Date, java.util.Date, int, java.lang.String, com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl.WriteCallback)
   */
  @Override
  protected int aggregate(Date startTime, Date endTime, int interval, String indexName,
      WriteCallback writeCallback) throws IOException {
    int success = 0;
    List<Tuple2<String, Boolean>> termFields = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    termFields.add(AGGS_TERM_FIELD);
    Map<String, String> aggsFields = getAggsFields(Lists.newArrayList("ALL"));

    Map<String, Object> after = null;
    List<Map<String, Object>> batchList = null;
    do {
      List<String> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      Tuple2<Map<String, Object>,
          List<Map<String, Object>>> batchResult = compositeTermMetricAggregate(startTime, endTime,
              indexName, termFields, aggsFields, COMPOSITE_BATCH_SIZE, after);

      after = batchResult.getT1();
      batchList = batchResult.getT2();

      batchList.forEach(item -> {
        item.put("timestamp", endTime);
        result.add(JsonHelper.serialize(tranResultMapToDateRecord(item)));
      });
      success += writeCallback.process(result);
    } while (batchList.size() == COMPOSITE_BATCH_SIZE && !GracefulShutdownHelper.isShutdownNow());

    return success;
  }

  private Map<String, String> getAggsFields(List<String> metrics) {
    Map<String, String> aggsFields = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (metrics.contains(ManagerConstants.METRIC_NPM_FLOW) || metrics.contains("ALL")) {
      aggsFields.put("byteps_peak", "max");
      aggsFields.put("packetps_peak", "max");
      aggsFields.put("total_bytes", "sum");
      aggsFields.put("total_packets", "sum");
      aggsFields.put("downstream_bytes", "sum");
      aggsFields.put("downstream_packets", "sum");
      aggsFields.put("upstream_bytes", "sum");
      aggsFields.put("upstream_packets", "sum");
      aggsFields.put("filter_discard_bytes", "sum");
      aggsFields.put("filter_discard_packets", "sum");
      aggsFields.put("overload_discard_bytes", "sum");
      aggsFields.put("overload_discard_packets", "sum");
      aggsFields.put("deduplication_bytes", "sum");
      aggsFields.put("deduplication_packets", "sum");
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_IP_PROTOCOL) || metrics.contains("ALL")) {
      aggsFields.put("tcp_total_packets", "sum");
      aggsFields.put("udp_total_packets", "sum");
      aggsFields.put("icmp_total_packets", "sum");
      aggsFields.put("icmp6_total_packets", "sum");
      aggsFields.put("other_total_packets", "sum");
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_FRAGMENT) || metrics.contains("ALL")) {
      aggsFields.put("fragment_total_bytes", "sum");
      aggsFields.put("fragment_total_packets", "sum");
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_TCP) || metrics.contains("ALL")) {
      aggsFields.put("tcp_syn_packets", "sum");
      aggsFields.put("tcp_client_syn_packets", "sum");
      aggsFields.put("tcp_server_syn_packets", "sum");
      aggsFields.put("tcp_syn_ack_packets", "sum");
      aggsFields.put("tcp_syn_rst_packets", "sum");
      aggsFields.put("tcp_established_fail_counts", "sum");
      aggsFields.put("tcp_established_success_counts", "sum");
      aggsFields.put("tcp_established_time_avg", "avg");
      aggsFields.put("tcp_zero_window_packets", "sum");
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_FRAME_LENGTH) || metrics.contains("ALL")) {
      aggsFields.put("tiny_packets", "sum");
      aggsFields.put("small_packets", "sum");
      aggsFields.put("medium_packets", "sum");
      aggsFields.put("big_packets", "sum");
      aggsFields.put("large_packets", "sum");
      aggsFields.put("huge_packets", "sum");
      aggsFields.put("jumbo_packets", "sum");
      aggsFields.put("packet_length_avg", "avg");
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_SESSION) || metrics.contains("ALL")) {
      aggsFields.put("active_sessions", "sum");
      aggsFields.put("concurrent_sessions", "max");
      aggsFields.put("concurrent_tcp_sessions", "max");
      aggsFields.put("concurrent_udp_sessions", "max");
      aggsFields.put("concurrent_arp_sessions", "max");
      aggsFields.put("concurrent_icmp_sessions", "max");
      aggsFields.put("established_sessions", "sum");
      aggsFields.put("destroyed_sessions", "sum");
      aggsFields.put("established_tcp_sessions", "sum");
      aggsFields.put("established_udp_sessions", "sum");
      aggsFields.put("established_icmp_sessions", "sum");
      aggsFields.put("established_other_sessions", "sum");
      aggsFields.put("established_upstream_sessions", "sum");
      aggsFields.put("established_downstream_sessions", "sum");
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_ETHERNET_TYPE) || metrics.contains("ALL")) {
      aggsFields.put("ipv4_frames", "sum");
      aggsFields.put("ipv6_frames", "sum");
      aggsFields.put("arp_frames", "sum");
      aggsFields.put("ieee8021x_frames", "sum");
      aggsFields.put("ipx_frames", "sum");
      aggsFields.put("lacp_frames", "sum");
      aggsFields.put("mpls_frames", "sum");
      aggsFields.put("stp_frames", "sum");
      aggsFields.put("other_frames", "sum");
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_PACKET_TYPE) || metrics.contains("ALL")) {
      aggsFields.put("unicast_bytes", "sum");
      aggsFields.put("broadcast_bytes", "sum");
      aggsFields.put("multicast_bytes", "sum");
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_PERFORMANCE) || metrics.contains("ALL")) {
      aggsFields.put("tcp_client_network_latency", "sum");
      aggsFields.put("tcp_client_network_latency_counts", "sum");
      aggsFields.put("tcp_server_network_latency", "sum");
      aggsFields.put("tcp_server_network_latency_counts", "sum");
      aggsFields.put("server_response_latency", "sum");
      aggsFields.put("server_response_latency_counts", "sum");
      aggsFields.put("server_response_latency_peak", "max");
      aggsFields.put("tcp_client_retransmission_packets", "sum");
      aggsFields.put("tcp_client_packets", "sum");
      aggsFields.put("tcp_server_retransmission_packets", "sum");
      aggsFields.put("tcp_server_packets", "sum");
      aggsFields.put("tcp_client_zero_window_packets", "sum");
      aggsFields.put("tcp_server_zero_window_packets", "sum");
      aggsFields.put("server_response_fast_counts", "sum");
      aggsFields.put("server_response_normal_counts", "sum");
      aggsFields.put("server_response_timeout_counts", "sum");
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_UNIQUE_IP_COUNTS) || metrics.contains("ALL")) {
      aggsFields.put("unique_ip_counts", "max");
    }

    return aggsFields;
  }

  private List<BucketScriptPipelineAggregationBuilder> getBucketScriptAggsFields(
      List<String> metrics) {
    List<BucketScriptPipelineAggregationBuilder> bucketScriptList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    if (metrics.contains(ManagerConstants.METRIC_NPM_PERFORMANCE) || metrics.contains("ALL")) {
      setBucketScriptAggsKPIFields(bucketScriptList);
    }

    return bucketScriptList;
  }

  private MetricNetworkDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricNetworkDataRecordDO recordDO = new MetricNetworkDataRecordDO();
    tranKPIMapToDateRecord(item, recordDO);

    recordDO.setBytepsPeak(MapUtils.getLongValue(item, "byteps_peak"));
    recordDO.setPacketpsPeak(MapUtils.getLongValue(item, "packetps_peak"));
    recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "downstream_bytes"));
    recordDO.setDownstreamPackets(MapUtils.getLongValue(item, "downstream_packets"));
    recordDO.setUpstreamBytes(MapUtils.getLongValue(item, "upstream_bytes"));
    recordDO.setUpstreamPackets(MapUtils.getLongValue(item, "upstream_packets"));
    recordDO.setFilterDiscardBytes(MapUtils.getLongValue(item, "filter_discard_bytes"));
    recordDO.setFilterDiscardPackets(MapUtils.getLongValue(item, "filter_discard_packets"));
    recordDO.setOverloadDiscardBytes(MapUtils.getLongValue(item, "overload_discard_bytes"));
    recordDO.setOverloadDiscardPackets(MapUtils.getLongValue(item, "overload_discard_packets"));
    recordDO.setDeduplicationBytes(MapUtils.getLongValue(item, "deduplication_bytes"));
    recordDO.setDeduplicationPackets(MapUtils.getLongValue(item, "deduplication_packets"));

    recordDO.setTcpTotalPackets(MapUtils.getLongValue(item, "tcp_total_packets"));
    recordDO.setUdpTotalPackets(MapUtils.getLongValue(item, "udp_total_packets"));
    recordDO.setIcmpTotalPackets(MapUtils.getLongValue(item, "icmp_total_packets"));
    recordDO.setIcmp6TotalPackets(MapUtils.getLongValue(item, "icmp6_total_packets"));
    recordDO.setOtherTotalPackets(MapUtils.getLongValue(item, "other_total_packets"));

    recordDO.setFragmentTotalBytes(MapUtils.getLongValue(item, "fragment_total_bytes"));
    recordDO.setFragmentTotalPackets(MapUtils.getLongValue(item, "fragment_total_packets"));

    recordDO.setTcpSynPackets(MapUtils.getLongValue(item, "tcp_syn_packets"));
    recordDO.setTcpServerSynPackets(MapUtils.getLongValue(item, "tcp_server_syn_packets"));
    recordDO.setTcpClientSynPackets(MapUtils.getLongValue(item, "tcp_client_syn_packets"));
    recordDO.setTcpSynAckPackets(MapUtils.getLongValue(item, "tcp_syn_ack_packets"));
    recordDO.setTcpSynRstPackets(MapUtils.getLongValue(item, "tcp_syn_rst_packets"));
    recordDO.setTcpEstablishedTimeAvg(MapUtils.getLongValue(item, "tcp_established_time_avg"));
    recordDO.setTcpZeroWindowPackets(MapUtils.getLongValue(item, "tcp_zero_window_packets"));

    recordDO.setTinyPackets(MapUtils.getLongValue(item, "tiny_packets"));
    recordDO.setSmallPackets(MapUtils.getLongValue(item, "small_packets"));
    recordDO.setMediumPackets(MapUtils.getLongValue(item, "medium_packets"));
    recordDO.setBigPackets(MapUtils.getLongValue(item, "big_packets"));
    recordDO.setLargePackets(MapUtils.getLongValue(item, "large_packets"));
    recordDO.setHugePackets(MapUtils.getLongValue(item, "huge_packets"));
    recordDO.setJumboPackets(MapUtils.getLongValue(item, "jumbo_packets"));
    recordDO.setPacketLengthAvg(MapUtils.getLongValue(item, "packet_length_avg"));

    recordDO.setActiveSessions(MapUtils.getLongValue(item, "active_sessions"));
    recordDO.setConcurrentSessions(MapUtils.getLongValue(item, "concurrent_sessions"));
    recordDO.setConcurrentTcpSessions(MapUtils.getLongValue(item, "concurrent_tcp_sessions"));
    recordDO.setConcurrentUdpSessions(MapUtils.getLongValue(item, "concurrent_udp_sessions"));
    recordDO.setConcurrentArpSessions(MapUtils.getLongValue(item, "concurrent_arp_sessions"));
    recordDO.setConcurrentIcmpSessions(MapUtils.getLongValue(item, "concurrent_icmp_sessions"));
    recordDO.setDestroyedSessions(MapUtils.getLongValue(item, "destroyed_sessions"));
    recordDO.setEstablishedTcpSessions(MapUtils.getLongValue(item, "established_tcp_sessions"));
    recordDO.setEstablishedUdpSessions(MapUtils.getLongValue(item, "established_udp_sessions"));
    recordDO.setEstablishedIcmpSessions(MapUtils.getLongValue(item, "established_icmp_sessions"));
    recordDO.setEstablishedOtherSessions(MapUtils.getLongValue(item, "established_other_sessions"));
    recordDO.setEstablishedUpstreamSessions(
        MapUtils.getLongValue(item, "established_upstream_sessions"));
    recordDO.setEstablishedDownstreamSessions(
        MapUtils.getLongValue(item, "established_downstream_sessions"));

    recordDO.setIpv4Frames(MapUtils.getLongValue(item, "ipv4_frames"));
    recordDO.setIpv6Frames(MapUtils.getLongValue(item, "ipv6_frames"));
    recordDO.setArpFrames(MapUtils.getLongValue(item, "arp_frames"));
    recordDO.setIeee8021xFrames(MapUtils.getLongValue(item, "ieee8021x_frames"));
    recordDO.setIpxFrames(MapUtils.getLongValue(item, "ipx_frames"));
    recordDO.setLacpFrames(MapUtils.getLongValue(item, "lacp_frames"));
    recordDO.setMplsFrames(MapUtils.getLongValue(item, "mpls_frames"));
    recordDO.setStpFrames(MapUtils.getLongValue(item, "stp_frames"));
    recordDO.setOtherFrames(MapUtils.getLongValue(item, "other_frames"));

    recordDO.setUnicastBytes(MapUtils.getLongValue(item, "unicast_bytes"));
    recordDO.setBroadcastBytes(MapUtils.getLongValue(item, "broadcast_bytes"));
    recordDO.setMulticastBytes(MapUtils.getLongValue(item, "multicast_bytes"));

    recordDO
        .setServerResponseFastCounts(MapUtils.getLongValue(item, "server_response_fast_counts"));
    recordDO.setServerResponseNormalCounts(
        MapUtils.getLongValue(item, "server_response_normal_counts"));
    recordDO.setServerResponseTimeoutCounts(
        MapUtils.getLongValue(item, "server_response_timeout_counts"));
    recordDO
        .setServerResponseLatencyPeak(MapUtils.getLongValue(item, "server_response_latency_peak"));

    recordDO.setUniqueIpCounts(MapUtils.getLongValue(item, "unique_ip_counts"));

    return recordDO;
  }

}
