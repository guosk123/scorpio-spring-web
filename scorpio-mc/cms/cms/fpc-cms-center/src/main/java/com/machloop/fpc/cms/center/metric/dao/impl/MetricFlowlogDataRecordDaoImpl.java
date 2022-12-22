package com.machloop.fpc.cms.center.metric.dao.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.ZoneOffset;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.*;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao;
import com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.*;

/**
 * @author guosk
 *
 * create at 2021年5月25日, fpc-manager
 */
@Repository
public class MetricFlowlogDataRecordDaoImpl implements MetricFlowlogDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricFlowlogDataRecordDaoImpl.class);

  private static final int SCALE_COUNTS = 4;

  private static final int BATCH_SIZE = 100;

  private static final int IP_LOCALITY_INTRANET = 0;
  private static final int IP_LOCALITY_EXTRANET = 1;

  private static final String DIVIDE_NULL_NAN = "NaN";
  private static final String DIVIDE_NULL_INF = "Infinity";

  private static final String FILTER_SOURCE_NETWORK = "network";
  private static final String FILTER_SOURCE_LOCATION_UP = "location_up";
  private static final String FILTER_SOURCE_LOCATION_DOWN = "location_down";
  private static final String FILTER_SOURCE_PORT_INITIATOR = "port_initiator";
  private static final String FILTER_SOURCE_PORT_RESPONDER = "port_responder";
  private static final String FILTER_SOURCE_MAC_INITIATOR = "mac_initiator";
  private static final String FILTER_SOURCE_MAC_RESPONDER = "mac_responder";
  private static final String FILTER_SOURCE_IP_INITIATOR = "ip_initiator";
  private static final String FILTER_SOURCE_IP_RESPONDER = "ip_responder";
  private static final String FILTER_SOURCE_HOSTGROUP_INITIATOR = "hostgroup_initiator";
  private static final String FILTER_SOURCE_HOSTGROUP_RESPONDER = "hostgroup_responder";
  private static final String FILTER_SOURCE_IP_CONVERSATION = "ip_conversation";

  private static final List<
      String> specialSortProperty = Lists.newArrayList("tcp_client_network_latency_avg",
          "tcp_server_network_latency_avg", "server_response_latency_avg",
          "tcp_client_retransmission_rate", "tcp_server_retransmission_rate");

  private static final List<String> upDownFields = Lists.newArrayList("upstream_bytes",
      "downstream_bytes", "upstream_payload_bytes", "downstream_payload_bytes", "upstream_packets",
      "downstream_packets", "upstream_payload_packets", "downstream_payload_packets");

  private static final String TABLE_FLOW_LOG_RECORD = "d_fpc_flow_log_record";

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricNetworks(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricNetworks(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty) {
    queryVO.setCount(Constants.NUM_1000);
    queryVO.setFilterType(FILTER_SOURCE_NETWORK);

    // 分组字段
    Map<String, String> keys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    keys.put("arrayJoin(network_id)", "networkId");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    aggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    aggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    aggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (onlyAggSortProperty) {
      result = unidirectionalMetricStatisticsOnlySortProperty(queryVO, keys, sortProperty,
          sortDirection);
    } else {
      result = unidirectionalMetricStatistics(queryVO, null, keys, aggs, sortProperty,
          sortDirection);
    }

    return result.stream()
        .filter(item -> queryVO.getNetworkIds().contains(MapUtils.getString(item, "networkId")))
        .collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricNetworkHistograms(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricNetworkHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<String> networkIds) {
    // 分组字段
    Map<String, String> keys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    keys.put("arrayJoin(network_id)", "networkId");

    queryVO.setFilterType(FILTER_SOURCE_NETWORK);
    queryVO.setNetworkIds(networkIds);
    List<Map<String, Object>> result = unidirectionalMetricDateHistogramStatistics(queryVO, null,
        keys, aggsField);

    return result.stream()
        .filter(item -> queryVO.getNetworkIds().contains(MapUtils.getString(item, "networkId")))
        .collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricLocations(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricLocations(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty) {
    // 整理上下行查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO,
        Boolean>> keyAndAggsList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 上行统计
    // 过滤条件： 网络、上行（源IP为内网，目的IP为外网）、时间范围、其他过滤条件；
    // 聚合：网络、业务、目的地区（目的国家、目的省份、目的城市）
    MetricFlowLogQueryVO upQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, upQueryVO);
    upQueryVO.setFilterType(FILTER_SOURCE_LOCATION_UP);

    // 分组字段
    Map<String, String> upKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    upKeys.put("country_id_responder", "countryId");
    upKeys.put("province_id_responder", "provinceId");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> upAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    upAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    upAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    upAggs.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "total_payload_bytes"));
    upAggs.put("upstreamPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));
    upAggs.put("downstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));

    upAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    upAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    upAggs.put("totalPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "total_payload_packets"));
    upAggs.put("upstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));
    upAggs.put("downstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));

    upAggs.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
    upAggs.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
    upAggs.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));

    keyAndAggsList.add(Tuples.of(upKeys, upAggs, upQueryVO, true));

    // 下行统计
    // 过滤条件： 网络、下行（源IP为外网，目的IP为内网）、时间范围、其他过滤条件；
    // 聚合：网络、业务、源地区（源国家、源省份、源城市）
    MetricFlowLogQueryVO downQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, downQueryVO);
    downQueryVO.setFilterType(FILTER_SOURCE_LOCATION_DOWN);

    // 分组字段
    Map<String,
        String> downKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    downKeys.put("country_id_initiator", "countryId");
    downKeys.put("province_id_initiator", "provinceId");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> downAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    downAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    downAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    downAggs.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "total_payload_bytes"));
    downAggs.put("upstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));
    downAggs.put("downstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));

    downAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    downAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    downAggs.put("totalPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "total_payload_packets"));
    downAggs.put("upstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));
    downAggs.put("downstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));

    downAggs.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
    downAggs.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
    downAggs.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));

    keyAndAggsList.add(Tuples.of(downKeys, downAggs, downQueryVO, true));

    List<Map<String, Object>> result = bilateralMetricStatisticsOnlySortProperty(keyAndAggsList,
        sortProperty, sortDirection, true);
    if (!onlyAggSortProperty) {
      // 按排序键聚合,取出结果内的key值，再次过滤，降低数据扫描范围
      final List<Map<String, Object>> metricStatistics = result;
      List<Tuple5<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
          MetricFlowLogQueryVO, Boolean,
          List<Map<String, Object>>>> tempKeyAndAggsList = keyAndAggsList.stream()
              .map(keyAndAggs -> {
                List<Map<String, Object>> keyList = metricStatistics.stream().map(metricData -> {
                  Map<String,
                      Object> key = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
                  keyAndAggs.getT1().entrySet().forEach(entry -> {
                    key.put(entry.getKey(), metricData.get(entry.getValue()));
                  });
                  return key;
                }).collect(Collectors.toList());

                return Tuples.of(keyAndAggs.getT1(), keyAndAggs.getT2(), keyAndAggs.getT3(),
                    keyAndAggs.getT4(), keyList);
              }).collect(Collectors.toList());
      result = bilateralMetricStatistics(tempKeyAndAggsList, sortProperty, sortDirection, true);

      if (StringUtils.equals(queryVO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_PACKET_FILE)) {
        result.forEach(item -> {
          item.put("upstreamBytes", 0);
          item.put("downstreamBytes", 0);
          item.put("upstreamPayloadBytes", 0);
          item.put("downstreamPayloadBytes", 0);
          item.put("upstreamPackets", 0);
          item.put("downstreamPackets", 0);
          item.put("upstreamPayloadPackets", 0);
          item.put("downstreamPayloadPackets", 0);
        });
      }
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricLocationHistograms(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricLocationHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions) {
    // topN的key值过滤
    List<Map<String, Object>> upCombinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> downCombinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    combinationConditions.forEach(combinationCondition -> {
      Map<String, Object> upMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Map<String, Object> downMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Object itemCountryId = combinationCondition.get("country_id");
      upMap.put("country_id_responder", itemCountryId);
      downMap.put("country_id_initiator", itemCountryId);
      Object itemProvinceId = combinationCondition.get("province_id");
      upMap.put("province_id_responder", itemProvinceId);
      downMap.put("province_id_initiator", itemProvinceId);

      upCombinationConditions.add(upMap);
      downCombinationConditions.add(downMap);
    });

    // 整理上下行查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO, List<Map<String, Object>>>> keyAndAggsList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 上行统计
    // 过滤条件： 网络、上行（源IP为内网，目的IP为外网）、时间范围、其他过滤条件；
    // 聚合：网络、业务、目的地区（目的国家、目的省份、目的城市）
    MetricFlowLogQueryVO upQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, upQueryVO);
    upQueryVO.setFilterType(FILTER_SOURCE_LOCATION_UP);

    // 分组字段
    Map<String, String> upKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    upKeys.put("country_id_responder", "countryId");
    upKeys.put("province_id_responder", "provinceId");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> upAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(upAggs, false, aggsField);

    keyAndAggsList.add(Tuples.of(upKeys, upAggs, upQueryVO, upCombinationConditions));

    // 下行统计
    // 过滤条件： 网络、下行（源IP为外网，目的IP为内网）、时间范围、其他过滤条件；
    // 聚合：网络、业务、源地区（源国家、源省份、源城市）
    MetricFlowLogQueryVO downQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, downQueryVO);
    downQueryVO.setFilterType(FILTER_SOURCE_LOCATION_DOWN);

    // 分组字段
    Map<String,
        String> downKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    downKeys.put("country_id_initiator", "countryId");
    downKeys.put("province_id_initiator", "provinceId");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> downAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(downAggs, true, aggsField);

    keyAndAggsList.add(Tuples.of(downKeys, downAggs, downQueryVO, downCombinationConditions));

    List<Map<String, Object>> result = bilateralMetricDateHistogramStatistics(keyAndAggsList,
        aggsField, queryVO.getInterval());

    if (StringUtils.equals(queryVO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_PACKET_FILE)
        && upDownFields.contains(aggsField)) {
      result.forEach(item -> item.put(TextUtils.underLineToCamel(aggsField), 0));
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricApplications(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, reactor.util.function.Tuple2, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplications(MetricFlowLogQueryVO queryVO,
      Tuple2<String, String> termField, String sortProperty, String sortDirection,
      boolean onlyAggSortProperty) {
    // 整理上下行查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO,
        Boolean>> keyAndAggsList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 不区分上下行统计
    // 分组字段
    Map<String, String> keys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    keys.put(termField.getT1(), termField.getT2());

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "total_payload_bytes"));
    aggs.put("upstreamPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("downstreamPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));

    aggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("totalPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "total_payload_packets"));
    aggs.put("upstreamPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("downstreamPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));

    aggs.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
    aggs.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
    aggs.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));
    keyAndAggsList.add(Tuples.of(keys, aggs, queryVO, true));

    // 上行统计
    MetricFlowLogQueryVO upQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, upQueryVO);
    upQueryVO.setIpLocalityInitiator(IP_LOCALITY_INTRANET);
    upQueryVO.setIpLocalityResponder(IP_LOCALITY_EXTRANET);
    // 分组字段
    Map<String, String> upKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    upKeys.put(termField.getT1(), termField.getT2());

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> upAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    upAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    upAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    upAggs.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));
    upAggs.put("upstreamPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));
    upAggs.put("downstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));

    upAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    upAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    upAggs.put("totalPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    upAggs.put("upstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));
    upAggs.put("downstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));

    upAggs.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    upAggs.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    upAggs.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    keyAndAggsList.add(Tuples.of(upKeys, upAggs, upQueryVO, false));

    // 下行统计
    MetricFlowLogQueryVO downQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, downQueryVO);
    downQueryVO.setIpLocalityInitiator(IP_LOCALITY_EXTRANET);
    downQueryVO.setIpLocalityResponder(IP_LOCALITY_INTRANET);
    // 分组字段
    Map<String,
        String> downKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    downKeys.put(termField.getT1(), termField.getT2());

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> downAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    downAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    downAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    downAggs.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));
    downAggs.put("upstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));
    downAggs.put("downstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));

    downAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    downAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    downAggs.put("totalPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    downAggs.put("upstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));
    downAggs.put("downstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));

    downAggs.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    downAggs.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    downAggs.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    keyAndAggsList.add(Tuples.of(downKeys, downAggs, downQueryVO, false));

    List<Map<String, Object>> result = bilateralMetricStatisticsOnlySortProperty(keyAndAggsList,
        sortProperty, sortDirection, false);
    if (!onlyAggSortProperty) {
      // 按排序键聚合,取出结果内的key值，再次过滤，降低数据扫描范围
      final List<Map<String, Object>> metricStatistics = result;
      List<Tuple5<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
          MetricFlowLogQueryVO, Boolean,
          List<Map<String, Object>>>> tempKeyAndAggsList = keyAndAggsList.stream()
              .map(keyAndAggs -> {
                List<Map<String, Object>> keyList = metricStatistics.stream().map(metricData -> {
                  Map<String,
                      Object> key = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
                  keyAndAggs.getT1().entrySet().forEach(entry -> {
                    key.put(entry.getKey(), metricData.get(entry.getValue()));
                  });
                  return key;
                }).collect(Collectors.toList());

                return Tuples.of(keyAndAggs.getT1(), keyAndAggs.getT2(), keyAndAggs.getT3(),
                    keyAndAggs.getT4(), keyList);
              }).collect(Collectors.toList());
      result = bilateralMetricStatistics(tempKeyAndAggsList, sortProperty, sortDirection, false);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricApplicationHistograms(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, reactor.util.function.Tuple2, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplicationHistograms(MetricFlowLogQueryVO queryVO,
      Tuple2<String, String> termField, String aggsField, List<String> termValues) {
    // topN的key值过滤
    List<Map<String, Object>> keyConditions = termValues.stream().map(termValue -> {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(1);
      map.put(termField.getT1(), termValue);
      return map;
    }).collect(Collectors.toList());

    // 整理上下行查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO, List<Map<String, Object>>>> keyAndAggsList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 不区分上下行统计
    // 分组字段
    Map<String, String> keys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    keys.put(termField.getT1(), termField.getT2());

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(aggs, false, aggsField);
    if (upDownFields.contains(aggsField)) {
      aggs = aggs.entrySet().stream().collect(
          Collectors.toMap(Entry::getKey, entry -> Tuples.of(entry.getValue().getT1(), "0")));
    }

    keyAndAggsList.add(Tuples.of(keys, aggs, queryVO, keyConditions));

    // 上行统计
    MetricFlowLogQueryVO upQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, upQueryVO);
    upQueryVO.setIpLocalityInitiator(IP_LOCALITY_INTRANET);
    upQueryVO.setIpLocalityResponder(IP_LOCALITY_EXTRANET);

    // 分组字段
    Map<String, String> upKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    upKeys.put(termField.getT1(), termField.getT2());

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> upAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(upAggs, false, aggsField);
    if (!upDownFields.contains(aggsField)) {
      upAggs = upAggs.entrySet().stream().collect(
          Collectors.toMap(Entry::getKey, entry -> Tuples.of(entry.getValue().getT1(), "0")));
    }

    keyAndAggsList.add(Tuples.of(upKeys, upAggs, upQueryVO, keyConditions));

    // 下行统计
    MetricFlowLogQueryVO downQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, downQueryVO);
    downQueryVO.setIpLocalityInitiator(IP_LOCALITY_EXTRANET);
    downQueryVO.setIpLocalityResponder(IP_LOCALITY_INTRANET);

    // 分组字段
    Map<String,
        String> downKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    downKeys.put(termField.getT1(), termField.getT2());

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> downAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(downAggs, true, aggsField);
    if (!upDownFields.contains(aggsField)) {
      downAggs = downAggs.entrySet().stream().collect(
          Collectors.toMap(Entry::getKey, entry -> Tuples.of(entry.getValue().getT1(), "0")));
    }

    keyAndAggsList.add(Tuples.of(downKeys, downAggs, downQueryVO, keyConditions));

    return bilateralMetricDateHistogramStatistics(keyAndAggsList, aggsField, queryVO.getInterval());
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricL7Protocols(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricL7Protocols(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty) {
    // 整理上下行查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO,
        Boolean>> keyAndAggsList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 不区分上下行统计
    // 分组字段
    Map<String, String> keys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    keys.put("l7_protocol_id", "l7ProtocolId");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "total_payload_bytes"));
    aggs.put("upstreamPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("downstreamPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));

    aggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("totalPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "total_payload_packets"));
    aggs.put("upstreamPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    aggs.put("downstreamPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));

    aggs.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
    aggs.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
    aggs.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));
    keyAndAggsList.add(Tuples.of(keys, aggs, queryVO, true));

    // 上行统计
    MetricFlowLogQueryVO upQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, upQueryVO);
    upQueryVO.setIpLocalityInitiator(IP_LOCALITY_INTRANET);
    upQueryVO.setIpLocalityResponder(IP_LOCALITY_EXTRANET);
    // 分组字段
    Map<String, String> upKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    upKeys.put("l7_protocol_id", "l7ProtocolId");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> upAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    upAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    upAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    upAggs.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));
    upAggs.put("upstreamPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));
    upAggs.put("downstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));

    upAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    upAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    upAggs.put("totalPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    upAggs.put("upstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));
    upAggs.put("downstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));

    upAggs.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    upAggs.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    upAggs.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    keyAndAggsList.add(Tuples.of(upKeys, upAggs, upQueryVO, false));

    // 下行统计
    MetricFlowLogQueryVO downQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, downQueryVO);
    downQueryVO.setIpLocalityInitiator(IP_LOCALITY_EXTRANET);
    downQueryVO.setIpLocalityResponder(IP_LOCALITY_INTRANET);
    // 分组字段
    Map<String,
        String> downKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    downKeys.put("l7_protocol_id", "l7ProtocolId");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> downAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    downAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    downAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    downAggs.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "0"));
    downAggs.put("upstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));
    downAggs.put("downstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));

    downAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    downAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    downAggs.put("totalPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    downAggs.put("upstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));
    downAggs.put("downstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));

    downAggs.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    downAggs.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    downAggs.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "0"));
    keyAndAggsList.add(Tuples.of(downKeys, downAggs, downQueryVO, false));

    List<Map<String, Object>> result = bilateralMetricStatisticsOnlySortProperty(keyAndAggsList,
        sortProperty, sortDirection, false);
    if (!onlyAggSortProperty) {
      // 按排序键聚合,取出结果内的key值，再次过滤，降低数据扫描范围
      final List<Map<String, Object>> metricStatistics = result;
      List<Tuple5<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
          MetricFlowLogQueryVO, Boolean,
          List<Map<String, Object>>>> tempKeyAndAggsList = keyAndAggsList.stream()
              .map(keyAndAggs -> {
                List<Map<String, Object>> keyList = metricStatistics.stream().map(metricData -> {
                  Map<String,
                      Object> key = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
                  keyAndAggs.getT1().entrySet().forEach(entry -> {
                    key.put(entry.getKey(), metricData.get(entry.getValue()));
                  });
                  return key;
                }).collect(Collectors.toList());

                return Tuples.of(keyAndAggs.getT1(), keyAndAggs.getT2(), keyAndAggs.getT3(),
                    keyAndAggs.getT4(), keyList);
              }).collect(Collectors.toList());
      result = bilateralMetricStatistics(tempKeyAndAggsList, sortProperty, sortDirection, false);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricL7ProtocolHistograms(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricL7ProtocolHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<String> l7ProtocolIds) {
    // topN的key值过滤
    List<Map<String, Object>> keyConditions = l7ProtocolIds.stream().map(l7ProtocolId -> {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(1);
      map.put("l7_protocol_id", l7ProtocolId);
      return map;
    }).collect(Collectors.toList());

    // 整理上下行查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO, List<Map<String, Object>>>> keyAndAggsList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 不区分上下行统计
    // 分组字段
    Map<String, String> keys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    keys.put("l7_protocol_id", "l7ProtocolId");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(aggs, false, aggsField);
    if (upDownFields.contains(aggsField)) {
      aggs = aggs.entrySet().stream().collect(
          Collectors.toMap(Entry::getKey, entry -> Tuples.of(entry.getValue().getT1(), "0")));
    }

    keyAndAggsList.add(Tuples.of(keys, aggs, queryVO, keyConditions));

    // 上行统计
    MetricFlowLogQueryVO upQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, upQueryVO);
    upQueryVO.setIpLocalityInitiator(IP_LOCALITY_INTRANET);
    upQueryVO.setIpLocalityResponder(IP_LOCALITY_EXTRANET);

    // 分组字段
    Map<String, String> upKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    upKeys.put("l7_protocol_id", "l7ProtocolId");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> upAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(upAggs, false, aggsField);
    if (!upDownFields.contains(aggsField)) {
      upAggs = upAggs.entrySet().stream().collect(
          Collectors.toMap(Entry::getKey, entry -> Tuples.of(entry.getValue().getT1(), "0")));
    }

    keyAndAggsList.add(Tuples.of(upKeys, upAggs, upQueryVO, keyConditions));

    // 下行统计
    MetricFlowLogQueryVO downQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, downQueryVO);
    downQueryVO.setIpLocalityInitiator(IP_LOCALITY_EXTRANET);
    downQueryVO.setIpLocalityResponder(IP_LOCALITY_INTRANET);

    // 分组字段
    Map<String,
        String> downKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    downKeys.put("l7_protocol_id", "l7ProtocolId");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> downAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(downAggs, true, aggsField);
    if (!upDownFields.contains(aggsField)) {
      downAggs = downAggs.entrySet().stream().collect(
          Collectors.toMap(Entry::getKey, entry -> Tuples.of(entry.getValue().getT1(), "0")));
    }

    keyAndAggsList.add(Tuples.of(downKeys, downAggs, downQueryVO, keyConditions));

    return bilateralMetricDateHistogramStatistics(keyAndAggsList, aggsField, queryVO.getInterval());
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricPorts(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricPorts(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty) {
    // 整理源目的查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO,
        Boolean>> keyAndAggsList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 源端口统计（分组：网络、业务、源端口）
    // 过滤类型
    MetricFlowLogQueryVO initiatorQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, initiatorQueryVO);
    initiatorQueryVO.setFilterType(FILTER_SOURCE_PORT_INITIATOR);

    // 分组字段
    Map<String,
        String> initiatorKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorKeys.put("port_initiator", "port");
    initiatorKeys.put("ip_protocol", "ipProtocol");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> initiatorAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    initiatorAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    initiatorAggs.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "total_payload_bytes"));
    initiatorAggs.put("upstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));
    initiatorAggs.put("downstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));

    initiatorAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    initiatorAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    initiatorAggs.put("totalPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "total_payload_packets"));
    initiatorAggs.put("upstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));
    initiatorAggs.put("downstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));

    initiatorAggs.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
    initiatorAggs.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
    initiatorAggs.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));

    keyAndAggsList.add(Tuples.of(initiatorKeys, initiatorAggs, initiatorQueryVO, true));

    // 目的端口统计（分组：网络、业务、目的端口）
    // 过滤类型
    MetricFlowLogQueryVO responderQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, responderQueryVO);
    responderQueryVO.setFilterType(FILTER_SOURCE_PORT_RESPONDER);

    // 分组字段
    Map<String,
        String> responderKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderKeys.put("port_responder", "port");
    responderKeys.put("ip_protocol", "ipProtocol");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> responderAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    responderAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    responderAggs.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "total_payload_bytes"));
    responderAggs.put("upstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));
    responderAggs.put("downstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));

    responderAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    responderAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    responderAggs.put("totalPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "total_payload_packets"));
    responderAggs.put("upstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));
    responderAggs.put("downstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));

    responderAggs.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
    responderAggs.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
    responderAggs.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));

    keyAndAggsList.add(Tuples.of(responderKeys, responderAggs, responderQueryVO, true));

    List<Map<String, Object>> result = bilateralMetricStatisticsOnlySortProperty(keyAndAggsList,
        sortProperty, sortDirection, true);
    if (!onlyAggSortProperty) {
      // 按排序键聚合,取出结果内的key值，再次过滤，降低数据扫描范围
      final List<Map<String, Object>> metricStatistics = result;
      List<Tuple5<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
          MetricFlowLogQueryVO, Boolean,
          List<Map<String, Object>>>> tempKeyAndAggsList = keyAndAggsList.stream()
              .map(keyAndAggs -> {
                List<Map<String, Object>> keyList = metricStatistics.stream().map(metricData -> {
                  Map<String,
                      Object> key = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
                  keyAndAggs.getT1().entrySet().forEach(entry -> {
                    key.put(entry.getKey(), metricData.get(entry.getValue()));
                  });
                  return key;
                }).collect(Collectors.toList());

                return Tuples.of(keyAndAggs.getT1(), keyAndAggs.getT2(), keyAndAggs.getT3(),
                    keyAndAggs.getT4(), keyList);
              }).collect(Collectors.toList());
      result = bilateralMetricStatistics(tempKeyAndAggsList, sortProperty, sortDirection, true);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricPortHistograms(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricPortHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<String> ports) {
    // topN的key值过滤
    List<Map<String, Object>> initiatorConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> responderConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    ports.forEach(port -> {
      Map<String,
          Object> initiatorMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      initiatorMap.put("port_initiator", port);
      initiatorConditions.add(initiatorMap);

      Map<String,
          Object> responderMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      responderMap.put("port_responder", port);
      responderConditions.add(responderMap);
    });

    // 整理源目的查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO, List<Map<String, Object>>>> keyAndAggsList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 源端口统计（分组：网络、业务、源端口）
    // 过滤类型
    MetricFlowLogQueryVO initiatorQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, initiatorQueryVO);
    initiatorQueryVO.setFilterType(FILTER_SOURCE_PORT_INITIATOR);

    // 分组字段
    Map<String,
        String> initiatorKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorKeys.put("port_initiator", "port");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> initiatorAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(initiatorAggs, false, aggsField);

    keyAndAggsList
        .add(Tuples.of(initiatorKeys, initiatorAggs, initiatorQueryVO, initiatorConditions));

    // 目的端口统计（分组：网络、业务、目的端口）
    // 过滤类型
    MetricFlowLogQueryVO responderQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, responderQueryVO);
    responderQueryVO.setFilterType(FILTER_SOURCE_PORT_RESPONDER);

    // 分组字段
    Map<String,
        String> responderKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderKeys.put("port_responder", "port");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> responderAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(responderAggs, true, aggsField);

    keyAndAggsList
        .add(Tuples.of(responderKeys, responderAggs, responderQueryVO, responderConditions));

    return bilateralMetricDateHistogramStatistics(keyAndAggsList, aggsField, queryVO.getInterval());
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricHostGroups(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricHostGroups(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty) {
    // 整理源目的查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO,
        Boolean>> keyAndAggsList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 源地址组统计（分组：网络、业务、源地址组）
    // 过滤类型
    MetricFlowLogQueryVO initiatorQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, initiatorQueryVO);
    initiatorQueryVO.setFilterType(FILTER_SOURCE_HOSTGROUP_INITIATOR);

    // 分组字段
    Map<String,
        String> initiatorKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorKeys.put("hostgroup_id_initiator", "hostgroupId");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> initiatorAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    initiatorAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    initiatorAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    initiatorAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));

    keyAndAggsList.add(Tuples.of(initiatorKeys, initiatorAggs, initiatorQueryVO, true));

    // 目的地址组统计（分组：网络、业务、目的地址组）
    // 过滤类型
    MetricFlowLogQueryVO responderQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, responderQueryVO);
    responderQueryVO.setFilterType(FILTER_SOURCE_HOSTGROUP_RESPONDER);

    // 分组字段
    Map<String,
        String> responderKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderKeys.put("hostgroup_id_responder", "hostgroupId");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> responderAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    responderAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    responderAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    responderAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));

    keyAndAggsList.add(Tuples.of(responderKeys, responderAggs, responderQueryVO, true));

    List<Map<String, Object>> result = bilateralMetricStatisticsOnlySortProperty(keyAndAggsList,
        sortProperty, sortDirection, true);
    if (!onlyAggSortProperty) {
      // 按排序键聚合,取出结果内的key值，再次过滤，降低数据扫描范围
      final List<Map<String, Object>> metricStatistics = result;
      List<Tuple5<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
          MetricFlowLogQueryVO, Boolean,
          List<Map<String, Object>>>> tempKeyAndAggsList = keyAndAggsList.stream()
              .map(keyAndAggs -> {
                List<Map<String, Object>> keyList = metricStatistics.stream().map(metricData -> {
                  Map<String,
                      Object> key = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
                  keyAndAggs.getT1().entrySet().forEach(entry -> {
                    key.put(entry.getKey(), metricData.get(entry.getValue()));
                  });
                  return key;
                }).collect(Collectors.toList());

                return Tuples.of(keyAndAggs.getT1(), keyAndAggs.getT2(), keyAndAggs.getT3(),
                    keyAndAggs.getT4(), keyList);
              }).collect(Collectors.toList());
      result = bilateralMetricStatistics(tempKeyAndAggsList, sortProperty, sortDirection, true);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricHostGroupHistograms(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricHostGroupHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<String> hostgroupIds) {
    // topN的key值过滤
    List<Map<String, Object>> initiatorConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> responderConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    hostgroupIds.forEach(hostgroupId -> {
      Map<String,
          Object> initiatorMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      initiatorMap.put("hostgroup_id_initiator", hostgroupId);
      initiatorConditions.add(initiatorMap);

      Map<String,
          Object> responderMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      responderMap.put("hostgroup_id_responder", hostgroupId);
      responderConditions.add(responderMap);
    });

    // 整理源目的查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO, List<Map<String, Object>>>> keyAndAggsList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 源地址组统计（分组：网络、业务、源地址组）
    // 过滤类型
    MetricFlowLogQueryVO initiatorQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, initiatorQueryVO);
    initiatorQueryVO.setFilterType(FILTER_SOURCE_HOSTGROUP_INITIATOR);

    // 分组字段
    Map<String,
        String> initiatorKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorKeys.put("hostgroup_id_initiator", "hostgroupId");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> initiatorAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(initiatorAggs, false, aggsField);

    keyAndAggsList
        .add(Tuples.of(initiatorKeys, initiatorAggs, initiatorQueryVO, initiatorConditions));

    // 目的地址组统计（分组：网络、业务、目的地址组）
    // 过滤类型
    MetricFlowLogQueryVO responderQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, responderQueryVO);
    responderQueryVO.setFilterType(FILTER_SOURCE_HOSTGROUP_RESPONDER);

    // 分组字段
    Map<String,
        String> responderKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderKeys.put("hostgroup_id_responder", "hostgroupId");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> responderAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(responderAggs, true, aggsField);

    keyAndAggsList
        .add(Tuples.of(responderKeys, responderAggs, responderQueryVO, responderConditions));

    return bilateralMetricDateHistogramStatistics(keyAndAggsList, aggsField, queryVO.getInterval());
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricL2Devices(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricL2Devices(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty) {
    // 整理源目的查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO,
        Boolean>> keyAndAggsList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 源二层主机统计（分组：网络、业务、源二层主机）
    // 过滤类型
    MetricFlowLogQueryVO initiatorQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, initiatorQueryVO);
    initiatorQueryVO.setFilterType(FILTER_SOURCE_MAC_INITIATOR);

    // 分组字段
    Map<String,
        String> initiatorKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorKeys.put("ethernet_initiator", "macAddress");
    initiatorKeys.put("ethernet_type", "ethernetType");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> initiatorAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    initiatorAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    initiatorAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    initiatorAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));

    keyAndAggsList.add(Tuples.of(initiatorKeys, initiatorAggs, initiatorQueryVO, true));

    // 目的二层主机统计（分组：网络、业务、目的二层主机）
    // 过滤类型
    MetricFlowLogQueryVO responderQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, responderQueryVO);
    responderQueryVO.setFilterType(FILTER_SOURCE_MAC_RESPONDER);

    // 分组字段
    Map<String,
        String> responderKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderKeys.put("ethernet_responder", "macAddress");
    responderKeys.put("ethernet_type", "ethernetType");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> responderAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    responderAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    responderAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    responderAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));

    keyAndAggsList.add(Tuples.of(responderKeys, responderAggs, responderQueryVO, true));

    List<Map<String, Object>> result = bilateralMetricStatisticsOnlySortProperty(keyAndAggsList,
        sortProperty, sortDirection, true);
    if (!onlyAggSortProperty) {
      // 按排序键聚合,取出结果内的key值，再次过滤，降低数据扫描范围
      final List<Map<String, Object>> metricStatistics = result;
      List<Tuple5<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
          MetricFlowLogQueryVO, Boolean,
          List<Map<String, Object>>>> tempKeyAndAggsList = keyAndAggsList.stream()
              .map(keyAndAggs -> {
                List<Map<String, Object>> keyList = metricStatistics.stream().map(metricData -> {
                  Map<String,
                      Object> key = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
                  keyAndAggs.getT1().entrySet().forEach(entry -> {
                    key.put(entry.getKey(), metricData.get(entry.getValue()));
                  });
                  return key;
                }).collect(Collectors.toList());

                return Tuples.of(keyAndAggs.getT1(), keyAndAggs.getT2(), keyAndAggs.getT3(),
                    keyAndAggs.getT4(), keyList);
              }).collect(Collectors.toList());
      result = bilateralMetricStatistics(tempKeyAndAggsList, sortProperty, sortDirection, true);
    }
    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricL2DeviceHistograms(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricL2DeviceHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<String> macAddress) {
    // topN的key值过滤
    List<Map<String, Object>> initiatorConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> responderConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    macAddress.forEach(ethernet -> {
      Map<String,
          Object> initiatorMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      initiatorMap.put("ethernet_initiator", ethernet);
      initiatorConditions.add(initiatorMap);

      Map<String,
          Object> responderMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      responderMap.put("ethernet_responder", ethernet);
      responderConditions.add(responderMap);
    });

    // 整理源目的查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO, List<Map<String, Object>>>> keyAndAggsList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 源二层主机统计（分组：网络、业务、源二层主机）
    // 过滤类型
    MetricFlowLogQueryVO initiatorQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, initiatorQueryVO);
    initiatorQueryVO.setFilterType(FILTER_SOURCE_MAC_INITIATOR);

    // 分组字段
    Map<String,
        String> initiatorKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorKeys.put("ethernet_initiator", "macAddress");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> initiatorAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(initiatorAggs, false, aggsField);

    keyAndAggsList
        .add(Tuples.of(initiatorKeys, initiatorAggs, initiatorQueryVO, initiatorConditions));

    // 目的二层主机统计（分组：网络、业务、目的二层主机）
    // 过滤类型
    MetricFlowLogQueryVO responderQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, responderQueryVO);
    responderQueryVO.setFilterType(FILTER_SOURCE_MAC_RESPONDER);

    // 分组字段
    Map<String,
        String> responderKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderKeys.put("ethernet_responder", "macAddress");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> responderAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(responderAggs, true, aggsField);

    keyAndAggsList
        .add(Tuples.of(responderKeys, responderAggs, responderQueryVO, responderConditions));

    return bilateralMetricDateHistogramStatistics(keyAndAggsList, aggsField, queryVO.getInterval());
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricL3Devices(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricL3Devices(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty) {
    // 整理源目的查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO,
        Boolean>> keyAndAggsList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 源三层主机统计（分组：网络、业务、源三层主机）
    // 过滤类型
    MetricFlowLogQueryVO initiatorQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, initiatorQueryVO);
    initiatorQueryVO.setFilterType(FILTER_SOURCE_IP_INITIATOR);

    // 分组字段
    Map<String,
        String> initiatorKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorKeys.put("ipv4_initiator", "ipv4Address");
    initiatorKeys.put("ipv6_initiator", "ipv6Address");
    initiatorKeys.put("ip_locality_initiator", "ipLocality");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> initiatorAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    initiatorAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    initiatorAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    initiatorAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    initiatorAggs.put("activeEstablishedSessions",
        Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));
    initiatorAggs.put("passiveEstablishedSessions", Tuples.of(AggsFunctionEnum.SUM, "0"));

    keyAndAggsList.add(Tuples.of(initiatorKeys, initiatorAggs, initiatorQueryVO, true));

    // 目的三层主机统计（分组：网络、业务、目的三层主机）
    // 过滤类型
    MetricFlowLogQueryVO responderQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, responderQueryVO);
    responderQueryVO.setFilterType(FILTER_SOURCE_IP_RESPONDER);

    // 分组字段
    Map<String,
        String> responderKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderKeys.put("ipv4_responder", "ipv4Address");
    responderKeys.put("ipv6_responder", "ipv6Address");
    responderKeys.put("ip_locality_responder", "ipLocality");

    // 独有聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> responderAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderAggs.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    responderAggs.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    responderAggs.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    responderAggs.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    responderAggs.put("activeEstablishedSessions", Tuples.of(AggsFunctionEnum.SUM, "0"));
    responderAggs.put("passiveEstablishedSessions",
        Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));

    keyAndAggsList.add(Tuples.of(responderKeys, responderAggs, responderQueryVO, true));

    List<Map<String, Object>> result = bilateralMetricStatisticsOnlySortProperty(keyAndAggsList,
        sortProperty, sortDirection, true);
    if (!onlyAggSortProperty) {
      // 按排序键聚合,取出结果内的key值，再次过滤，降低数据扫描范围
      List<Map<String, Object>> tempResult = Lists.newArrayListWithCapacity(result.size());
      int now = 0;
      List<Map<String, Object>> metricList = Lists.newArrayListWithCapacity(BATCH_SIZE);
      while (now < result.size()) {
        metricList.add(result.get(now));

        now++;
        if (now % BATCH_SIZE == 0) {
          List<Tuple5<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
              MetricFlowLogQueryVO, Boolean, List<Map<String, Object>>>> tempKeyAndAggsList = Lists
                  .newArrayListWithCapacity(keyAndAggsList.size());
          tempKeyAndAggsList.add(Tuples.of(keyAndAggsList.get(0).getT1(),
              keyAndAggsList.get(0).getT2(), keyAndAggsList.get(0).getT3(),
              keyAndAggsList.get(0).getT4(), getInitiatorKeyAndAggsList(metricList)));
          tempKeyAndAggsList.add(Tuples.of(keyAndAggsList.get(1).getT1(),
              keyAndAggsList.get(1).getT2(), keyAndAggsList.get(1).getT3(),
              keyAndAggsList.get(1).getT4(), getResponderKeyAndAggsList(metricList)));

          tempResult.addAll(
              bilateralMetricStatistics(tempKeyAndAggsList, sortProperty, sortDirection, true));

          metricList = Lists.newArrayListWithCapacity(BATCH_SIZE);
        }
      }

      if (now % BATCH_SIZE != 0) {
        List<Tuple5<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
            MetricFlowLogQueryVO, Boolean, List<Map<String, Object>>>> tempKeyAndAggsList = Lists
                .newArrayListWithCapacity(keyAndAggsList.size());
        tempKeyAndAggsList.add(Tuples.of(keyAndAggsList.get(0).getT1(),
            keyAndAggsList.get(0).getT2(), keyAndAggsList.get(0).getT3(),
            keyAndAggsList.get(0).getT4(), getInitiatorKeyAndAggsList(metricList)));
        tempKeyAndAggsList.add(Tuples.of(keyAndAggsList.get(1).getT1(),
            keyAndAggsList.get(1).getT2(), keyAndAggsList.get(1).getT3(),
            keyAndAggsList.get(1).getT4(), getResponderKeyAndAggsList(metricList)));

        tempResult.addAll(
            bilateralMetricStatistics(tempKeyAndAggsList, sortProperty, sortDirection, true));
      }

      result = tempResult;
    }

    result.forEach(metricL3Device -> {
      metricL3Device.put("macAddress", queryVO.getMacAddress());
    });

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricL3DeviceHistograms(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricL3DeviceHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions) {
    // topN的key值过滤
    List<Map<String, Object>> initiatorConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> responderConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    combinationConditions.forEach(combinationCondition -> {
      Map<String,
          Object> initiatorMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Map<String,
          Object> responderMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      String ipAddress = MapUtils.getString(combinationCondition, "ip_address");
      if (NetworkUtils.isInetAddress(ipAddress, IpVersion.V4)) {
        initiatorMap.put("ipv4_initiator", ipAddress);
        responderMap.put("ipv4_responder", ipAddress);
      } else {
        initiatorMap.put("ipv6_initiator", ipAddress);
        responderMap.put("ipv6_responder", ipAddress);
      }
      initiatorMap.put("ip_locality_initiator", combinationCondition.get("ip_locality"));
      responderMap.put("ip_locality_responder", combinationCondition.get("ip_locality"));

      initiatorConditions.add(initiatorMap);
      responderConditions.add(responderMap);
    });

    // 整理源目的查询所需数据
    List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO, List<Map<String, Object>>>> keyAndAggsList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 源三层主机统计（分组：网络、业务、源三层主机）
    // 过滤类型
    MetricFlowLogQueryVO initiatorQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, initiatorQueryVO);
    initiatorQueryVO.setFilterType(FILTER_SOURCE_IP_INITIATOR);

    // 分组字段
    Map<String,
        String> initiatorKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    initiatorKeys.put("ipv4_initiator", "ipv4Address");
    initiatorKeys.put("ipv6_initiator", "ipv6Address");
    initiatorKeys.put("ip_locality_initiator", "ipLocality");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> initiatorAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(initiatorAggs, false, aggsField);

    keyAndAggsList
        .add(Tuples.of(initiatorKeys, initiatorAggs, initiatorQueryVO, initiatorConditions));

    // 目的三层主机统计（分组：网络、业务、目的三层主机）
    // 过滤类型
    MetricFlowLogQueryVO responderQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, responderQueryVO);
    responderQueryVO.setFilterType(FILTER_SOURCE_IP_RESPONDER);

    // 分组字段
    Map<String,
        String> responderKeys = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    responderKeys.put("ipv4_responder", "ipv4Address");
    responderKeys.put("ipv6_responder", "ipv6Address");
    responderKeys.put("ip_locality_responder", "ipLocality");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> responderAggs = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(responderAggs, true, aggsField);

    keyAndAggsList
        .add(Tuples.of(responderKeys, responderAggs, responderQueryVO, responderConditions));

    return bilateralMetricDateHistogramStatistics(keyAndAggsList, aggsField, queryVO.getInterval());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#queryMetricIpConversations(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversations(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty) {
    // 只聚合排序键
    List<Map<String, Object>> result = metricIpConversation(queryVO, null, sortProperty,
        sortDirection, true);

    if (!onlyAggSortProperty) {
      // 按排序键聚合,取出结果内的key值，再次过滤，降低数据扫描范围
      List<Map<String, Object>> tempResult = Lists.newArrayListWithCapacity(result.size());
      int now = 0;
      List<Map<String, Object>> metricList = Lists.newArrayListWithCapacity(BATCH_SIZE);
      while (now < result.size()) {
        metricList.add(result.get(now));

        now++;
        if (now % BATCH_SIZE == 0) {
          List<Map<String, Object>> keyList = Lists
              .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
          metricList.forEach(metricData -> {
            Map<String,
                Object> keyMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            Map<String,
                Object> keyMap2 = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            String ipAAddress = MapUtils.getString(metricData, "ipAAddress");
            String ipBAddress = MapUtils.getString(metricData, "ipBAddress");

            if (StringUtils.startsWithIgnoreCase(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX)
                && StringUtils.startsWithIgnoreCase(ipBAddress,
                    CenterConstants.IPV4_TO_IPV6_PREFIX)) {
              // ipA->ipB
              keyMap.put("ipv4_initiator",
                  StringUtils.substringAfterLast(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
              keyMap.put("ipv4_responder",
                  StringUtils.substringAfterLast(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
              keyList.add(keyMap);

              // ipB->ipA
              keyMap2.put("ipv4_initiator",
                  StringUtils.substringAfterLast(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
              keyMap2.put("ipv4_responder",
                  StringUtils.substringAfterLast(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
              keyList.add(keyMap2);
            } else {
              // ipA->ipB
              keyMap.put("ipv6_initiator", ipAAddress);
              keyMap.put("ipv6_responder", ipBAddress);
              keyList.add(keyMap);

              // ipB->ipA
              keyMap2.put("ipv6_initiator", ipBAddress);
              keyMap2.put("ipv6_responder", ipAAddress);
              keyList.add(keyMap2);
            }
          });

          tempResult
              .addAll(metricIpConversation(queryVO, keyList, sortProperty, sortDirection, false));

          metricList = Lists.newArrayListWithCapacity(BATCH_SIZE);
        }
      }

      if (now % BATCH_SIZE != 0) {
        List<Map<String, Object>> keyList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        metricList.forEach(metricData -> {
          Map<String, Object> keyMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          Map<String, Object> keyMap2 = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          String ipAAddress = MapUtils.getString(metricData, "ipAAddress");
          String ipBAddress = MapUtils.getString(metricData, "ipBAddress");

          if (StringUtils.startsWithIgnoreCase(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX)
              && StringUtils.startsWithIgnoreCase(ipBAddress,
                  CenterConstants.IPV4_TO_IPV6_PREFIX)) {
            // ipA->ipB
            keyMap.put("ipv4_initiator",
                StringUtils.substringAfterLast(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
            keyMap.put("ipv4_responder",
                StringUtils.substringAfterLast(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
            keyList.add(keyMap);

            // ipB->ipA
            keyMap2.put("ipv4_initiator",
                StringUtils.substringAfterLast(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
            keyMap2.put("ipv4_responder",
                StringUtils.substringAfterLast(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
            keyList.add(keyMap2);
          } else {
            // ipA->ipB
            keyMap.put("ipv6_initiator", ipAAddress);
            keyMap.put("ipv6_responder", ipBAddress);
            keyList.add(keyMap);

            // ipB->ipA
            keyMap2.put("ipv6_initiator", ipBAddress);
            keyMap2.put("ipv6_responder", ipAAddress);
            keyList.add(keyMap2);
          }
        });

        tempResult
            .addAll(metricIpConversation(queryVO, keyList, sortProperty, sortDirection, false));
      }

      result = tempResult;
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricFlowlogDataRecordDao#queryMetricIpConversationHistograms(com.machloop.fpc.manager.metric.vo.MetricFlowLogQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversationHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());
    enrichWhereSql(queryVO, null, whereSql, params, "");
    // 添加附加条件
    List<Map<String, Object>> additionalConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    combinationConditions.forEach(combinationCondition -> {
      Map<String, Object> keyMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Map<String, Object> keyMap2 = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      String ipAAddress = MapUtils.getString(combinationCondition, "ipAAddress");
      String ipBAddress = MapUtils.getString(combinationCondition, "ipBAddress");

      if ((NetworkUtils.isInetAddress(ipAAddress, IpVersion.V4)
          || StringUtils.startsWithIgnoreCase(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX))
          && (NetworkUtils.isInetAddress(ipBAddress, IpVersion.V4) || StringUtils
              .startsWithIgnoreCase(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX))) {
        String ipA = StringUtils.startsWithIgnoreCase(ipAAddress,
            CenterConstants.IPV4_TO_IPV6_PREFIX)
                ? StringUtils.substringAfterLast(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX)
                : ipAAddress;
        String ipB = StringUtils.startsWithIgnoreCase(ipBAddress,
            CenterConstants.IPV4_TO_IPV6_PREFIX)
                ? StringUtils.substringAfterLast(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX)
                : ipBAddress;

        // ipA->ipB
        keyMap.put("ipv4_initiator", ipA);
        keyMap.put("ipv4_responder", ipB);
        additionalConditions.add(keyMap);

        // ipB->ipA
        keyMap2.put("ipv4_initiator", ipB);
        keyMap2.put("ipv4_responder", ipA);
        additionalConditions.add(keyMap2);
      } else {
        // ipA->ipB
        keyMap.put("ipv6_initiator", ipAAddress);
        keyMap.put("ipv6_responder", ipBAddress);
        additionalConditions.add(keyMap);

        // ipB->ipA
        keyMap2.put("ipv6_initiator", ipBAddress);
        keyMap2.put("ipv6_responder", ipAAddress);
        additionalConditions.add(keyMap2);
      }
    });
    enrichKeySql(additionalConditions, whereSql, params);

    // 普通聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(aggFields, false, aggsField);

    // 子查询
    StringBuilder innerSql = new StringBuilder();
    innerSql.append("(select report_time, ");
    innerSql.append(
        " if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, IPv4ToIPv6(ipv4_initiator), IPv4ToIPv6(ipv4_responder)), if(ipv6_initiator < ipv6_responder, ipv6_initiator, ipv6_responder)) as smallIp, ");
    innerSql.append(
        " if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, IPv4ToIPv6(ipv4_responder), IPv4ToIPv6(ipv4_initiator)), if(ipv6_initiator < ipv6_responder, ipv6_responder, ipv6_initiator)) as bigIp");
    if (aggFields.containsKey("upstreamBytes")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, downstream_bytes, upstream_bytes), if(ipv6_initiator < ipv6_responder, downstream_bytes, upstream_bytes)) as upstreamBytes");
    }
    if (aggFields.containsKey("downstreamBytes")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, upstream_bytes, downstream_bytes), if(ipv6_initiator < ipv6_responder, upstream_bytes, downstream_bytes)) as downstreamBytes");
    }
    if (aggFields.containsKey("upstreamPackets")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, downstream_packets, upstream_packets), if(ipv6_initiator < ipv6_responder, downstream_packets, upstream_packets)) as upstreamPackets");
    }
    if (aggFields.containsKey("downstreamPackets")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, upstream_packets, downstream_packets), if(ipv6_initiator < ipv6_responder, upstream_packets, downstream_packets)) as downstreamPackets");
    }
    if (aggFields.containsKey("activeEstablishedSessions")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, established_sessions, 0), if(ipv6_initiator < ipv6_responder, established_sessions, 0)) as activeEstablishedSessions");
    }
    if (aggFields.containsKey("passiveEstablishedSessions")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, 0, established_sessions), if(ipv6_initiator < ipv6_responder, 0, established_sessions)) as passiveEstablishedSessions");
    }
    aggFields.entrySet().stream()
        .filter(entry -> !StringUtils.equalsAny(entry.getKey(), "upstreamBytes", "downstreamBytes",
            "upstreamPackets", "downstreamPackets", "activeEstablishedSessions",
            "passiveEstablishedSessions"))
        .forEach(entry -> {
          innerSql.append(", ").append(entry.getValue().getT2()).append(" AS ")
              .append(entry.getKey());
        });

    innerSql.append(" from ").append(getTableName(queryVO));
    innerSql.append(whereSql.toString()).append(") ");

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(
        " select toDateTime(multiply(ceil(divide(toUnixTimestamp(report_time), :interval)), :interval), 'UTC') as timestamp, ");
    // 分组字段
    sql.append(" IPv6NumToString(smallIp) as ipAAddress, IPv6NumToString(bigIp) as ipBAddress");
    // 特殊聚合字段
    specialAggregateFields(aggFields, aggsField);
    List<String> specialSortPropertyByCamel = specialSortProperty.stream()
        .map(item -> TextUtils.underLineToCamel(item)).collect(Collectors.toList());
    aggFields.entrySet().forEach(entry -> {
      if (specialSortPropertyByCamel.contains(entry.getKey())) {
        sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
      } else {
        sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getKey()).append(") AS ").append(entry.getKey());
      }
    });
    sql.append(" from ").append(innerSql.toString());
    sql.append(" group by timestamp, smallIp, bigIp ");
    sql.append(" order by timestamp ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryMetricIpConversationHistograms sql : [{}], param: [{}] ", sql.toString(),
          params);
    }

    return metricDateHistogramDataConversion(
        queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper()));
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao#graphMetricIpConversations(com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> graphMetricIpConversations(MetricFlowLogQueryVO queryVO,
      Integer minEstablishedSessions, Integer minTotalBytes, String sortProperty,
      String sortDirection) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, null, whereSql, params, "");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggFields.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
    aggFields.put("establishedSessions", Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(
        " select IPv6NumToString(smallIp) as ipAAddress, IPv6NumToString(bigIp) as ipBAddress");
    aggFields.forEach((key, value) -> {
      sql.append(", ").append(value.getT1().getOperation()).append("(").append(key).append(") AS ")
          .append(key);
    });
    sql.append(" from ");

    // 子查询开始
    sql.append(
        "(select if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, IPv4ToIPv6(ipv4_initiator), IPv4ToIPv6(ipv4_responder)), if(ipv6_initiator < ipv6_responder, ipv6_initiator, ipv6_responder)) as smallIp, ");
    sql.append(
        " if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, IPv4ToIPv6(ipv4_responder), IPv4ToIPv6(ipv4_initiator)), if(ipv6_initiator < ipv6_responder, ipv6_responder, ipv6_initiator)) as bigIp");
    aggFields.forEach((key, value) -> {
      sql.append(", ").append(value.getT2()).append(" AS ").append(key);
    });
    sql.append(" from ").append(getTableName(queryVO));
    sql.append(whereSql.toString()).append(") ");
    // 子查询结束

    sql.append(" group by smallIp, bigIp ");
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
    sql.append(" limit ").append(queryVO.getCount());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("graphMetricIpConversations sql : [{}], param: [{}] ", sql.toString(), params);
    }

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  @Override
  public Page<Map<String, Object>> queryFlowLogsAsHistogram(MetricFlowLogQueryVO queryVO,
      PageRequest page, String queryProperty) {

    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append("select ");
    switch (queryProperty) {
      case "ip_initiator":
        sql.append(
            " ifNull(IPv4NumToString(ipv4_initiator),IPv6NumToString(ipv6_initiator)) as ip_address,any(ip_locality_initiator) as ip_locality_initiator,sum(total_bytes) as totalBytes, ");
        sql.append(
            " sum(established_sessions) as tcpEstablishedCounts,sum(tcp_established_fail_flag) as tcpEstablishedFailCounts ");
        break;
      case "ip_responder":
        sql.append(
            " ifNull(IPv4NumToString(ipv4_responder),IPv6NumToString(ipv6_responder)) as ip_address,any(ip_locality_responder) as ip_locality_responder,sum(total_bytes) as totalBytes, ");
        sql.append(
            " sum(established_sessions) as tcpEstablishedCounts,sum(tcp_established_fail_flag) as tcpEstablishedFailCounts ");
        break;
      case "port_initiator":
        sql.append(" port_responder,sum(total_bytes) totalBytes, ");
        sql.append(
            " sum(established_sessions) as tcpEstablishedCounts,sum(tcp_established_fail_flag) as tcpEstablishedFailCounts ");
        break;
      case "port_responder":
        sql.append(" port_responder,sum(total_bytes) totalBytes, ");
        sql.append(
            " sum(established_sessions) as tcpEstablishedCounts,sum(tcp_established_fail_flag) as tcpEstablishedFailCounts ");
        break;
      case "application":
        sql.append(" application_id,sum(total_bytes) totalBytes, ");
        sql.append(
            " sum(established_sessions) as tcpEstablishedCounts,sum(tcp_established_fail_flag) as tcpEstablishedFailCounts ");
        break;
      case "country_initiator":
        sql.append(" country_id_initiator,province_id_initiator, ");
        sql.append(
            " sum(established_sessions) as establishedSessions, sum(total_bytes) as totalBytes ");
        break;
      case "country_responder":
        sql.append(" country_id_responder,province_id_responder, ");
        sql.append(
            " sum(established_sessions) as establishedSessions, sum(total_bytes) as totalBytes ");
        break;
    }
    sql.append(" from ").append(getTableName(queryVO));
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (StringUtils.equals(queryProperty, "application")) {
      String ipInitiator = queryVO.getIpInitiator();
      String ipResponder = queryVO.getIpResponder();
      queryVO.setIpInitiator("");
      queryVO.setIpResponder("");
      enrichWhereSql(queryVO, whereSql, params);
      if (StringUtils.isNotBlank(ipInitiator) && StringUtils.isNotBlank(ipResponder)) {
        StringBuilder ipInitiatorWhereSql = new StringBuilder();
        enrichIpCondition(ipInitiator, "ipv4_initiator", "ipv6_initiator", ipInitiatorWhereSql,
            params, "initiator");
        StringBuilder ipResponderWhereSql = new StringBuilder();
        enrichIpCondition(ipResponder, "ipv4_responder", "ipv6_responder", ipResponderWhereSql,
            params, "responder");
        whereSql.append(ipInitiatorWhereSql.toString().replace("and (", "and (("));
        whereSql.append(ipResponderWhereSql.toString().replace("and (", "or (")).append(")");
      }
    } else {
      enrichWhereSql(queryVO, whereSql, params);
    }
    if (StringUtils.equals(queryProperty, "country_initiator")) {
      whereSql.append(" and country_id_initiator is not null ");
    }
    if (StringUtils.equals(queryProperty, "country_responder")) {
      whereSql.append(" and country_id_responder is not null ");
    }
    if (StringUtils.equals(queryProperty, "port_initiator")) {
      whereSql.append(" and downstream_payload_bytes > 0 ");
      whereSql.append(" and port_responder > 0 ");
    }
    sql.append(whereSql);
    switch (queryProperty) {
      case "ip_initiator":
        sql.append(" group by ip_address ");
        break;
      case "ip_responder":
        sql.append(" group by ip_address ");
        break;
      case "port_initiator":
        sql.append(" group by port_responder ");
        break;
      case "port_responder":
        sql.append(" group by port_responder ");
        break;
      case "application":
        sql.append(" group by application_id ");
        break;
      case "country_initiator":
        sql.append(" group by country_id_initiator, province_id_initiator ");
        break;
      case "country_responder":
        sql.append(" group by country_id_responder, province_id_responder ");
        break;
    }
    if (!queryProperty.contains("country")) {
      sql.append(" having totalBytes >0 ");
      if (StringUtils.equalsAny(queryProperty, "ip_initiator", "ip_responder")) {
        if (queryVO.getIpLocalityInitiator() != null) {
          sql.append(" and ip_locality_initiator = :ip_locality_initiator ");
          params.put("ip_locality_initiator", queryVO.getIpLocalityInitiator());
        }
        if (queryVO.getIpLocalityResponder() != null) {
          sql.append(" and ip_locality_responder = :ip_locality_responder ");
          params.put("ip_locality_responder", queryVO.getIpLocalityResponder());
        }
      }
      PageUtils.appendPage(sql, page, Lists.newArrayListWithCapacity(0));
      List<Map<String, Object>> resultList = queryWithExceptionHandle(sql.toString(), params,
          new ColumnMapRowMapper());
      long total = 0;
      return new PageImpl<>(resultList, page, total);
    } else {
      sql.append(" limit 10000 ");
      List<Map<String, Object>> resultList = queryWithExceptionHandle(sql.toString(), params,
          new ColumnMapRowMapper());
      return new PageImpl<>(resultList, page, 10000);
    }

  }

  @Override
  public long countFlowLogsStatistics(MetricFlowLogQueryVO queryVO, String queryProperty) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(" select count(1) from ( ");
    sql.append("select ");
    switch (queryProperty) {
      case "ip_initiator":
        sql.append(
            " ifNull(IPv4NumToString(ipv4_initiator),IPv6NumToString(ipv6_initiator)) as ip_address ");
        break;
      case "ip_responder":
        sql.append(
            " ifNull(IPv4NumToString(ipv4_responder),IPv6NumToString(ipv6_responder)) as ip_address ");
        break;
      case "port_initiator":
        sql.append(" port_responder ");
        break;
      case "port_responder":
        sql.append(" port_responder ");
        break;
      case "application":
        sql.append(" application_id ");
        break;
    }
    sql.append(" from ").append(getTableName(queryVO));
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (StringUtils.equals(queryProperty, "application")) {
      String ipInitiator = queryVO.getIpInitiator();
      String ipResponder = queryVO.getIpResponder();
      queryVO.setIpInitiator("");
      queryVO.setIpResponder("");
      enrichWhereSql(queryVO, whereSql, params);
      if (StringUtils.isNotBlank(ipInitiator) && StringUtils.isNotBlank(ipResponder)) {
        StringBuilder ipInitiatorWhereSql = new StringBuilder();
        enrichIpCondition(ipInitiator, "ipv4_initiator", "ipv6_initiator", ipInitiatorWhereSql,
            params, "initiator");
        StringBuilder ipResponderWhereSql = new StringBuilder();
        enrichIpCondition(ipResponder, "ipv4_responder", "ipv6_responder", ipResponderWhereSql,
            params, "responder");
        whereSql.append(ipInitiatorWhereSql.toString().replace("and (", "and (("));
        whereSql.append(ipResponderWhereSql.toString().replace("and (", "or (")).append(")");
      }
    } else {
      enrichWhereSql(queryVO, whereSql, params);
      if (queryVO.getIpLocalityInitiator() != null) {
        whereSql.append(" and ip_locality_initiator = :ip_locality_initiator ");
        params.put("ip_locality_initiator", queryVO.getIpLocalityInitiator());
      }
      if (queryVO.getIpLocalityResponder() != null) {
        whereSql.append(" and ip_locality_responder = :ip_locality_responder ");
        params.put("ip_locality_responder", queryVO.getIpLocalityResponder());
      }
    }
    if (StringUtils.equals(queryProperty, "port_initiator")) {
      whereSql.append(" and downstream_payload_bytes > 0 ");
      whereSql.append(" and port_responder > 0 ");
    }
    sql.append(whereSql);
    switch (queryProperty) {
      case "ip_initiator":
        sql.append(" group by ip_address ");
        break;
      case "ip_responder":
        sql.append(" group by ip_address ");
        break;
      case "port_initiator":
        sql.append(" group by port_responder ");
        break;
      case "port_responder":
        sql.append(" group by port_responder ");
        break;
      case "application":
        sql.append(" group by application_id ");
        break;
    }
    long total = 0;
    if (!queryProperty.contains("country")) {
      sql.append(" having sum(total_bytes) >0 ").append(")");
      total = queryForLongWithExceptionHandle(sql.toString(), params);
    }
    return total;
  }

  private void enrichWhereSql(MetricFlowLogQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {

    whereSql.append(" where 1 = 1 ");

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), whereSql, params);

    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      whereSql.append(" and has(network_id, :networkId)=1 ");
      params.put("networkId", queryVO.getNetworkId());
    }
    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      whereSql.append(" and hasAny(network_id, [ :networkIds ])=1 ");
      params.put("networkIds", queryVO.getNetworkIds());
    }
    if (StringUtils.isNotBlank(queryVO.getServiceId())) {
      whereSql.append(" and has(service_id, :serviceId)=1 ");
      params.put("serviceId", queryVO.getServiceId());
    }
    if (StringUtils.isNotBlank(queryVO.getIpInitiator())) {
      enrichIpCondition(queryVO.getIpInitiator(), "ipv4_initiator", "ipv6_initiator", whereSql,
          params, "initiator");
    }
    if (StringUtils.isNotBlank(queryVO.getIpResponder())) {
      enrichIpCondition(queryVO.getIpResponder(), "ipv4_responder", "ipv6_responder", whereSql,
          params, "responder");
    }
  }

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    whereSql.append(String.format(" and report_time %s toDateTime64(:start_time, 9, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and report_time %s toDateTime64(:end_time, 9, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  private static void enrichIpCondition(String ipCondition, String ipv4FieldName,
      String ipv6FieldName, StringBuilder whereSql, Map<String, Object> params,
      String paramsIndexIdent) {
    List<String> ipv4List = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, String>> ipv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> ipv6List = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, String>> ipv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<String> ipv4CidrList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> ipv6CidrList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    String[] ipConditionList = StringUtils.split(ipCondition, ",");

    if (ipConditionList.length > FpcCmsConstants.HOSTGROUP_MAX_IP_COUNT) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "查询的IP地址条目数过多, 请修改查询。");
    }

    // 按ip类型分类
    for (String ip : ipConditionList) {
      if (StringUtils.contains(ip, "-")) {
        // ip范围 10.0.0.1-10.0.0.100
        String[] ipRange = StringUtils.split(ip, "-");
        if (ipRange.length != 2) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }

        String ipStart = ipRange[0];
        String ipEnd = ipRange[1];

        // 起止都是正确的ip
        if (!NetworkUtils.isInetAddress(StringUtils.trim(ipStart))
            || !NetworkUtils.isInetAddress(StringUtils.trim(ipEnd))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }
        if (NetworkUtils.isInetAddress(ipStart, IpVersion.V4)
            && NetworkUtils.isInetAddress(ipEnd, IpVersion.V4)) {
          ipv4RangeList.add(Tuples.of(ipStart, ipEnd));
        } else if (NetworkUtils.isInetAddress(ipStart, IpVersion.V6)
            && NetworkUtils.isInetAddress(ipEnd, IpVersion.V6)) {
          ipv6RangeList.add(Tuples.of(ipStart, ipEnd));
        } else {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }
      } else {
        // 单个IP或CIDR格式
        ip = StringUtils.trim(ip);
        if (!NetworkUtils.isInetAddress(ip) && !NetworkUtils.isCidr(ip)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "请输入正确的IP地址");
        }
        if (NetworkUtils.isInetAddress(ip, IpVersion.V4)) {
          ipv4List.add(ip);
        } else if (NetworkUtils.isCidr(ip, IpVersion.V4)) {
          ipv4CidrList.add(ip);
        } else if (NetworkUtils.isInetAddress(ip, IpVersion.V6)
            || NetworkUtils.isCidr(ip, IpVersion.V6)) {
          ipv6List.add(ip);
        } else if (NetworkUtils.isCidr(ip, IpVersion.V6)) {
          ipv6CidrList.add(ip);
        }
      }
    }

    List<String> ipConditionSqlList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    // 拼接sql
    if (CollectionUtils.isNotEmpty(ipv4List) || CollectionUtils.isNotEmpty(ipv6List)
        || CollectionUtils.isNotEmpty(ipv4CidrList) || CollectionUtils.isNotEmpty(ipv6CidrList)
        || CollectionUtils.isNotEmpty(ipv4RangeList) || CollectionUtils.isNotEmpty(ipv6RangeList)) {
      int index = 0;

      // 单ipv4
      if (CollectionUtils.isNotEmpty(ipv4List)) {
        List<String> tmpList = Lists.newArrayListWithCapacity(ipv4List.size());
        for (String ip : ipv4List) {
          tmpList.add("toIPv4(:ipv4_" + index + paramsIndexIdent + ")");
          params.put("ipv4_" + index + paramsIndexIdent, ip);
          index += 1;
        }
        ipConditionSqlList
            .add(String.format(" (%s in (%s)) ", ipv4FieldName, StringUtils.join(tmpList, ",")));
      }

      // 单ipv6
      if (CollectionUtils.isNotEmpty(ipv6List)) {
        List<String> tmpList = Lists.newArrayListWithCapacity(ipv6List.size());
        for (String ip : ipv6List) {
          tmpList.add("toIPv6(:ipv6_" + index + paramsIndexIdent + ")");
          params.put("ipv6_" + index + paramsIndexIdent, ip);
          index += 1;
        }
        ipConditionSqlList
            .add(String.format(" (%s in (%s)) ", ipv6FieldName, StringUtils.join(tmpList, ",")));
      }

      // ipv4掩码
      if (CollectionUtils.isNotEmpty(ipv4CidrList)) {
        for (String ip : ipv4CidrList) {
          ipConditionSqlList.add(String.format(
              " (%s between IPv4CIDRToRange(toIPv4(:ipv4_%s), :cidr_%s).1 and IPv4CIDRToRange(toIPv4(:ipv4_%s), :cidr_%s).2) ",
              ipv4FieldName, index + paramsIndexIdent, index + paramsIndexIdent,
              index + paramsIndexIdent, index + paramsIndexIdent));
          String[] ipAndCidr = ip.split("/");
          params.put("ipv4_" + index + paramsIndexIdent, ipAndCidr[0]);
          params.put("cidr_" + index + paramsIndexIdent, Integer.parseInt(ipAndCidr[1]));
          index += 1;
        }
      }
      // ipv6掩码
      if (CollectionUtils.isNotEmpty(ipv6CidrList)) {
        for (String ip : ipv6CidrList) {
          ipConditionSqlList.add(String.format(
              " (%s between IPv6CIDRToRange(toIPv6(:ipv6_%s), :cidr_%s).1 and IPv6CIDRToRange(toIPv6(:ipv6_%s), :cidr_%s).2) ",
              ipv6FieldName, index + paramsIndexIdent, index + paramsIndexIdent,
              index + paramsIndexIdent, index + paramsIndexIdent));
          String[] ipAndCidr = ip.split("/");
          params.put("ipv6_" + index + paramsIndexIdent, ipAndCidr[0]);
          params.put("cidr_" + index + paramsIndexIdent, Integer.parseInt(ipAndCidr[1]));
          index += 1;
        }
      }

      // ipv4范围
      for (Tuple2<String, String> range : ipv4RangeList) {
        ipConditionSqlList
            .add(String.format(" (%s between toIPv4(:ipv4_start%s) and toIPv4(:ipv4_end%s)) ",
                ipv4FieldName, index + paramsIndexIdent, index + paramsIndexIdent));
        params.put("ipv4_start" + index + paramsIndexIdent, range.getT1());
        params.put("ipv4_end" + index + paramsIndexIdent, range.getT2());
        index += 1;
      }
      // ipv6范围
      for (Tuple2<String, String> range : ipv6RangeList) {
        ipConditionSqlList
            .add(String.format(" (%s between toIPv6(:ipv6_start%s) and toIPv6(:ipv6_end%s)) ",
                ipv6FieldName, index + paramsIndexIdent, index + paramsIndexIdent));
        params.put("ipv6_start" + index + paramsIndexIdent, range.getT1());
        params.put("ipv6_end" + index + paramsIndexIdent, range.getT2());
        index += 1;
      }
      whereSql.append(" and ( ");
      whereSql.append(String.join(" or ", ipConditionSqlList));
      whereSql.append(" ) ");
    }
  }

  private List<Map<String, Object>> metricIpConversation(MetricFlowLogQueryVO queryVO,
      List<Map<String, Object>> keyList, String sortProperty, String sortDirection,
      boolean onlyAggSortProperty) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, null, whereSql, params, "");
    // 将key值集合作为条件过滤
    enrichKeySql(keyList, whereSql, params);

    // 普通聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (onlyAggSortProperty) {
      aggregateFields(aggFields, false, sortProperty);
    } else {
      aggFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
      aggFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
      aggFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
      aggFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
      aggFields.put("activeEstablishedSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));
      aggFields.put("passiveEstablishedSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));

      kpiAggs(aggFields, true);
    }

    // 子查询
    StringBuilder innerSql = new StringBuilder();
    innerSql.append(
        "(select if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, IPv4ToIPv6(ipv4_initiator), IPv4ToIPv6(ipv4_responder)), if(ipv6_initiator < ipv6_responder, ipv6_initiator, ipv6_responder)) as smallIp, ");
    innerSql.append(
        " if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, IPv4ToIPv6(ipv4_responder), IPv4ToIPv6(ipv4_initiator)), if(ipv6_initiator < ipv6_responder, ipv6_responder, ipv6_initiator)) as bigIp");
    if (aggFields.containsKey("upstreamBytes")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, downstream_bytes, upstream_bytes), if(ipv6_initiator < ipv6_responder, downstream_bytes, upstream_bytes)) as upstreamBytes");
    }
    if (aggFields.containsKey("downstreamBytes")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, upstream_bytes, downstream_bytes), if(ipv6_initiator < ipv6_responder, upstream_bytes, downstream_bytes)) as downstreamBytes");
    }
    if (aggFields.containsKey("upstreamPackets")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, downstream_packets, upstream_packets), if(ipv6_initiator < ipv6_responder, downstream_packets, upstream_packets)) as upstreamPackets");
    }
    if (aggFields.containsKey("downstreamPackets")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, upstream_packets, downstream_packets), if(ipv6_initiator < ipv6_responder, upstream_packets, downstream_packets)) as downstreamPackets");
    }
    if (aggFields.containsKey("activeEstablishedSessions")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, established_sessions, 0), if(ipv6_initiator < ipv6_responder, established_sessions, 0)) as activeEstablishedSessions");
    }
    if (aggFields.containsKey("passiveEstablishedSessions")) {
      innerSql.append(
          ", if(isNotNull(ipv4_initiator), if(ipv4_initiator < ipv4_responder, 0, established_sessions), if(ipv6_initiator < ipv6_responder, 0, established_sessions)) as passiveEstablishedSessions");
    }
    aggFields.entrySet().stream()
        .filter(entry -> !StringUtils.equalsAny(entry.getKey(), "upstreamBytes", "downstreamBytes",
            "upstreamPackets", "downstreamPackets", "activeEstablishedSessions",
            "passiveEstablishedSessions"))
        .forEach(entry -> {
          innerSql.append(", ").append(entry.getValue().getT2()).append(" AS ")
              .append(entry.getKey());
        });

    innerSql.append(" from ").append(getTableName(queryVO));
    innerSql.append(whereSql.toString()).append(") ");

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(
        " select IPv6NumToString(smallIp) as ipAAddress, IPv6NumToString(bigIp) as ipBAddress");
    // 特殊聚合字段
    if (onlyAggSortProperty) {
      specialAggregateFields(aggFields, sortProperty);
    } else {
      specialKpiAggs(aggFields);
    }
    List<String> specialSortPropertyByCamel = specialSortProperty.stream()
        .map(item -> TextUtils.underLineToCamel(item)).collect(Collectors.toList());
    aggFields.entrySet().forEach(entry -> {
      if (specialSortPropertyByCamel.contains(entry.getKey())) {
        sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
      } else {
        sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getKey()).append(") AS ").append(entry.getKey());
      }
    });
    sql.append(" from ").append(innerSql.toString());
    sql.append(" group by smallIp, bigIp ");
    sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
        .append(sortDirection);
    sql.append(" limit ").append(queryVO.getCount());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("metricIpConversation sql : [{}], param: [{}] ", sql.toString(), params);
    }

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  /**
   * 流日志单向聚合统计（仅聚合排序键）
   * @param queryVO 过滤条件
   * @param keys <分组字段名， 别名>
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @return 统计结果
   * @return
   */
  private List<Map<String, Object>> unidirectionalMetricStatisticsOnlySortProperty(
      MetricFlowLogQueryVO queryVO, Map<String, String> keys, String sortProperty,
      String sortDirection) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, keys, whereSql, params, "");

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    List<String> terms = keys.entrySet().stream()
        .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
    sql.append(" select ").append(StringUtils.join(terms, ","));

    // 指标
    Map<String, Tuple2<AggsFunctionEnum, String>> aggs = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(aggs, false, sortProperty);
    specialAggregateFields(aggs, sortProperty);
    aggs.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(getTableName(queryVO));
    sql.append(whereSql.toString());
    sql.append(" group by ").append(StringUtils.join(keys.values(), ","));
    sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
        .append(sortDirection);
    sql.append(" limit ").append(queryVO.getCount());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("unidirectionalMetricStatisticsOnlySortProperty sql : [{}], param: [{}] ",
          sql.toString(), params);
    }

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  /**
   * 流日志单向聚合统计
   * @param queryVO 过滤条件
   * @param keyList key值过滤集合
   * @param keys <分组字段名， 别名>
   * @param aggs <聚合字段名，<聚合类型，别名>>
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @return 统计结果
   */
  private List<Map<String, Object>> unidirectionalMetricStatistics(MetricFlowLogQueryVO queryVO,
      List<Map<String, Object>> keyList, Map<String, String> keys,
      Map<String, Tuple2<AggsFunctionEnum, String>> aggs, String sortProperty,
      String sortDirection) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, keys, whereSql, params, "");
    // 将key值集合作为条件过滤
    enrichKeySql(keyList, whereSql, params);

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    List<String> terms = keys.entrySet().stream()
        .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
    sql.append(" select ").append(StringUtils.join(terms, ","));

    // 填充关键指标
    kpiAggs(aggs, true);
    specialKpiAggs(aggs);
    // 过滤用户指定的列
    aggs = filterColumns(aggs, queryVO.getColumns(), sortProperty);
    aggs.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(getTableName(queryVO));
    sql.append(whereSql.toString());
    sql.append(" group by ").append(StringUtils.join(keys.values(), ","));
    sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
        .append(sortDirection);
    sql.append(" limit ").append(queryVO.getCount());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("unidirectionalMetricStatistics sql : [{}], param: [{}] ", sql.toString(),
          params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    return StringUtils.equals(queryVO.getColumns(), "*") ? metricDataConversion(result)
        : metricDateHistogramDataConversion(result);
  }

  /**
   * 流日志双向聚合统计（仅聚合排序键）
   * @param keyAndAggsList T4<keys(分组字段),aggs(聚合字段),queryVO(查询条件),statisticsKpi(是否聚合kpi指标)>
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @param internalSortAndLimit 子查询是否排序限制返回条数
   * @return 统计结果
   */
  private List<Map<String, Object>> bilateralMetricStatisticsOnlySortProperty(
      List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
          MetricFlowLogQueryVO, Boolean>> keyAndAggsList,
      String sortProperty, String sortDirection, boolean internalSortAndLimit) {
    // 排序键聚合所需字段集合
    Map<String, Tuple2<AggsFunctionEnum, String>> sortPropertyFieldMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(sortPropertyFieldMap, false, sortProperty);
    specialAggregateFields(sortPropertyFieldMap, sortProperty);
    List<String> sortPropertyFields = sortPropertyFieldMap.entrySet().stream()
        .map(item -> item.getKey()).collect(Collectors.toList());

    // 构造临时表
    List<String> tempTables = Lists.newArrayListWithCapacity(keyAndAggsList.size());
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    int index = 1;
    for (Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO, Boolean> keyAndAggs : keyAndAggsList) {
      // 构造过滤条件
      StringBuilder whereSql = new StringBuilder();
      enrichWhereSql(keyAndAggs.getT3(), keyAndAggs.getT1(), whereSql, params,
          String.valueOf(index));

      // 构造临时表查询语句
      StringBuilder unidirectionalSql = new StringBuilder();
      List<String> terms = keyAndAggs.getT1().entrySet().stream()
          .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
      unidirectionalSql.append("select ").append(StringUtils.join(terms, ","));

      // 填充指标
      kpiAggs(keyAndAggs.getT2(), keyAndAggs.getT4());
      keyAndAggs.getT2().entrySet().stream()
          .filter(agg -> sortPropertyFields.contains(agg.getKey())).forEach(entry -> {
            unidirectionalSql.append(", ").append(entry.getValue().getT1().getOperation())
                .append("(").append(entry.getValue().getT2()).append(") AS ")
                .append(entry.getKey());
          });
      unidirectionalSql.append(" from ").append(getTableName(keyAndAggs.getT3()));
      unidirectionalSql.append(whereSql.toString());
      unidirectionalSql.append(" group by ")
          .append(StringUtils.join(keyAndAggs.getT1().values(), ","));
      if (!specialSortProperty.contains(sortProperty) && internalSortAndLimit) {
        unidirectionalSql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty))
            .append(" ").append(sortDirection);
        unidirectionalSql.append(" limit ").append(keyAndAggs.getT3().getCount());
      }

      tempTables.add(unidirectionalSql.toString());
      index++;
    }

    // 标识查询，用于取消查询
    String queryId = keyAndAggsList.get(0).getT3().getQueryId();
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 构造完整查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(" select ");
    // 分组字段
    sql.append(StringUtils.join(keyAndAggsList.get(0).getT1().values(), ","));
    // 普通聚合字段
    keyAndAggsList.get(0).getT2().entrySet().stream()
        .filter(agg -> sortPropertyFields.contains(agg.getKey())).forEach(entry -> {
          sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
              .append(entry.getKey()).append(") AS ").append(entry.getKey());
        });
    // 计算特殊字段
    Map<String, Tuple2<AggsFunctionEnum, String>> specialKpiAggs = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    specialKpiAggs(specialKpiAggs);
    specialKpiAggs.entrySet().stream().filter(agg -> sortPropertyFields.contains(agg.getKey()))
        .forEach(entry -> {
          sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
              .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
        });
    // 构造查询对象
    sql.append(" from (").append(StringUtils.join(tempTables, " UNION ALL ")).append(") ");
    // 聚合
    sql.append(" group by ").append(StringUtils.join(keyAndAggsList.get(0).getT1().values(), ","));
    // 排序
    sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
        .append(sortDirection);
    sql.append(" limit ").append(keyAndAggsList.get(0).getT3().getCount());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("bilateralMetricStatisticsOnlySortProperty sql : [{}], param: [{}] ",
          sql.toString(), params);
    }

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  /**
   * 流日志双向聚合统计
   * @param keyAndAggsList T5<keys(分组字段),aggs(聚合字段),queryVO(查询条件),keyList(指定key值集合),statisticsKpi(是否聚合kpi指标)>
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @param internalSortAndLimit 子查询是否排序限制返回条数
   * @return 统计结果
   */
  private List<Map<String, Object>> bilateralMetricStatistics(
      List<Tuple5<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
          MetricFlowLogQueryVO, Boolean, List<Map<String, Object>>>> keyAndAggsList,
      String sortProperty, String sortDirection, boolean internalSortAndLimit) {
    // 构造临时表
    List<String> tempTables = Lists.newArrayListWithCapacity(keyAndAggsList.size());
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    int index = 1;
    for (Tuple5<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO, Boolean, List<Map<String, Object>>> keyAndAggs : keyAndAggsList) {
      // 构造过滤条件
      StringBuilder whereSql = new StringBuilder();
      if (CollectionUtils.isNotEmpty(keyAndAggs.getT5())) {
        enrichWhereSql(keyAndAggs.getT3(), keyAndAggs.getT1(), whereSql, params,
            String.valueOf(index));
        // 将key值集合作为条件过滤
        enrichKeySql(keyAndAggs.getT5(), whereSql, params);
      } else {
        whereSql.append(" where 1=2 ");
      }

      // 构造临时表查询语句
      StringBuilder unidirectionalSql = new StringBuilder();
      List<String> terms = keyAndAggs.getT1().entrySet().stream()
          .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
      unidirectionalSql.append("select ").append(StringUtils.join(terms, ","));

      // 填充关键指标
      kpiAggs(keyAndAggs.getT2(), keyAndAggs.getT4());
      // 查询指定的指标
      Map<String, Tuple2<AggsFunctionEnum, String>> filterColumns = filterColumns(
          keyAndAggs.getT2(), keyAndAggs.getT3().getColumns(), sortProperty);
      filterColumns.entrySet().forEach(entry -> {
        unidirectionalSql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
      });
      unidirectionalSql.append(" from ").append(getTableName(keyAndAggs.getT3()));
      unidirectionalSql.append(whereSql.toString());
      unidirectionalSql.append(" group by ")
          .append(StringUtils.join(keyAndAggs.getT1().values(), ","));
      if (!specialSortProperty.contains(sortProperty) && internalSortAndLimit) {
        unidirectionalSql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty))
            .append(" ").append(sortDirection);
        unidirectionalSql.append(" limit ").append(keyAndAggs.getT3().getCount());
      }

      tempTables.add(unidirectionalSql.toString());
      index++;
    }

    // 标识查询，用于取消查询
    String queryId = keyAndAggsList.get(0).getT3().getQueryId();
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 构造完整查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(" select ");
    // 分组字段
    sql.append(StringUtils.join(keyAndAggsList.get(0).getT1().values(), ","));
    // 普通聚合字段做加和
    Map<String, Tuple2<AggsFunctionEnum, String>> filterColumns = filterColumns(
        keyAndAggsList.get(0).getT2(), keyAndAggsList.get(0).getT3().getColumns(), sortProperty);
    filterColumns.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getKey()).append(") AS ").append(entry.getKey());
    });
    // 计算特殊字段
    Map<String, Tuple2<AggsFunctionEnum, String>> specialKpiAggs = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    specialKpiAggs(specialKpiAggs);
    // 查询指定的指标
    specialKpiAggs = filterColumns(specialKpiAggs, keyAndAggsList.get(0).getT3().getColumns(),
        sortProperty);
    specialKpiAggs.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    // 构造查询对象
    sql.append(" from (").append(StringUtils.join(tempTables, " UNION ALL ")).append(") ");
    // 聚合
    sql.append(" group by ").append(StringUtils.join(keyAndAggsList.get(0).getT1().values(), ","));
    // 排序
    sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
        .append(sortDirection);
    sql.append(" limit ").append(keyAndAggsList.get(0).getT3().getCount());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("bilateralMetricStatistics sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    return StringUtils.equals(keyAndAggsList.get(0).getT3().getColumns(), "*")
        ? metricDataConversion(result)
        : metricDateHistogramDataConversion(result);
  }

  /**
   * 流日志单向TOPN趋势图
   * @param queryVO 前端过滤条件
   * @param additionalConditions topN的key值
   * @param keys <分组字段名， 别名>
   * @param aggsField 聚合指标字段名称
   * @return
   */
  private List<Map<String, Object>> unidirectionalMetricDateHistogramStatistics(
      MetricFlowLogQueryVO queryVO, List<Map<String, Object>> additionalConditions,
      Map<String, String> keys, String aggsField) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());
    enrichWhereSql(queryVO, keys, whereSql, params, "");
    // 添加附加条件
    enrichKeySql(additionalConditions, whereSql, params);

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(
        " select toDateTime(multiply(ceil(divide(toUnixTimestamp(report_time), :interval)), :interval), 'UTC') as timestamp, ");
    // 分组字段
    List<String> terms = keys.entrySet().stream()
        .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
    sql.append(StringUtils.join(terms, ","));
    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggregateFields(aggFields, false, aggsField);
    specialAggregateFields(aggFields, aggsField);
    aggFields.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });

    sql.append(" from ").append(getTableName(queryVO));
    sql.append(whereSql.toString());
    sql.append(" group by timestamp, ").append(StringUtils.join(keys.values(), ","));
    sql.append(" order by timestamp ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("unidirectionalMetricDateHistogramStatistics sql : [{}], param: [{}] ",
          sql.toString(), params);
    }

    return metricDateHistogramDataConversion(
        queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper()));
  }

  /**
   * 流日志双向TOPN趋势图
   * @param keyAndAggsList T3<keys(分组字段),aggs(聚合字段),queryVO(查询条件),additionalConditions(附加条件)>
   * @param aggsField 聚合指标字段名称
   * @param interval 时间间隔
   * @return
   */
  private List<Map<String, Object>> bilateralMetricDateHistogramStatistics(
      List<Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
          MetricFlowLogQueryVO, List<Map<String, Object>>>> keyAndAggsList,
      String aggsField, int interval) {
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", interval);

    // 构造临时表
    List<String> tempTables = Lists.newArrayListWithCapacity(keyAndAggsList.size());
    int index = 1;
    for (Tuple4<Map<String, String>, Map<String, Tuple2<AggsFunctionEnum, String>>,
        MetricFlowLogQueryVO, List<Map<String, Object>>> keyAndAggs : keyAndAggsList) {
      // 构造过滤条件
      StringBuilder whereSql = new StringBuilder();
      enrichWhereSql(keyAndAggs.getT3(), keyAndAggs.getT1(), whereSql, params,
          String.valueOf(index));
      // 添加TOPN排行key过滤
      enrichKeySql(keyAndAggs.getT4(), whereSql, params);

      // 构造查询语句
      StringBuilder unidirectionalSql = new StringBuilder();
      unidirectionalSql.append(
          "select toDateTime(multiply(ceil(divide(toUnixTimestamp(report_time), :interval)), :interval), 'UTC') as timestamp, ");
      // 分组字段
      List<String> terms = keyAndAggs.getT1().entrySet().stream()
          .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
      unidirectionalSql.append(StringUtils.join(terms, ","));
      // 聚合字段
      keyAndAggs.getT2().entrySet().forEach(entry -> {
        unidirectionalSql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
      });

      unidirectionalSql.append(" from ").append(getTableName(keyAndAggs.getT3()));
      unidirectionalSql.append(whereSql.toString());
      unidirectionalSql.append(" group by timestamp, ")
          .append(StringUtils.join(keyAndAggs.getT1().values(), ","));
      unidirectionalSql.append(" order by timestamp ");

      tempTables.add(unidirectionalSql.toString());
      index++;
    }

    // 标识查询，用于取消查询
    String queryId = keyAndAggsList.get(0).getT3().getQueryId();
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 构造完整查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    // 分组字段
    sql.append(" select timestamp,")
        .append(StringUtils.join(keyAndAggsList.get(0).getT1().values(), ","));
    // 普通聚合字段做加和
    keyAndAggsList.get(0).getT2().entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getKey()).append(") AS ").append(entry.getKey());
    });
    // 计算特殊字段
    Map<String, Tuple2<AggsFunctionEnum, String>> specialKpiAggs = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    specialAggregateFields(specialKpiAggs, aggsField);
    specialKpiAggs.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    // 构造查询对象
    sql.append(" from (").append(StringUtils.join(tempTables, " UNION ALL ")).append(") ");
    // 聚合
    sql.append(" group by timestamp,")
        .append(StringUtils.join(keyAndAggsList.get(0).getT1().values(), ","));
    // 排序
    sql.append(" order by timestamp ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("bilateralMetricDateHistogramStatistics sql : [{}], param: [{}] ",
          sql.toString(), params);
    }

    return metricDateHistogramDataConversion(
        queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper()));
  }

  private Map<String, Tuple2<AggsFunctionEnum, String>> filterColumns(
      Map<String, Tuple2<AggsFunctionEnum, String>> aggFields, String columns,
      String sortProperty) {
    if (!StringUtils.equals(columns, "*")) {
      // 过滤要查询的列
      Set<String> fields = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      List<String> list = CsvUtils.convertCSVToList(columns);
      list.add(TextUtils.underLineToCamel(sortProperty));
      list.forEach(field -> {
        List<String> aggsFields;
        switch (field) {
          case "tcpClientNetworkLatencyAvg":
            aggsFields = Lists.newArrayList("tcpClientNetworkLatency",
                "tcpClientNetworkLatencyCounts", "tcpClientNetworkLatencyAvg");
            break;
          case "tcpServerNetworkLatencyAvg":
            aggsFields = Lists.newArrayList("tcpServerNetworkLatency",
                "tcpServerNetworkLatencyCounts", "tcpServerNetworkLatencyAvg");
            break;
          case "serverResponseLatencyAvg":
            aggsFields = Lists.newArrayList("serverResponseLatency", "serverResponseLatencyCounts",
                "serverResponseLatencyAvg");
            break;
          case "tcpClientRetransmissionRate":
            aggsFields = Lists.newArrayList("tcpClientRetransmissionPackets", "tcpClientPackets",
                "tcpClientRetransmissionRate");
            break;
          case "tcpServerRetransmissionRate":
            aggsFields = Lists.newArrayList("tcpServerRetransmissionPackets", "tcpServerPackets",
                "tcpServerRetransmissionRate");
            break;
          default:
            aggsFields = Lists.newArrayList(field);
            break;
        }
        fields.addAll(aggsFields);
      });

      aggFields = aggFields.entrySet().stream()
          .filter(aggsField -> fields.contains(aggsField.getKey()))
          .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    return aggFields;
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
        LOGGER.info("drilldown flowLogs has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("drilldown flowLogs failed, error msg: {}",
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

  protected Long queryForLongWithExceptionHandle(String sql, Map<String, ?> paramMap) {
    Long result = 0L;
    try {
      result = clickHouseTemplate.getJdbcTemplate().queryForObject(sql, paramMap, Long.class);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query metadata log has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query metadata log failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else {
        throw e;
      }
    }
    return result;
  }

  /**
   * 关键指标
   * @param kpiAggs 关键指标
   * @param statisticsKpi 是否统计kpi值（由于双向聚合使用UNION ALL，各个子查询所包含的字段必须一致，尽管kpi字段不用聚合也需要在子查询中出现该字段，用0代替该字段原有的值不影响最终结果）
   */
  private void kpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> kpiAggs,
      boolean statisticsKpi) {
    // 总字节数
    kpiAggs.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "total_bytes" : "0"));
    // 总包数
    kpiAggs.put("totalPackets",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "total_packets" : "0"));
    // 新建会话数
    kpiAggs.put("establishedSessions",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "established_sessions" : "0"));
    // TCP客户端网络总时延
    kpiAggs.put("tcpClientNetworkLatency",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_client_network_latency" : "0"));
    // TCP客户端网络时延统计次数
    kpiAggs.put("tcpClientNetworkLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_client_network_latency_flag" : "0"));
    // TCP服务端网络总时延
    kpiAggs.put("tcpServerNetworkLatency",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_server_network_latency" : "0"));
    // TCP服务端网络时延统计次数
    kpiAggs.put("tcpServerNetworkLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_server_network_latency_flag" : "0"));
    // 服务端响应总时延
    kpiAggs.put("serverResponseLatency",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "server_response_latency" : "0"));
    // 服务端响应时延统计次数
    kpiAggs.put("serverResponseLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "server_response_latency_flag" : "0"));
    // TCP客户端总包数
    kpiAggs.put("tcpClientPackets",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_client_packets" : "0"));
    // TCP客户端重传包数
    kpiAggs.put("tcpClientRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_client_retransmission_packets" : "0"));
    // TCP服务端总包数
    kpiAggs.put("tcpServerPackets",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_server_packets" : "0"));
    // TCP服务端重传包数
    kpiAggs.put("tcpServerRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_server_retransmission_packets" : "0"));
    // 客户端零窗口包数
    kpiAggs.put("tcpClientZeroWindowPackets",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_client_zero_window_packets" : "0"));
    // 服务端零窗口包数
    kpiAggs.put("tcpServerZeroWindowPackets",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_server_zero_window_packets" : "0"));
    // TCP建连成功数
    kpiAggs.put("tcpEstablishedSuccessCounts",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_established_success_flag" : "0"));
    // TCP建连失败数
    kpiAggs.put("tcpEstablishedFailCounts",
        Tuples.of(AggsFunctionEnum.SUM, statisticsKpi ? "tcp_established_fail_flag" : "0"));
  }

  /**
   * 关键指标
   * @param kpiAggs 关键指标
   */
  private void specialKpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> kpiAggs) {
    // TCP客户端时延均值
    kpiAggs.put("tcpClientNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
        "tcpClientNetworkLatency, tcpClientNetworkLatencyCounts"));
    // TCP服务端时延均值
    kpiAggs.put("tcpServerNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
        "tcpServerNetworkLatency, tcpServerNetworkLatencyCounts"));
    // 服务端响应时延均值
    kpiAggs.put("serverResponseLatencyAvg",
        Tuples.of(AggsFunctionEnum.DIVIDE, "serverResponseLatency, serverResponseLatencyCounts"));
    // TCP客户端重传率
    kpiAggs.put("tcpClientRetransmissionRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpClientRetransmissionPackets, tcpClientPackets"));
    // TCP服务端重传率
    kpiAggs.put("tcpServerRetransmissionRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpServerRetransmissionPackets, tcpServerPackets"));
  }

  /**
   * 流日志基础字段聚合方式
   * @param aggFields
   * @param downstream
   * @param field
   * @return
   */
  private void aggregateFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggFields,
      boolean downstream, String field) {
    /*
     * 随机选择排序字段，可在此方法内找到该字段的聚合方式。 如果是特殊字段，需要把计算的前置条件先进行聚合，
     * 至于特殊字段的计算要根据该统计是否区分上下行来分别处理：如果区分上下行就不能再聚合时直接计算，需要上下行数据汇总后再计算；如果不区分上下行可直接计算特殊值
     */
    // 总字节数
    if (StringUtils.equals(field, "total_bytes")) {
      aggFields.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
    }
    // 上行字节数
    if (StringUtils.equals(field, "upstream_bytes")) {
      if (downstream) {
        aggFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
      } else {
        aggFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
      }
    }
    // 下行字节数
    if (StringUtils.equals(field, "downstream_bytes")) {
      if (downstream) {
        aggFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
      } else {
        aggFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
      }
    }
    // 总负载字节数
    if (StringUtils.equals(field, "total_payload_bytes")) {
      aggFields.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "total_payload_bytes"));
    }
    // 上行负载字节数
    if (StringUtils.equals(field, "upstream_payload_bytes")) {
      if (downstream) {
        aggFields.put("upstreamPayloadBytes",
            Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));
      } else {
        aggFields.put("upstreamPayloadBytes",
            Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));
      }
    }
    // 下行负载字节数
    if (StringUtils.equals(field, "downstream_payload_bytes")) {
      if (downstream) {
        aggFields.put("downstreamPayloadBytes",
            Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));
      } else {
        aggFields.put("downstreamPayloadBytes",
            Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));
      }
    }
    // 总包数
    if (StringUtils.equals(field, "total_packets")) {
      aggFields.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
    }
    // 上行包数
    if (StringUtils.equals(field, "upstream_packets")) {
      if (downstream) {
        aggFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
      } else {
        aggFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
      }
    }
    // 下行包数
    if (StringUtils.equals(field, "downstream_packets")) {
      if (downstream) {
        aggFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
      } else {
        aggFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
      }
    }
    // 总负载包数
    if (StringUtils.equals(field, "total_payload_packets")) {
      aggFields.put("totalPayloadPackets",
          Tuples.of(AggsFunctionEnum.SUM, "total_payload_packets"));
    }
    // 上行负载包数
    if (StringUtils.equals(field, "upstream_payload_packets")) {
      if (downstream) {
        aggFields.put("upstreamPayloadPackets",
            Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));
      } else {
        aggFields.put("upstreamPayloadPackets",
            Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));
      }
    }
    // 下行负载包数
    if (StringUtils.equals(field, "downstream_payload_packets")) {
      if (downstream) {
        aggFields.put("downstreamPayloadPackets",
            Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));
      } else {
        aggFields.put("downstreamPayloadPackets",
            Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));
      }
    }
    // tcp同步数据包
    if (StringUtils.equals(field, "tcp_syn_packets")) {
      aggFields.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
    }
    // tcp同步确认数据包
    if (StringUtils.equals(field, "tcp_syn_ack_packets")) {
      aggFields.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
    }
    // tcp同步重置数据包
    if (StringUtils.equals(field, "tcp_syn_rst_packets")) {
      aggFields.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));
    }
    // 新建会话数
    if (StringUtils.equals(field, "established_sessions")) {
      aggFields.put("establishedSessions", Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));
    }
    // 主动新建会话数
    if (StringUtils.equals(field, "active_established_sessions")) {
      if (downstream) {
        aggFields.put("activeEstablishedSessions", Tuples.of(AggsFunctionEnum.SUM, "0"));
      } else {
        aggFields.put("activeEstablishedSessions",
            Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));
      }
    }
    // 被动新建会话数
    if (StringUtils.equals(field, "passive_established_sessions")) {
      if (downstream) {
        aggFields.put("passiveEstablishedSessions",
            Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));
      } else {
        aggFields.put("passiveEstablishedSessions", Tuples.of(AggsFunctionEnum.SUM, "0"));
      }
    }
    // 客户端网络总时延
    if (StringUtils.equals(field, "tcp_client_network_latency")) {
      aggFields.put("tcpClientNetworkLatency",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency"));
    }
    // 客户端网络时延均值
    if (StringUtils.equals(field, "tcp_client_network_latency_avg")) {
      aggFields.put("tcpClientNetworkLatency",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency"));
      aggFields.put("tcpClientNetworkLatencyCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_flag"));
    }
    // 服务端网络总时延
    if (StringUtils.equals(field, "tcp_server_network_latency")) {
      aggFields.put("tcpServerNetworkLatency",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency"));
    }
    // 服务端网络时延均值
    if (StringUtils.equals(field, "tcp_server_network_latency_avg")) {
      aggFields.put("tcpServerNetworkLatency",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency"));
      aggFields.put("tcpServerNetworkLatencyCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_flag"));
    }
    // 服务端响应总时延
    if (StringUtils.equals(field, "server_response_latency")) {
      aggFields.put("serverResponseLatency",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency"));
    }
    // 服务端响应时延均值
    if (StringUtils.equals(field, "server_response_latency_avg")) {
      aggFields.put("serverResponseLatency",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency"));
      aggFields.put("serverResponseLatencyCounts",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency_flag"));
    }
    // TCP客户端总包数
    if (StringUtils.equals(field, "tcp_client_packets")) {
      aggFields.put("tcpClientPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets"));
    }
    // TCP客户端重传包数
    if (StringUtils.equals(field, "tcp_client_retransmission_packets")) {
      aggFields.put("tcpClientRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
    }
    // TCP客户端重传率
    if (StringUtils.equals(field, "tcp_client_retransmission_rate")) {
      aggFields.put("tcpClientRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
      aggFields.put("tcpClientPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets"));
    }
    // TCP服务端总包数
    if (StringUtils.equals(field, "tcp_server_packets")) {
      aggFields.put("tcpServerPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets"));
    }
    // TCP服务端重传包数
    if (StringUtils.equals(field, "tcp_server_retransmission_packets")) {
      aggFields.put("tcpServerRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));
    }
    // TCP服务端重传率
    if (StringUtils.equals(field, "tcp_server_retransmission_rate")) {
      aggFields.put("tcpServerRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));
      aggFields.put("tcpServerPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets"));
    }
    // TCP客户端零窗口包数
    if (StringUtils.equals(field, "tcp_client_zero_window_packets")) {
      aggFields.put("tcpClientZeroWindowPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_zero_window_packets"));
    }
    // TCP服务端零窗口包数
    if (StringUtils.equals(field, "tcp_server_zero_window_packets")) {
      aggFields.put("tcpServerZeroWindowPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_zero_window_packets"));
    }
    // TCP建连成功数
    if (StringUtils.equals(field, "tcp_established_success_counts")) {
      aggFields.put("tcpEstablishedSuccessCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_success_flag"));
    }
    // TCP建连失败数
    if (StringUtils.equals(field, "tcp_established_fail_counts")) {
      aggFields.put("tcpEstablishedFailCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_fail_flag"));
    }
  }

  /**
   * 流日志特殊字段聚合方式
   * @param specialAggFields
   * @param field
   * @return
   */
  private void specialAggregateFields(
      Map<String, Tuple2<AggsFunctionEnum, String>> specialAggFields, String field) {
    // 客户端网络时延均值
    if (StringUtils.equals(field, "tcp_client_network_latency_avg")) {
      specialAggFields.put("tcpClientNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpClientNetworkLatency, tcpClientNetworkLatencyCounts"));
    }
    // 服务端网络时延均值
    if (StringUtils.equals(field, "tcp_server_network_latency_avg")) {
      specialAggFields.put("tcpServerNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpServerNetworkLatency, tcpServerNetworkLatencyCounts"));
    }
    // 服务端响应时延均值
    if (StringUtils.equals(field, "server_response_latency_avg")) {
      specialAggFields.put("serverResponseLatencyAvg",
          Tuples.of(AggsFunctionEnum.DIVIDE, "serverResponseLatency, serverResponseLatencyCounts"));
    }
    // 客户端重传率
    if (StringUtils.equals(field, "tcp_client_retransmission_rate")) {
      specialAggFields.put("tcpClientRetransmissionRate",
          Tuples.of(AggsFunctionEnum.DIVIDE, "tcpClientRetransmissionPackets, tcpClientPackets"));
    }
    // 服务端重传率
    if (StringUtils.equals(field, "tcp_server_retransmission_rate")) {
      specialAggFields.put("tcpServerRetransmissionRate",
          Tuples.of(AggsFunctionEnum.DIVIDE, "tcpServerRetransmissionPackets, tcpServerPackets"));
    }
  }

  private String getTableName(MetricFlowLogQueryVO queryVO) {
    String tableName = TABLE_FLOW_LOG_RECORD;
    if (StringUtils.equals(queryVO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_PACKET_FILE)) {
      tableName = String.join("_", TABLE_FLOW_LOG_RECORD, queryVO.getPacketFileId());

      // 判断离线数据包文件的分析结果是否存在
      List<Map<String, Object>> existResult = clickHouseTemplate.getJdbcTemplate().query(String
          .format("show tables from %s where name = '%s'", CenterConstants.FPC_DATABASE, tableName),
          new ColumnMapRowMapper());
      if (CollectionUtils.isEmpty(existResult)) {
        LOGGER.warn("search failed, offline packet file flowlog not found: {}", tableName);
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "离线分析结果异常，未查找到会话详单");
      }
    }

    return tableName;
  }

  /**
   * 筛选双向组合有效key值
   * @param sql
   * @param keys
   */
  @SuppressWarnings("unused")
  private void filterBilateralKey(StringBuilder sql, Collection<String> keys) {
    Iterator<String> queryKeyIterator = keys.iterator();
    while (queryKeyIterator.hasNext()) {
      String key = queryKeyIterator.next();
      switch (key) {
        case "countryId":
        case "provinceId":
        case "cityId":
        case "ipLocality":
        case "port":
          sql.append(" if(assumeNotNull(t1.").append(key).append(") != 0, t1.").append(key)
              .append(", t2.").append(key).append(") AS ").append(key);
          break;
        case "hostgroupId":
        case "macAddress":
          sql.append(" if(notEmpty(t1.").append(key).append("), t1.").append(key).append(", t2.")
              .append(key).append(") AS ").append(key);
          break;
        case "ipv4Address":
        case "ipv6Address":
          sql.append(" if(isNotNull(t1.").append(key).append("), t1.").append(key).append(", t2.")
              .append(key).append(") AS ").append(key);
          break;
        default:
          throw new UnsupportedOperationException();
      }

      if (queryKeyIterator.hasNext()) {
        sql.append(",");
      }
    }
  }

  private void enrichWhereSql(MetricFlowLogQueryVO queryVO, Map<String, String> keys,
      StringBuilder whereSql, Map<String, Object> params, String tag) {
    whereSql.append(" where 1 = 1 ");

    // 使用dsl表达式查询
    List<Map<String, Object>> filterContents = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        filterContents = spl2SqlHelper.getFilterContent(queryVO.getDsl());
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // key值不能为空
    switch (queryVO.getFilterType()) {
      case FILTER_SOURCE_NETWORK:
        break;
      case FILTER_SOURCE_LOCATION_UP:
        whereSql.append(" and isNotNull(country_id_responder) ");
        break;
      case FILTER_SOURCE_LOCATION_DOWN:
        whereSql.append(" and isNotNull(country_id_initiator) ");
        break;
      case FILTER_SOURCE_IP_INITIATOR:
        whereSql.append(" and (isNotNull(ipv4_initiator) or isNotNull(ipv6_initiator)) ");
        break;
      case FILTER_SOURCE_IP_RESPONDER:
        whereSql.append(" and (isNotNull(ipv4_responder) or isNotNull(ipv6_responder)) ");
        break;
      case FILTER_SOURCE_IP_CONVERSATION:
        whereSql.append(" and (isNotNull(ipv4_initiator) or isNotNull(ipv6_initiator)) ");
        whereSql.append(" and (isNotNull(ipv4_responder) or isNotNull(ipv6_responder)) ");
        break;
      default:
        if (MapUtils.isNotEmpty(keys)) {
          keys.entrySet().forEach(entry -> {
            whereSql.append(" and isNotNull(").append(entry.getKey()).append(") ");
          });
        }
        break;
    }

    // 过滤时间 <左闭右开>
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(), whereSql,
        params);

    // 过滤流方向，区分上下行
    if (queryVO.getIpLocalityInitiator() != null) {
      whereSql.append(String.format(" and ip_locality_initiator = :ip_locality_initiator%s ", tag));
      params.put("ip_locality_initiator" + tag, queryVO.getIpLocalityInitiator());
    }
    if (queryVO.getIpLocalityResponder() != null) {
      whereSql.append(String.format(" and ip_locality_responder = :ip_locality_responder%s ", tag));
      params.put("ip_locality_responder" + tag, queryVO.getIpLocalityResponder());
    }

    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      whereSql.append(" and (1=2 ");
      for (int i = 0; i < queryVO.getNetworkIds().size(); i++) {
        whereSql.append(String.format(" or has(network_id, :networkId%s)=1 ", i));
        params.put("networkId" + i, queryVO.getNetworkIds().get(i));
      }
      whereSql.append(" ) ");
    }

    if (CollectionUtils.isNotEmpty(queryVO.getServiceNetworkIds())) {
      whereSql.append(" and (1=2 ");
      for (int i = 0; i < queryVO.getServiceNetworkIds().size(); i++) {
        whereSql.append(String.format(
            " or ( has(service_id, :serviceId%s)=1 and has(network_id, :networkId%s)=1 )", i, i));
        params.put("serviceId" + i, queryVO.getServiceNetworkIds().get(i).getT1());
        params.put("networkId" + i, queryVO.getServiceNetworkIds().get(i).getT2());
      }
      whereSql.append(" ) ");
    }

    int index = 0;
    for (Map<String, Object> fieldMap : filterContents) {
      index++;
      tag = StringUtils.join(tag, index);
      String field = MapUtils.getString(fieldMap, "field");
      String operator = MapUtils.getString(fieldMap, "operator");
      Object operand = fieldMap.get("operand");

      if (StringUtils.equals(field, "network_id")) {
        whereSql.append(" and has(network_id, :network_id)=1 ");
        params.put("network_id", operand);
        continue;
      }

      if (StringUtils.equals(field, "service_id")) {
        whereSql.append(" and has(service_id, :service_id)=1 ");
        params.put("service_id", operand);
        continue;
      }

      if (StringUtils.equals(field, "application_id")) {
        whereSql.append(String.format(" and application_id %s :application_id%s ", operator, tag));
        params.put("application_id" + tag, operand);
        continue;
      }

      if (StringUtils.equals(field, "category_id")) {
        whereSql.append(
            String.format(" and application_category_id %s :category_id%s ", operator, tag));
        params.put("category_id" + tag, operand);
        continue;
      }

      if (StringUtils.equals(field, "subcategory_id")) {
        whereSql.append(
            String.format(" and application_subcategory_id %s :subcategory_id%s ", operator, tag));
        params.put("subcategory_id" + tag, operand);
        continue;
      }

      if (StringUtils.equals(field, "ip_protocol")) {
        whereSql.append(String.format(" and ip_protocol %s :ip_protocol%s ", operator, tag));
        params.put("ip_protocol" + tag, operand);
        queryVO.setIpProtocol(String.valueOf(operand));
        continue;
      }

      if (StringUtils.equals(field, "l7_protocol_id")) {
        whereSql.append(String.format(" and l7_protocol_id %s :l7_protocol_id%s ", operator, tag));
        params.put("l7_protocol_id" + tag, operand);
        continue;
      }

      if (StringUtils.equals(field, "ethernet_type")) {
        whereSql.append(String.format(" and ethernet_type %s :ethernet_type%s ", operator, tag));
        params.put("ethernet_type" + tag, operand);
        queryVO.setEthernetType(Integer.parseInt(String.valueOf(operand)));
        continue;
      }

      if (StringUtils.equals(field, "country_id")) {
        if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_LOCATION_UP)) {
          whereSql
              .append(String.format(" and country_id_responder %s :country_id%s ", operator, tag));
        } else if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_LOCATION_DOWN)) {
          whereSql
              .append(String.format(" and country_id_initiator %s :country_id%s ", operator, tag));
        } else {
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(String.format(
                " and (country_id_initiator != :country_id%s or isNull(country_id_initiator)) "
                    + "and (country_id_responder != :country_id%s or isNull(country_id_responder)) ",
                tag, tag));
          } else {
            whereSql.append(String.format(
                " and (country_id_initiator = :country_id%s or country_id_responder = :country_id%s) ",
                tag, tag));
          }
        }
        params.put("country_id" + tag, operand);
        continue;
      }

      if (StringUtils.equals(field, "province_id")) {
        if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_LOCATION_UP)) {
          whereSql.append(
              String.format(" and (province_id_responder %s :province_id%s ", operator, tag));
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(" or isNull(province_id_responder) ");
          }
          whereSql.append(") ");
        } else if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_LOCATION_DOWN)) {
          whereSql.append(
              String.format(" and (province_id_initiator %s :province_id%s ", operator, tag));
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(" or isNull(province_id_initiator) ");
          }
          whereSql.append(") ");
        } else {
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(String.format(
                " and (province_id_initiator != :province_id%s or isNull(province_id_initiator)) "
                    + "and (province_id_responder != :province_id%s or isNull(province_id_responder)) ",
                tag, tag));
          } else {
            whereSql.append(String.format(
                " and (province_id_initiator = :province_id%s or province_id_responder = :province_id%s) ",
                tag, tag));
          }
        }
        params.put("province_id" + tag, operand);
        continue;
      }

      if (StringUtils.equals(field, "city_id")) {
        if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_LOCATION_UP)) {
          whereSql.append(String.format(" and (city_id_responder %s :city_id%s ", operator, tag));
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(" or isNull(city_id_responder) ");
          }
          whereSql.append(") ");
        } else if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_LOCATION_DOWN)) {
          whereSql.append(String.format(" and (city_id_initiator %s :city_id%s ", operator, tag));
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(" or isNull(city_id_initiator) ");
          }
          whereSql.append(") ");
        } else {
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(String.format(
                " and (city_id_initiator != :city_id%s or isNull(city_id_initiator)) "
                    + "and (city_id_responder != :city_id%s or isNull(city_id_responder)) ",
                tag, tag));
          } else {
            whereSql.append(String.format(
                " and (city_id_initiator = :city_id%s or city_id_responder = :city_id%s) ", tag,
                tag));
          }
        }
        params.put("city_id" + tag, operand);
        continue;
      }

      if (StringUtils.equals(field, "port")) {
        if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_PORT_INITIATOR)) {
          whereSql.append(String.format(" and port_initiator %s :port%s ", operator, tag));
        } else if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_PORT_RESPONDER)) {
          whereSql.append(String.format(" and port_responder %s :port%s ", operator, tag));
        } else {
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(String
                .format(" and port_initiator != :port%s and port_responder != :port%s ", tag, tag));
          } else {
            whereSql.append(
                String.format(" and (port_initiator %s :port%s or port_responder %s :port%s) ",
                    operator, tag, operator, tag));
          }
        }
        params.put("port" + tag, operand);
        continue;
      }

      if (StringUtils.equals(field, "mac_address")) {
        if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_MAC_INITIATOR)) {
          whereSql
              .append(String.format(" and ethernet_initiator %s :mac_address%s ", operator, tag));
        } else if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_MAC_RESPONDER)) {
          whereSql
              .append(String.format(" and ethernet_responder %s :mac_address%s ", operator, tag));
        } else {
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(String.format(
                " and ethernet_initiator != :mac_address%s and ethernet_responder != :mac_address%s ",
                tag, tag));
          } else {
            whereSql.append(String.format(
                " and (ethernet_initiator %s :mac_address%s or ethernet_responder %s :mac_address%s) ",
                operator, tag, operator, tag));
          }
        }
        params.put("mac_address" + tag, operand);
        queryVO.setMacAddress(String.valueOf(operand));
        continue;
      }

      if (StringUtils.equals(field, "ip_address")) {
        String ip = String.valueOf(operand);
        if (!NetworkUtils.isInetAddress(ip) && !NetworkUtils.isCidr(ip)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "请输入正确的IP地址");
        }

        if (NetworkUtils.isInetAddress(ip)) {
          // 单IP
          Tuple2<String,
              String> ipVersion = NetworkUtils.isInetAddress(ip, IpVersion.V4)
                  ? Tuples.of("v4", "toIPv4")
                  : Tuples.of("v6", "toIPv6");
          if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_IP_INITIATOR)) {
            whereSql.append(String.format(" and (ip%s_initiator %s %s(:ip_address%s) ",
                ipVersion.getT1(), operator, ipVersion.getT2(), tag));
            if (StringUtils.equals(operator, "!=")) {
              whereSql.append(String.format(" or isNull(ip%s_initiator) ", ipVersion.getT1()));
            }
            whereSql.append(") ");
          } else if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_IP_RESPONDER)) {
            whereSql.append(String.format(" and (ip%s_responder %s %s(:ip_address%s) ",
                ipVersion.getT1(), operator, ipVersion.getT2(), tag));
            if (StringUtils.equals(operator, "!=")) {
              whereSql.append(String.format(" or isNull(ip%s_responder) ", ipVersion.getT1()));
            }
            whereSql.append(") ");
          } else {
            if (StringUtils.equals(operator, "!=")) {
              whereSql.append(String.format(
                  " and (ip%s_initiator != %s(:ip_address%s) or isNull(ip%s_initiator)) "
                      + "and (ip%s_responder != %s(:ip_address%s) or isNull(ip%s_responder)) ",
                  ipVersion.getT1(), ipVersion.getT2(), tag, ipVersion.getT1(), ipVersion.getT1(),
                  ipVersion.getT2(), tag, ipVersion.getT1()));
            } else {
              whereSql.append(String.format(
                  " and (ip%s_initiator = %s(:ip_address%s) or ip%s_responder = %s(:ip_address%s)) ",
                  ipVersion.getT1(), ipVersion.getT2(), tag, ipVersion.getT1(), ipVersion.getT2(),
                  tag));
            }
          }
          params.put("ip_address" + tag, operand);
        } else {
          // CIDR
          Tuple3<String, String,
              String> ipVersion = NetworkUtils.isCidr(ip, IpVersion.V4)
                  ? Tuples.of("v4", "IPv4CIDRToRange", "toIPv4")
                  : Tuples.of("v6", "IPv6CIDRToRange", "toIPv6");
          if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_IP_INITIATOR)) {
            whereSql.append(String.format(" and ((ip%s_initiator", ipVersion.getT1()));
            whereSql.append(StringUtils.equals(operator, "!=") ? " not " : "");
            whereSql.append(String.format(
                " between %s(%s(:ip_address%s), :cidr%s).1 and %s(%s(:ip_address%s), :cidr%s).2) ",
                ipVersion.getT2(), ipVersion.getT3(), tag, tag, ipVersion.getT2(),
                ipVersion.getT3(), tag, tag));
            if (StringUtils.equals(operator, "!=")) {
              whereSql.append(String.format(" or isNull(ip%s_initiator) ", ipVersion.getT1()));
            }
            whereSql.append(") ");
          } else if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_IP_RESPONDER)) {
            whereSql.append(String.format(" and ((ip%s_responder", ipVersion.getT1()));
            whereSql.append(StringUtils.equals(operator, "!=") ? " not " : "");
            whereSql.append(String.format(
                " between %s(%s(:ip_address%s), :cidr%s).1 and %s(%s(:ip_address%s), :cidr%s).2) ",
                ipVersion.getT2(), ipVersion.getT3(), tag, tag, ipVersion.getT2(),
                ipVersion.getT3(), tag, tag));
            if (StringUtils.equals(operator, "!=")) {
              whereSql.append(String.format(" or isNull(ip%s_responder) ", ipVersion.getT1()));
            }
            whereSql.append(") ");
          } else {
            whereSql.append(String.format(" and ((ip%s_initiator", ipVersion.getT1()));
            whereSql.append(StringUtils.equals(operator, "!=") ? " not " : "");
            whereSql.append(String.format(
                " between %s(%s(:ip_address%s), :cidr%s).1 and %s(%s(:ip_address%s), :cidr%s).2) ",
                ipVersion.getT2(), ipVersion.getT3(), tag, tag, ipVersion.getT2(),
                ipVersion.getT3(), tag, tag));
            whereSql.append(StringUtils.equals(operator, "!=")
                ? String.format(" or isNull(ip%s_initiator)) and ", ipVersion.getT1())
                : " or ");

            whereSql.append(String.format(" ((ip%s_responder", ipVersion.getT1()));
            whereSql.append(StringUtils.equals(operator, "!=") ? " not " : "");
            whereSql.append(String.format(
                " between %s(%s(:ip_address%s), :cidr%s).1 and %s(%s(:ip_address%s), :cidr%s).2) ",
                ipVersion.getT2(), ipVersion.getT3(), tag, tag, ipVersion.getT2(),
                ipVersion.getT3(), tag, tag));
            whereSql.append(StringUtils.equals(operator, "!=")
                ? String.format(" or isNull(ip%s_responder)) ", ipVersion.getT1())
                : ")) ");
          }

          String[] ipAndCidr = ip.split("/");
          params.put("ip_address" + tag, ipAndCidr[0]);
          params.put("cidr" + tag, Integer.parseInt(ipAndCidr[1]));
        }

        continue;
      }

      if (StringUtils.equals(field, "hostgroup_id")) {
        if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_HOSTGROUP_INITIATOR)) {
          whereSql.append(
              String.format(" and hostgroup_id_initiator %s :hostgroup_id%s ", operator, tag));
        } else if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_HOSTGROUP_RESPONDER)) {
          whereSql.append(
              String.format(" and hostgroup_id_responder %s :hostgroup_id%s ", operator, tag));
        } else {
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(String.format(
                " and (hostgroup_id_initiator != :hostgroup_id%s or isNull(hostgroup_id_initiator)) "
                    + "and (hostgroup_id_responder != :hostgroup_id%s or isNull(hostgroup_id_responder)) ",
                tag, tag));
          } else {
            whereSql.append(String.format(
                " and (hostgroup_id_initiator = :hostgroup_id%s or hostgroup_id_responder = :hostgroup_id%s) ",
                tag, tag));
          }
        }
        params.put("hostgroup_id" + tag, operand);
        continue;
      }

      if (StringUtils.equals(field, "ip_locality")) {
        if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_IP_INITIATOR)) {
          whereSql.append(
              String.format(" and ip_locality_initiator %s :ip_locality%s ", operator, tag));
        } else if (StringUtils.equals(queryVO.getFilterType(), FILTER_SOURCE_IP_RESPONDER)) {
          whereSql.append(
              String.format(" and ip_locality_responder %s :ip_locality%s ", operator, tag));
        } else if (StringUtils.equalsAny(queryVO.getFilterType(), FILTER_SOURCE_LOCATION_UP,
            FILTER_SOURCE_LOCATION_DOWN)) {
          // 地区统计是IP位置过滤条件忽略
        } else {
          if (StringUtils.equals(operator, "!=")) {
            whereSql.append(String.format(
                " and ip_locality_initiator != :ip_locality%s and ip_locality_responder != :ip_locality%s ",
                tag, tag));
          } else {
            whereSql.append(String.format(
                " and (ip_locality_initiator = :ip_locality%s or ip_locality_responder = :ip_locality%s) ",
                tag, tag));
          }
        }
        params.put("ip_locality" + tag, operand);
        queryVO.setIpLocality(Integer.parseInt(String.valueOf(operand)));
      }
    }
  }

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, StringBuilder whereSql,
      Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    // 日志开始时间在查询时间范围中
    whereSql.append(" and report_time >= toDateTime64(:start_time, 9, 'UTC') ");
    whereSql.append(" and report_time < toDateTime64(:end_time, 9, 'UTC') ");
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  private void enrichKeySql(List<Map<String, Object>> keyConditions, StringBuilder whereSql,
      Map<String, Object> params) {
    // 添加附加条件
    if (CollectionUtils.isNotEmpty(keyConditions)) {
      whereSql.append(" and ( ");

      List<String> conditions = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      int index = 0;
      for (Map<String, Object> condition : keyConditions) {
        StringBuilder conditionSql = new StringBuilder("(");
        Iterator<Entry<String, Object>> iterator = condition.entrySet().iterator();
        while (iterator.hasNext()) {
          Entry<String, Object> entry = iterator.next();
          if (entry.getValue() == null) {
            if (StringUtils.equalsAny(entry.getKey(), "ipv4_initiator", "ipv4_responder",
                "ipv6_initiator", "ipv6_responder")) {
              // ip类key值空值判断无实际意义并且影响查询
              conditionSql.append("1=1");
            } else {
              conditionSql.append("isNull(").append(entry.getKey()).append(")");
            }
          } else if (NetworkUtils.isInetAddress(String.valueOf(entry.getValue()))) {
            String ip = (String) entry.getValue();
            conditionSql.append(entry.getKey()).append(" = ")
                .append(NetworkUtils.isInetAddress(ip, IpVersion.V4) ? "toIPv4(:" : "toIPv6(:")
                .append(entry.getKey()).append(index).append(")");
            params.put(entry.getKey() + index, ip);
          } else {
            conditionSql.append(entry.getKey()).append(" = :").append(entry.getKey()).append(index);
            params.put(entry.getKey() + index, entry.getValue());
          }

          if (iterator.hasNext()) {
            conditionSql.append(" and ");
          }
        }
        conditionSql.append(")");
        conditions.add(conditionSql.toString());
        index++;
      }

      whereSql.append(StringUtils.join(conditions, " or ")).append(")");
    }
  }

  private List<Map<String, Object>> metricDataConversion(List<Map<String, Object>> metricResult) {
    if (CollectionUtils.isEmpty(metricResult)) {
      return metricResult;
    }

    metricResult.forEach(metricData -> {
      metricData.put("tcpClientNetworkLatencyAvg",
          StringUtils.equalsAny(String.valueOf(metricData.get("tcpClientNetworkLatencyAvg")),
              DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                  : new BigDecimal(String.valueOf(metricData.get("tcpClientNetworkLatencyAvg")))
                      .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      metricData.put("tcpServerNetworkLatencyAvg",
          StringUtils.equalsAny(String.valueOf(metricData.get("tcpServerNetworkLatencyAvg")),
              DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                  : new BigDecimal(String.valueOf(metricData.get("tcpServerNetworkLatencyAvg")))
                      .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      metricData.put("serverResponseLatencyAvg",
          StringUtils.equalsAny(String.valueOf(metricData.get("serverResponseLatencyAvg")),
              DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                  : new BigDecimal(String.valueOf(metricData.get("serverResponseLatencyAvg")))
                      .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      metricData.put("tcpClientRetransmissionRate",
          StringUtils.equalsAny(String.valueOf(metricData.get("tcpClientRetransmissionRate")),
              DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                  : new BigDecimal(String.valueOf(metricData.get("tcpClientRetransmissionRate")))
                      .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      metricData.put("tcpServerRetransmissionRate",
          StringUtils.equalsAny(String.valueOf(metricData.get("tcpServerRetransmissionRate")),
              DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                  : new BigDecimal(String.valueOf(metricData.get("tcpServerRetransmissionRate")))
                      .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
    });

    return metricResult;
  }

  private List<Map<String, Object>> metricDateHistogramDataConversion(
      List<Map<String, Object>> metricResult) {
    if (CollectionUtils.isEmpty(metricResult)) {
      return metricResult;
    }

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

  private List<Map<String, Object>> getInitiatorKeyAndAggsList(
      List<Map<String, Object>> metricList) {
    List<Map<String, Object>> initiatorKeyList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    initiatorKeyList = metricList.stream().map(metricData -> {
      Map<String, Object> key = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) metricData.get("ipv4Address");
      key.put("ipv4_initiator", ipv4 == null ? null : ipv4.getHostAddress());
      Inet6Address ipv6 = (Inet6Address) metricData.get("ipv6Address");
      key.put("ipv6_initiator", ipv6 == null ? null : ipv6.getHostAddress());
      key.put("ip_locality_initiator", metricData.get("ipLocality"));

      return key;
    }).collect(Collectors.toList());
    return initiatorKeyList;
  }

  private List<Map<String, Object>> getResponderKeyAndAggsList(
      List<Map<String, Object>> metricList) {
    List<Map<String, Object>> responderKeyList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    responderKeyList = metricList.stream().map(metricData -> {
      Map<String, Object> key = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) metricData.get("ipv4Address");
      key.put("ipv4_responder", ipv4 == null ? null : ipv4.getHostAddress());
      Inet6Address ipv6 = (Inet6Address) metricData.get("ipv6Address");
      key.put("ipv6_responder", ipv6 == null ? null : ipv6.getHostAddress());
      key.put("ip_locality_responder", metricData.get("ipLocality"));

      return key;
    }).collect(Collectors.toList());
    return responderKeyList;
  }
}
