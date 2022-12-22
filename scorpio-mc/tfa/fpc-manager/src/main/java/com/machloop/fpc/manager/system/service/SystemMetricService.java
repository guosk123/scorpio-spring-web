package com.machloop.fpc.manager.system.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.statistics.bo.TimeseriesBO;
import com.machloop.fpc.manager.system.bo.MonitorMetricBO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public interface SystemMetricService {

  Map<String, Object> queryDeviceCustomInfo();
  
  List<MonitorMetricBO> queryMonitorMetrics();
  
  Map<String, TimeseriesBO> queryCpuMemUsages(String interval, Date startTimeDate, Date endTimeDate);

  int produceMonitorMetric(Date metricDatetime);

  int statisticCpuMemUsage(Date metricTime);

}
