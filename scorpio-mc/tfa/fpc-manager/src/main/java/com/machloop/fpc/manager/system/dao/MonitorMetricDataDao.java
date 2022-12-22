package com.machloop.fpc.manager.system.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.manager.system.data.MonitorMetricDataDO;

public interface MonitorMetricDataDao {

  List<MonitorMetricDataDO> queryMonitorMetricData(Date startTime, Date endTime, int interval);

  List<MonitorMetricDataDO> queryLatestMonitorMetricData();

  List<MonitorMetricDataDO> queryMonitorMetricData(Date afterTime);

  void saveMonitorMetricData(MonitorMetricDataDO monitorMetricDO);
}
