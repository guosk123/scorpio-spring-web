package com.machloop.fpc.manager.system.task;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.system.service.DeviceNetifService;
import com.machloop.fpc.manager.system.service.SystemMetricService;

/**
 * @author liumeng
 *
 * create at 2018年12月17日, fpc-manager
 */
@Component
public class ServerMetricTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerMetricTask.class);

  @Autowired
  private DeviceNetifService deviceNetifService;

  @Autowired
  private SystemMetricService monitorMetricService;

  // 已移交sysmonitor组件
  // @Scheduled(cron = "${task.monitor.schedule.cron}")
  public void run() {
    int update = 0;

    Date metricDatetime = DateUtils.now();

    // 定时采集管理口上下行流量、包数，写入RRD
    update = deviceNetifService.statisticNetifUsage(metricDatetime);
    LOGGER.debug("Statistic netif count={}.", update);

    // 定时采集CPU内存使用率，写入RRD
    update = monitorMetricService.statisticCpuMemUsage(metricDatetime);
    LOGGER.debug("Statistic cpu memory count={}.", update);
  }

}
