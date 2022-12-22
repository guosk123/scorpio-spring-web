package com.machloop.fpc.manager.system.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.fpc.manager.system.service.DeviceDiskService;
import com.machloop.fpc.manager.system.service.DeviceNetifService;

/**
 * @author liumeng
 *
 * create at 2018年12月17日, fpc-manager
 */
@Component
public class MonitorMetricTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorMetricTask.class);

  @Autowired
  private DeviceNetifService deviceNetifService;

  @Autowired
  private DeviceDiskService deviceDiskService;

  @Scheduled(fixedRateString = "${task.monitor.schedule.fixedrate.ms}")
  public void run() {
    int update = 0;

    // 监控CPU、内存、各个分区使用率，根据告警配置产生告警
    // 已移交sysmonitor组件
    /*update = monitorMetricService.produceMonitorMetric(DateUtils.now());
    LOGGER.debug("Monitor metric count={}.", update);*/

    // 监控管理口状态
    update = deviceNetifService.monitorNetifState();
    LOGGER.debug("Detect netif count={}.", update);

    // 监控磁盘RAID状态，状态异常时产生告警
    update = deviceDiskService.monitorDiskState();
    LOGGER.debug("Detect disk count={}.", update);
  }

}
