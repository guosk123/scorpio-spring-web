package com.machloop.fpc.cms.center.system.task;

import java.util.Date;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.central.service.CentralSystemService;
import com.machloop.fpc.cms.center.central.service.CentralNetifService;

@Component
public class ServerMetricTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerMetricTask.class);

  @Autowired
  private CentralNetifService centralNetifService;

  @Autowired
  private CentralSystemService centralSystemService;

  @Scheduled(cron = "${task.monitor.schedule.cron}")
  public void run() {
    // 系统默认部署在linux
    if (!SystemUtils.IS_OS_LINUX) {
      LOGGER.debug("finish monitor system state because os is not linux.");
      return;
    }

    int update = 0;

    Date metricDatetime = DateUtils.now();

    // 监控网卡状态
    update = centralNetifService.statisticCentralNetifs(metricDatetime);
    LOGGER.debug("statistic netif data count={}.", update);

    // 监控系统状态（CPU、内存、磁盘状态）
    update = centralSystemService.statisticCentralSystem(metricDatetime);
    LOGGER.debug("statistic cpu memory data count={}.", update);

  }

}
