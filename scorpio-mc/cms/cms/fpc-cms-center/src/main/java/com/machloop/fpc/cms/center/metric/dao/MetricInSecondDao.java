package com.machloop.fpc.cms.center.metric.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import reactor.util.function.Tuple2;

/**
 * @author guosk
 *
 * create at 2022年2月17日, fpc-cms-center
 */
public interface MetricInSecondDao {

  List<Map<String, Object>> queryNetworkStatistics(Date startTime, List<String> networkIds);

  List<Map<String, Object>> queryServiceStatistics(Date startTime,
      List<Tuple2<String, String>> serviceNetworkIds);

  List<Map<String, Object>> queryPayloadStatistics(Date startTime, String sourceType,
      List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds);

  List<Map<String, Object>> queryPerformanceStatistics(Date startTime, String sourceType,
      List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds);

  List<Map<String, Object>> queryTcpStatistics(Date startTime, String sourceType,
      List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds);

  List<Map<String, Object>> queryDscpStatistics(Date startTime, String sourceType,
      List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds);

  Map<String, List<Map<String, Object>>> queryL3DeviceStatistics(Date timestamp, String sourceType,
      List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds);

  Map<String, List<Map<String, Object>>> queryIpConversationStatistics(Date timestamp,
      String sourceType, List<String> networkIds, List<Tuple2<String, String>> serviceNetworkIds);

  int batchSaveNetworkMetrics(List<Map<String, Object>> metricData, Date startTime);

  int batchSaveServiceMetrics(List<Map<String, Object>> metricData, Date startTime);

  int batchSavePayloadMetrics(List<Map<String, Object>> metricData, Date startTime,
      String networkId, String serviceId);

  int batchSavePerformanceMetrics(List<Map<String, Object>> metricData, Date startTime,
      String networkId, String serviceId);

  int batchSaveTcpMetrics(List<Map<String, Object>> metricData, Date startTime, String networkId,
      String serviceId);

  int batchSaveDscpMetrics(List<Map<String, Object>> metricData, Date startTime, String networkId,
      String serviceId);

  int saveL3DeviceMetric(Map<String, Object> metricData);

  int saveIpConversationMetric(Map<String, Object> metricData);

  void deleteExpireMetricData(Date expireDate);

}
