package com.machloop.fpc.manager.system.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.system.service.MetricDiskIOService;

/**
 * @author guosk
 *
 * create at 2021年10月26日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-v1/system")
public class MetricDiskIOController {

  @Autowired
  private MetricDiskIOService metricDiskIOService;

  @GetMapping("/io-metrics/as-histogram")
  @Secured({"PERM_SYS_USER"})
  public List<Map<String, Object>> queryMetricDiskIOHistograms(@RequestParam String startTime,
      @RequestParam String endTime,
      @RequestParam(required = false, defaultValue = "60") int interval,
      @RequestParam(required = false, defaultValue = "") String partitionName) {
    Date startTimeDate = null;
    if (StringUtils.isNotBlank(startTime)) {
      startTimeDate = DateUtils.parseISO8601Date(startTime);
    }

    Date endTimeDate = null;
    if (StringUtils.isNotBlank(endTime)) {
      endTimeDate = DateUtils.parseISO8601Date(endTime);
    }

    return metricDiskIOService.queryMetricDiskIOHistograms(startTimeDate, endTimeDate, interval,
        partitionName);
  }

}
