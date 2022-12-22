package com.machloop.fpc.cms.center.central.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO;

/**
 * 探针硬盘分区IO统计
 * @author guosk
 *
 * create at 2021年12月7日, fpc-cms-center
 */
public interface FpcDiskIODao {

  List<Map<String, Object>> queryDiskIO(MetricSensorQueryVO queryVO,
      List<String> onlineTfaSerialNumberList);

  List<Map<String, Object>> queryDiskIOHistogram(MetricSensorQueryVO queryVO,
      List<String> topSerialNumberList);

}
