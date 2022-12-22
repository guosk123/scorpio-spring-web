package com.machloop.fpc.npm.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月26日, fpc-manager
 */
public interface MetricProtocolPortDao {
  List<Map<String, Object>> queryProtocolPortDashboardHistogram(MetricNetflowQueryVO queryVO,
      List<String> topData);

  List<Map<String, Object>> queryProtocolPortTable(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection);

  List<Map<String, Object>> queryProtocolPortTableHistogram(MetricNetflowQueryVO queryVO,
      List<String> topData);

}
