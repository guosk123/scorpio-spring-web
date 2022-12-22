package com.machloop.fpc.manager.system.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.manager.system.bo.MonitorMetricBO;
import com.machloop.fpc.manager.system.service.SystemMetricService;

/**
 * 
 * @author liumeng
 *
 * create at 2018年12月14日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-v1/system")
public class MonitorMetricController {

  @Autowired
  private SystemMetricService monitorMetricService;

  @GetMapping("/monitor-metrics")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> queryMonitorMetrics() {

    List<MonitorMetricBO> monitorMetricList = monitorMetricService.queryMonitorMetrics();
    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(monitorMetricList.size());
    for (MonitorMetricBO monitorMetric : monitorMetricList) {
      Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      resultMap.put("metricName", monitorMetric.getMetricName());
      resultMap.put("metricValue", monitorMetric.getMetricValue());
      resultMap.put("metricTime", monitorMetric.getMetricTime());

      resultList.add(resultMap);
    }

    return resultList;
  }

}
