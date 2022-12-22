package com.machloop.fpc.cms.center.system.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.central.bo.CentralSystemBO;
import com.machloop.fpc.cms.center.central.service.CentralSystemService;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.center.system.service.SystemMetricService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@RestController
@RequestMapping("/webapi/fpc-cms-v1/system")
public class MonitorMetricController {

  @Autowired
  private CentralSystemService centralSystemService;

  @Autowired
  private SystemMetricService systemMetricService;

  @Autowired
  private LicenseService licenseService;

  @GetMapping("/runtime-environments")
  @Secured({"PERM_USER", "PERM_SYS_USER", "PERM_AUDIT_USER"})
  public Map<String, Object> queryRuntimeEnvironment() {
    return systemMetricService.queryRuntimeEnvironment();
  }

  @GetMapping("/monitor-metrics")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public Map<String, Object> queryMonitorMetrics() {

    CentralSystemBO centralSystem = centralSystemService.queryCentralSystem(
        FpcCmsConstants.DEVICE_TYPE_CMS, licenseService.queryDeviceSerialNumber());
    return systemToMap(centralSystem);
  }

  @GetMapping("/monitor-metrics/as-histogram")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> queryCpuMemUsages(
      @RequestParam(name = "interval", required = false, defaultValue = "30") String interval,
      @RequestParam @NotEmpty(message = "查询开始时间不能为空") String startTime,
      @RequestParam @NotEmpty(message = "查询结束时间不能为空") String endTime) {

    Date startTimeDate = DateUtils.parseISO8601Date(startTime);
    Date endTimeDate = DateUtils.parseISO8601Date(endTime);

    List<CentralSystemBO> systems = centralSystemService.queryCentralSystems(
        FpcCmsConstants.DEVICE_TYPE_CMS, licenseService.queryDeviceSerialNumber(),
        Integer.parseInt(interval), startTimeDate, endTimeDate);

    List<Map<String, Object>> systemList = Lists.newArrayListWithExpectedSize(systems.size());
    systems.forEach(system -> systemList.add(systemToMap(system)));

    return systemList;
  }

  private Map<String, Object> systemToMap(CentralSystemBO centralSystemBO) {
    Map<String, Object> systemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    systemMap.put("id", centralSystemBO.getId());
    systemMap.put("deviceType", centralSystemBO.getDeviceType());
    systemMap.put("monitoredSerialNumber", centralSystemBO.getMonitoredSerialNumber());
    systemMap.put("cpuMetric", centralSystemBO.getCpuMetric());
    systemMap.put("memoryMetric", centralSystemBO.getMemoryMetric());
    systemMap.put("systemFsMetric", centralSystemBO.getSystemFsMetric());
    systemMap.put("metricTime", centralSystemBO.getMetricTime());

    return systemMap;
  }
}
