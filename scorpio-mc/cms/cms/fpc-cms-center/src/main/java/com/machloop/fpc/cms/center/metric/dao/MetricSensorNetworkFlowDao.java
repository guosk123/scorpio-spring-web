package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年1月25日, fpc-cms-center
 */
public interface MetricSensorNetworkFlowDao {

  List<Map<String, Object>> querySensorNetworkHistogramByMetric(MetricSensorQueryVO queryVO,
      List<String> networkIdList);

  List<Map<String, Object>> queryAllNetworkMetrics(MetricSensorQueryVO queryVO);

  List<Map<String, Object>> queryEstablishedSuccessRateHistogram(MetricSensorQueryVO queryVO,
      List<String> networkIdList);

  List<Map<String, Object>> queryAllNetworkTcpEstablishedSessionCount(MetricSensorQueryVO queryVO);

}
