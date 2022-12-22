package com.machloop.fpc.cms.center.metric.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2022年2月16日, fpc-cms-center
 */
public interface MetricInSecondService {

  public static final String METRIC_NETWORK = "network";
  public static final String METRIC_SERVICE = "service";
  public static final String METRIC_PAYLOAD = "payload";
  public static final String METRIC_PERFORMANCE = "performance";
  public static final String METRIC_TCP = "tcp";

  boolean asyncCollection(MetricQueryVO queryVO, String metricType, String path,
      HttpServletRequest request);

  Map<String, Object> queryNetworkDashboard(MetricQueryVO queryVO);

  Map<String, Object> queryServiceDashboard(MetricQueryVO queryVO);

  List<Map<String, Object>> queryPayloadStatistics(MetricQueryVO queryVO);

  List<Map<String, Object>> queryPerformanceStatistics(MetricQueryVO queryVO);

  List<Map<String, Object>> queryTcpStatistics(MetricQueryVO queryVO);

}
