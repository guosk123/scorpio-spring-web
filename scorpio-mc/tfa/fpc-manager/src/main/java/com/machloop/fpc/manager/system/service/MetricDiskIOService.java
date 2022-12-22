package com.machloop.fpc.manager.system.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author guosk
 *
 * create at 2021年10月26日, fpc-manager
 */
public interface MetricDiskIOService {

  List<Map<String, Object>> queryMetricDiskIOHistograms(Date startTime, Date endTime, int interval,
      String partitionName);

}
