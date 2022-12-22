package com.machloop.fpc.manager.system.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.fpc.manager.system.service.MonitorMetricDataService;


/**
 * @author liyongjun
 *
 * create at 2019年9月17日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-v1/system")
public class MonitorMetricDataController {

  @Autowired
  private MonitorMetricDataService monitorMetricService;

  @GetMapping("/runtime-environments")
  @Secured({"PERM_USER", "PERM_SYS_USER", "PERM_AUDIT_USER"})
  public Map<String, Object> queryRuntimeEnvironment() {
    return monitorMetricService.queryRuntimeEnvironment();
  }

  @GetMapping("/monitor-metrics/as-histogram")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> statMonitorMetric(@RequestParam String startTime,
      @RequestParam String endTime,
      @RequestParam(required = false, defaultValue = "60") int interval) {
    return monitorMetricService.statMonitorMetricData(startTime, endTime, interval);
  }
}
