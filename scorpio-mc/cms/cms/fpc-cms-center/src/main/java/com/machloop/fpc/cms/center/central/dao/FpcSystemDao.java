package com.machloop.fpc.cms.center.central.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.central.data.CentralSystemDO;
import com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO;

/**
 * 探针系统状态统计
 * @author guosk
 *
 * create at 2022年5月5日, fpc-cms-center
 */
public interface FpcSystemDao {

  CentralSystemDO queryLatestFpcSystemMetrics(String serialNumber);

  List<Map<String, Object>> queryFpcSystemHistogramByMetric(MetricSensorQueryVO queryVO,
      List<String> topSerialNumberList);

  List<Map<String, Object>> queryFpcSystemByMetric(MetricSensorQueryVO queryVO,
      List<String> tfaSerialNumberList);

  List<Map<String, Object>> queryFpcFreeSpaceMetric(MetricSensorQueryVO queryVO,
      List<String> tfaSerialNumberList);
}
