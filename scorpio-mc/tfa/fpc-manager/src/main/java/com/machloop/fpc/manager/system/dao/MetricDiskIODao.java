package com.machloop.fpc.manager.system.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.manager.system.data.MetricDiskIODO;

/**
 * @author guosk
 *
 * create at 2021年10月26日, fpc-manager
 */
public interface MetricDiskIODao {

  List<MetricDiskIODO> queryMetricDiskIOHistograms(Date startTime, Date endTime, int interval,
      String partitionName);
  
  List<MetricDiskIODO> queryMetricDiskIOs(Date afterTime);
}
