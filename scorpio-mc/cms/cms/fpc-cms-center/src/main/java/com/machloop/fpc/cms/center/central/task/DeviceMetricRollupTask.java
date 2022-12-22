package com.machloop.fpc.cms.center.central.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.fpc.cms.center.central.service.CentralNetifService;
import com.machloop.fpc.cms.center.central.service.CentralSystemService;

@Component
public class DeviceMetricRollupTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeviceMetricRollupTask.class);

  @Autowired
  private CentralNetifService centralNetifService;

  @Autowired
  private CentralSystemService centralSystemService;

  @Scheduled(cron = "${task.monitor.schedule.cron}")
  public void run() {

    // 聚合网卡流量数据
    LOGGER.debug("rollup netif traffic data.");
    centralNetifService.rollupCentralNetifs();

    // 聚合系统CPU、内存数据
    LOGGER.debug("rollup device metric data.");
    centralSystemService.rollupCentralSystem();

  }

}
