package com.machloop.fpc.manager.system.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.system.data.MonitorMetricDataDO;

/**
 * @author liyongjun
 *
 * create at 2019年9月16日, fpc-manager
 */
public interface MonitorMetricDataService {

  Map<String, Object> queryRuntimeEnvironment();

  List<Map<String, Object>> statMonitorMetricData(String startTime, String endTime, int interval);

  List<Map<String, Object>> queryLatestStatMonitorMetricData();

  MonitorMetricDataDO produceMonitorMetricData();
}
