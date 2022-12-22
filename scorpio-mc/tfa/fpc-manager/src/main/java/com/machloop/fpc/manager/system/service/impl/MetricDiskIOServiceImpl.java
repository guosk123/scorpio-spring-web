package com.machloop.fpc.manager.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.manager.system.dao.MetricDiskIODao;
import com.machloop.fpc.manager.system.data.MetricDiskIODO;
import com.machloop.fpc.manager.system.service.MetricDiskIOService;

/**
 * @author guosk
 *
 * create at 2021年10月26日, fpc-manager
 */
@Service
public class MetricDiskIOServiceImpl implements MetricDiskIOService {

  @Autowired
  private MetricDiskIODao metricDiskIODao;

  /**
   * @see com.machloop.fpc.manager.system.service.MetricDiskIOService#queryMetricDiskIOHistograms(java.util.Date, java.util.Date, int, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricDiskIOHistograms(Date startTime, Date endTime,
      int interval, String partitionName) {
    List<MetricDiskIODO> diskIOHistograms = metricDiskIODao.queryMetricDiskIOHistograms(startTime,
        endTime, interval, partitionName);

    return diskIOHistograms.stream().map(item -> {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.put("timestamp", item.getTimestamp());
      map.put("partitionName", item.getPartitionName());
      map.put("readByteps", item.getReadByteps());
      map.put("readBytepsPeak", item.getReadBytepsPeak());
      map.put("writeByteps", item.getWriteByteps());
      map.put("writeBytepsPeak", item.getWriteBytepsPeak());

      return map;
    }).collect(Collectors.toList());
  }

}
