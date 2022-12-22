package com.machloop.fpc.cms.center.metric.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年1月25日, fpc-cms-center
 */

public interface MetricSensorService {

  Map<String, Object> queryIndexData(MetricSensorQueryVO queryVO);

  List<Map<String, Object>> queryUsageRateTopTrend(MetricSensorQueryVO queryVO);
  
  List<Map<String, Object>> queryDiskIOTopTrend(MetricSensorQueryVO queryVO);

  List<Map<String, Object>> querySensorFreeSpace(MetricSensorQueryVO queryVO);

  List<Map<String, Object>> querySensorNetworkTopTrend(MetricSensorQueryVO queryVO);

  List<Map<String, Object>> queryEstablishSuccessRateTopTrend(MetricSensorQueryVO queryVO);
}
