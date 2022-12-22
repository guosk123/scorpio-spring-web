package com.machloop.fpc.cms.center.system.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.central.bo.CentralNetifBO;
import com.machloop.fpc.cms.center.central.service.CentralNetifService;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@RestController
@RequestMapping("/webapi/fpc-cms-v1/system")
public class DeviceNetifController {

  @Autowired
  private CentralNetifService centralNetifService;

  @Autowired
  private LicenseService licenseService;

  @GetMapping("/device-netifs")
  @Secured({"PERM_SYS_USER"})
  public List<Map<String, Object>> querySystemNetifs() {

    List<CentralNetifBO> systemNetifProfiles = centralNetifService.queryCentralNetifProfiles(
        FpcCmsConstants.DEVICE_TYPE_CMS, licenseService.queryDeviceSerialNumber(),
        FpcCmsConstants.DEVICE_NETIF_CATEGORY_MGMT, FpcCmsConstants.DEVICE_NETIF_CATEGORY_INGEST,
        FpcCmsConstants.DEVICE_NETIF_CATEGORY_TRANSMIT);

    List<Map<String, Object>> netifProfileList = Lists
        .newArrayListWithExpectedSize(systemNetifProfiles.size());
    systemNetifProfiles
        .forEach(netifProfile -> netifProfileList.add(netifToMap(netifProfile, true)));

    return netifProfileList;
  }

  @GetMapping("/device-netifs/{netifName}/as-histogram")
  @Secured({"PERM_SYS_USER"})
  public List<Map<String, Object>> querySystemNetifTraffics(@PathVariable String netifName,
      @RequestParam(name = "interval", required = false, defaultValue = "30") String interval,
      @RequestParam @NotEmpty(message = "查询开始时间不能为空") String startTime,
      @RequestParam @NotEmpty(message = "查询结束时间不能为空") String endTime) {

    Date startTimeDate = DateUtils.parseISO8601Date(startTime);
    Date endTimeDate = DateUtils.parseISO8601Date(endTime);

    List<CentralNetifBO> netifFlows = centralNetifService.queryCentralNetifs(
        FpcCmsConstants.DEVICE_TYPE_CMS, licenseService.queryDeviceSerialNumber(), netifName, null,
        Integer.parseInt(interval), startTimeDate, endTimeDate);

    List<Map<String, Object>> netifFlowList = Lists.newArrayListWithExpectedSize(netifFlows.size());
    netifFlows.forEach(netif -> netifFlowList.add(netifToMap(netif, false)));

    return netifFlowList;
  }

  private Map<String, Object> netifToMap(CentralNetifBO centralNetifBO, boolean isOnlyProfiles) {
    Map<String, Object> netifMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    netifMap.put("id", centralNetifBO.getId());
    netifMap.put("monitoredSerialNumber", centralNetifBO.getMonitoredSerialNumber());
    netifMap.put("netifName", centralNetifBO.getNetifName());
    netifMap.put("category", centralNetifBO.getCategory());

    if (isOnlyProfiles) {
      netifMap.put("state", centralNetifBO.getState());
      netifMap.put("specification", centralNetifBO.getSpecification());
    } else {
      netifMap.put("rxBps", centralNetifBO.getRxBps());
      netifMap.put("txBps", centralNetifBO.getTxBps());
      netifMap.put("rxPps", centralNetifBO.getRxPps());
      netifMap.put("txPps", centralNetifBO.getTxPps());
      netifMap.put("metricTime", centralNetifBO.getMetricTime());
    }

    return netifMap;
  }

}
