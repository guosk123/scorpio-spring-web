package com.machloop.fpc.npm.metric.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月26日, fpc-manager
 */
public interface MetricNetflowHistogramService {
  
  Map<String, Object> queryNetflowDashboard(MetricNetflowQueryVO queryVO);
  
  List<Map<String, Object>> queryMetricIpHistogram(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);
  
  List<Map<String, Object>> querySessionRecordIpHistogram(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricTransmitIpHistogram(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);
  
  List<Map<String, Object>> querySessionRecordTransmitIpHistogram(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricIngestIpHistogram(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);
  
  List<Map<String, Object>> querySessionRecordIngestIpHistogram(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);
  
  List<Map<String, Object>> querySessionRecordSessionHistogram(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricProtocolPortHistogram(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection);
  
  List<Map<String, Object>> querySessionRecordProtocolPortHistogram(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection);
}
