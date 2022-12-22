package com.machloop.fpc.npm.metric.service;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月26日, fpc-manager
 */
public interface MetricNetflowTableService {
  List<Map<String, Object>> queryMetricIp(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);
  
  List<Map<String, Object>> querySessionRecordIp(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricTransmitIp(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);
  
  List<Map<String, Object>> querySessionRecordTransmitIp(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricIngestIp(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);
  
  List<Map<String, Object>> querySessionRecordIngestIp(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);
  
  List<Map<String, Object>> querySessionRecordSession(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricProtocolPort(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection);
  
  List<Map<String, Object>> querySessionRecordProtocolPort(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection);

  Page<Map<String, Object>> querySessionRecords(String queryId, MetricNetflowQueryVO queryVO,
      Pageable page);

  Map<String, Object> querySessionRecordTotalElement(String queryId, MetricNetflowQueryVO queryVO);

}
