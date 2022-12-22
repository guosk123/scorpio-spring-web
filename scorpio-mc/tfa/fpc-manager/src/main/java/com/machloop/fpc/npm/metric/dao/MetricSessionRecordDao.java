package com.machloop.fpc.npm.metric.dao;

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
public interface MetricSessionRecordDao {

  List<Map<String, Object>> queryIpTable(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData);

  List<Map<String, Object>> queryTransmitIpTable(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryTransmitIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData);

  List<Map<String, Object>> queryIngestIpTable(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryIngestIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData);

  List<Map<String, Object>> queryProtocolportTable(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection);

  List<Map<String, Object>> queryProtocolportTableHistogram(MetricNetflowQueryVO queryVO,
      List<String> topData);

  List<Map<String, Object>> querySessionTable(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection);

  List<Map<String, Object>> querySessionTableHistogram(MetricNetflowQueryVO queryVO,
      List<String> topData);
  
  Page<Map<String, Object>> querySessionRecords(String queryId, Pageable page, MetricNetflowQueryVO queryVO,
      List<String> sessionIds);

  long countSessionRecords(String queryId, MetricNetflowQueryVO queryVO);
  
  
}
