package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author guosk
 *
 * create at 2021年5月25日, fpc-manager
 */
public interface MetricFlowlogDataRecordDao {

  List<Map<String, Object>> queryMetricNetworks(MetricFlowLogQueryVO queryVO, String sortProperty,
      String sortDirection, boolean onlyAggSortProperty);

  List<Map<String, Object>> queryMetricNetworkHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<String> networkIds);

  List<Map<String, Object>> queryMetricLocations(MetricFlowLogQueryVO queryVO, String sortProperty,
      String sortDirection, boolean onlyAggSortProperty);

  List<Map<String, Object>> queryMetricLocationHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions);

  List<Map<String, Object>> queryMetricApplications(MetricFlowLogQueryVO queryVO,
      Tuple2<String, String> termField, String sortProperty, String sortDirection,
      boolean onlyAggSortProperty);

  List<Map<String, Object>> queryMetricApplicationHistograms(MetricFlowLogQueryVO queryVO,
      Tuple2<String, String> termField, String aggsField, List<String> termValues);

  List<Map<String, Object>> queryMetricL7Protocols(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty);

  List<Map<String, Object>> queryMetricL7ProtocolHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<String> l7ProtocolIds);

  List<Map<String, Object>> queryMetricPorts(MetricFlowLogQueryVO queryVO, String sortProperty,
      String sortDirection, boolean onlyAggSortProperty);

  List<Map<String, Object>> queryMetricPortHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<String> ports);

  List<Map<String, Object>> queryMetricHostGroups(MetricFlowLogQueryVO queryVO, String sortProperty,
      String sortDirection, boolean onlyAggSortProperty);

  List<Map<String, Object>> queryMetricHostGroupHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<String> hostgroupIds);

  List<Map<String, Object>> queryMetricL2Devices(MetricFlowLogQueryVO queryVO, String sortProperty,
      String sortDirection, boolean onlyAggSortProperty);

  List<Map<String, Object>> queryMetricL2DeviceHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<String> macAddress);

  List<Map<String, Object>> queryMetricL3Devices(MetricFlowLogQueryVO queryVO, String sortProperty,
      String sortDirection, boolean onlyAggSortProperty);

  List<Map<String, Object>> queryMetricL3DeviceHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions);

  List<Map<String, Object>> queryMetricIpConversations(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty);

  List<Map<String, Object>> queryMetricIpConversationHistograms(MetricFlowLogQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions);

  List<Map<String, Object>> graphMetricIpConversations(MetricFlowLogQueryVO queryVO,
      Integer minEstablishedSessions, Integer minTotalBytes, String sortProperty,
      String sortDirection);

  Page<Map<String, Object>> queryFlowLogsAsHistogram(MetricFlowLogQueryVO queryVO, PageRequest page,
      String queryProperty);

  long countFlowLogsStatistics(MetricFlowLogQueryVO queryVO, String queryProperty);
}
