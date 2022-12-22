package com.machloop.fpc.npm.metric.dao;

import java.util.List;
import java.util.Map;
import com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月26日, fpc-manager
 */
public interface MetricIpDao {
  List<Map<String, Object>> querytransmitIngestBytesDataAggregate(MetricNetflowQueryVO queryVO);

  List<Map<String, Object>> queryMetricNetflowIndex(MetricNetflowQueryVO queryVO);
  
  List<Map<String, Object>> queryIpDashboardHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData);

  List<Map<String, Object>> queryIpTable(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData);

  List<Map<String, Object>> queryTransmitIpDashboardHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData);

  List<Map<String, Object>> queryTransmitIpTable(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryTransmitIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData);

  List<Map<String, Object>> queryIngestIpDashboardHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData);

  List<Map<String, Object>> queryIngestIpTable(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryIngestIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData);

}
