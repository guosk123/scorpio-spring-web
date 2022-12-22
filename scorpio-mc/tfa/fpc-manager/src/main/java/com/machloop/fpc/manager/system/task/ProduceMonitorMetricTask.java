package com.machloop.fpc.manager.system.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.machloop.fpc.manager.system.data.MonitorMetricDataDO;
import com.machloop.fpc.manager.system.service.MonitorMetricDataService;

/**
 * 
 * @author liyongjun
 *
 * create at 2019年9月16日, npb-manager
 */
@Component
public class ProduceMonitorMetricTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProduceMonitorMetricTask.class);

  @Autowired
  private MonitorMetricDataService monitorMetricDataService;

  // 已移交sysmonitor组件
  // @Scheduled(cron = "${task.monitor.schedule.cron}")
  public void run() {

    // 定时采集CPU、内存、分区使用率
    MonitorMetricDataDO metricDataDO = monitorMetricDataService.produceMonitorMetricData();

    LOGGER.debug("produce monitor metric data: {}.", metricDataDO);
  }

}
