package com.machloop.fpc.manager.system.dao;

import java.util.List;

import com.machloop.fpc.manager.system.data.MonitorMetricDO;

/**
 * @author liumeng
 *
 * create at 2018年12月14日, fpc-manager
 */
public interface MonitorMetricDao {

  List<MonitorMetricDO> queryMonitorMetrics();
  
  int saveOrUpdateMonitorMetric(MonitorMetricDO monitorMetricDO);
}
