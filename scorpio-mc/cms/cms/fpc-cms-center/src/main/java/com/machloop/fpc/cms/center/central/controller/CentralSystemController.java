package com.machloop.fpc.cms.center.central.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.central.bo.CentralSystemBO;
import com.machloop.fpc.cms.center.central.service.CentralSystemService;

@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/central")
public class CentralSystemController {

  @Autowired
  private CentralSystemService centralSystemService;

  @GetMapping("/devices/{deviceSerialNumber}/system")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryCentralSystem(
      @PathVariable @NotEmpty(message = "设备类型不能为空") String deviceType,
      @PathVariable @NotEmpty(message = "设备编号不能为空") String deviceSerialNumber) {

    return systemToMap(centralSystemService.queryCentralSystem(deviceType,deviceSerialNumber), true);
  }

  @GetMapping("/devices/{deviceSerialNumber}/system/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryCentralSystems(
      @PathVariable @NotEmpty(message = "设备类型不能为空") String deviceType,
      @PathVariable @NotEmpty(message = "设备编号不能为空") String deviceSerialNumber,
      @RequestParam(name = "interval", required = false, defaultValue = "30") String interval,
      @RequestParam @NotEmpty(message = "查询开始时间不能为空") String startTime,
      @RequestParam @NotEmpty(message = "查询结束时间不能为空") String endTime) {

    Date startTimeDate = DateUtils.parseISO8601Date(startTime);
    Date endTimeDate = DateUtils.parseISO8601Date(endTime);
    List<CentralSystemBO> systems = centralSystemService.queryCentralSystems(deviceType,deviceSerialNumber,
        Integer.parseInt(interval), startTimeDate, endTimeDate);

    List<Map<String, Object>> systemList = Lists.newArrayListWithExpectedSize(systems.size());
    systems.forEach(system -> systemList.add(systemToMap(system, false)));

    return systemList;
  }

  @GetMapping("/systems/as-usage-ranking")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryStorageSpaceUsageByRanking(
      @RequestParam(required = false, defaultValue = "5") int number) {

    return centralSystemService.queryStorageSpaceUsagesByRanking(number);
  }

  @GetMapping("/packet-oldest-time")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public Map<String, Object> queryMaxDataOldestTime(
      @RequestParam(required = false) String networkId,
      @RequestParam(required = false) String networkGroupId) {

    return centralSystemService.queryMaxDataOldestTime(networkId, networkGroupId);
  }

  private Map<String, Object> systemToMap(CentralSystemBO centralSystemBO, boolean isDetail) {
    Map<String, Object> systemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    systemMap.put("id", centralSystemBO.getId());
    systemMap.put("deviceType", centralSystemBO.getDeviceType());
    systemMap.put("monitoredSerialNumber", centralSystemBO.getMonitoredSerialNumber());
    systemMap.put("cpuMetric", centralSystemBO.getCpuMetric());
    systemMap.put("memoryMetric", centralSystemBO.getMemoryMetric());
    systemMap.put("metricTime", centralSystemBO.getMetricTime());
    if (isDetail) {
      systemMap.put("systemFsMetric", centralSystemBO.getSystemFsMetric());
      systemMap.put("indexFsMetric", centralSystemBO.getIndexFsMetric());
      systemMap.put("metadataFsMetric", centralSystemBO.getMetadataFsMetric());
      systemMap.put("packetFsMetric", centralSystemBO.getPacketFsMetric());
      systemMap.put("fsDataTotalByte", centralSystemBO.getFsDataTotalByte());
      systemMap.put("fsDataUsedPct", centralSystemBO.getFsDataUsedPct());
      systemMap.put("fsCacheTotalByte", centralSystemBO.getFsCacheTotalByte());
      systemMap.put("fsCacheUsedPct", centralSystemBO.getFsCacheUsedPct());
      systemMap.put("dataOldestTime", centralSystemBO.getDataOldestTime());
      systemMap.put("dataLast24TotalByte", centralSystemBO.getDataLast24TotalByte());
      systemMap.put("dataPredictTotalDay", centralSystemBO.getDataPredictTotalDay());
      systemMap.put("cacheFileAvgByte", centralSystemBO.getCacheFileAvgByte());
      systemMap.put("fsStoreTotalByte", centralSystemBO.getFsStoreTotalByte());
    }

    return systemMap;
  }

}
