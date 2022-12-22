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
import com.machloop.fpc.cms.center.central.bo.CentralNetifBO;
import com.machloop.fpc.cms.center.central.bo.CentralNetifUsage;
import com.machloop.fpc.cms.center.central.service.CentralNetifService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/central")
public class CentralNetifController {

  @Autowired
  private CentralNetifService centralNetifService;

  @GetMapping("/devices/{deviceSerialNumber}/netif")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetifProfiles(
      @PathVariable @NotEmpty(message = "设备类型不能为空") String deviceType,
      @PathVariable @NotEmpty(message = "设备编号不能为空") String deviceSerialNumber) {

    List<CentralNetifBO> centralNetifProfiles = centralNetifService.queryCentralNetifProfiles(
        deviceType, deviceSerialNumber, FpcCmsConstants.DEVICE_NETIF_CATEGORY_MGMT,
        FpcCmsConstants.DEVICE_NETIF_CATEGORY_INGEST,
        FpcCmsConstants.DEVICE_NETIF_CATEGORY_TRANSMIT);

    List<Map<String, Object>> netifProfileList = Lists
        .newArrayListWithExpectedSize(centralNetifProfiles.size());
    centralNetifProfiles
        .forEach(netifProfile -> netifProfileList.add(netifToMap(netifProfile, true)));

    return netifProfileList;
  }

  /**
   * 查询设备管理口流量
   * @param deviceSerialNumber
   * @param interval
   * @param startTime
   * @param endTime
   * @return
   */
  @GetMapping("/devices/{deviceSerialNumber}/management-netif/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryManagementNetifs(
      @PathVariable @NotEmpty(message = "设备类型不能为空") String deviceType,
      @PathVariable @NotEmpty(message = "设备编号不能为空") String deviceSerialNumber,
      @RequestParam(name = "interval", required = false, defaultValue = "30") String interval,
      @RequestParam @NotEmpty(message = "查询开始时间不能为空") String startTime,
      @RequestParam @NotEmpty(message = "查询结束时间不能为空") String endTime) {

    Date startTimeDate = DateUtils.parseISO8601Date(startTime);
    Date endTimeDate = DateUtils.parseISO8601Date(endTime);
    // 查询FPC设备管理口流量
    List<CentralNetifBO> netifFlows = centralNetifService.queryCentralNetifs(deviceType,
        deviceSerialNumber, null, Lists.newArrayList(FpcCmsConstants.DEVICE_NETIF_CATEGORY_MGMT),
        Integer.parseInt(interval), startTimeDate, endTimeDate);

    List<Map<String, Object>> netifFlowList = Lists.newArrayListWithExpectedSize(netifFlows.size());
    netifFlows.forEach(netif -> netifFlowList.add(netifToMap(netif, false)));

    return netifFlowList;
  }

  /**
   * 设备业务口流量统计（以接口为单位，或以设备为单位）
   * @param deviceSerialNumber
   * @param interval
   * @param startTime
   * @param endTime
   * @return
   */
  @GetMapping("/netifs/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetifs(
      @PathVariable @NotEmpty(message = "设备类型不能为空") String deviceType,
      @NotEmpty(message = "设备编号不能为空") @RequestParam(
          required = false, defaultValue = "") String deviceSerialNumber,
      @RequestParam(name = "interval", required = false, defaultValue = "30") String interval,
      @NotEmpty(message = "查询开始时间不能为空") @RequestParam String startTime,
      @NotEmpty(message = "查询结束时间不能为空") @RequestParam String endTime) {

    Date startTimeDate = DateUtils.parseISO8601Date(startTime);
    Date endTimeDate = DateUtils.parseISO8601Date(endTime);

    List<CentralNetifBO> netifFlows = centralNetifService.queryCentralNetifs(deviceType,
        deviceSerialNumber, null,
        Lists.newArrayList(FpcCmsConstants.DEVICE_NETIF_CATEGORY_INGEST,
            FpcCmsConstants.DEVICE_NETIF_CATEGORY_TRANSMIT),
        Integer.parseInt(interval), startTimeDate, endTimeDate);

    List<Map<String, Object>> netifFlowList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    netifFlows.forEach(netif -> netifFlowList.add(netifToMap(netif, false)));

    return netifFlowList;
  }

  /**
   * 查询所有设备接收总流量
   * @param interval
   * @param startTime
   * @param endTime
   * @return
   */
  @GetMapping("/netifs/service-netif/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryTotalReceivingNetifs(
      @RequestParam(name = "interval", required = false, defaultValue = "30") String interval,
      @RequestParam @NotEmpty(message = "查询开始时间不能为空") String startTime,
      @RequestParam @NotEmpty(message = "查询结束时间不能为空") String endTime) {

    Date startTimeDate = DateUtils.parseISO8601Date(startTime);
    Date endTimeDate = DateUtils.parseISO8601Date(endTime);

    List<CentralNetifBO> receivingNetifs = centralNetifService
        .queryTotalReceivingNetifs(Integer.parseInt(interval), startTimeDate, endTimeDate);

    List<Map<String, Object>> receivingNetifList = Lists
        .newArrayListWithExpectedSize(receivingNetifs.size());
    for (CentralNetifBO CentralNetifBO : receivingNetifs) {
      Map<String,
          Object> receivingNetifMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      receivingNetifMap.put("rxBps", CentralNetifBO.getRxBps());
      receivingNetifMap.put("rxPps", CentralNetifBO.getRxPps());
      receivingNetifMap.put("metricTime", CentralNetifBO.getMetricTime());

      receivingNetifList.add(receivingNetifMap);
    }

    return receivingNetifList;
  }

  @GetMapping("/netifs/as-usage-ranking")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetifUsagesByRanking(
      @RequestParam(required = false, defaultValue = "5") int number) {

    List<CentralNetifUsage> netifUsages = centralNetifService.queryNetifUsagesByRanking(number);

    List<Map<String, Object>> netifUsageList = Lists
        .newArrayListWithExpectedSize(netifUsages.size());
    netifUsages.forEach(usage -> netifUsageList.add(netifUsageToMap(usage)));

    return netifUsageList;
  }

  private Map<String, Object> netifToMap(CentralNetifBO centralNetifBO, boolean isOnlyProfiles) {
    Map<String, Object> netifMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    netifMap.put("id", centralNetifBO.getId());
    netifMap.put("deviceType", centralNetifBO.getDeviceType());
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

  private Map<String, Object> netifUsageToMap(CentralNetifUsage centralNetifUsage) {
    Map<String, Object> netifUsageMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    netifUsageMap.put("deviceSerialNumber", centralNetifUsage.getDeviceSerialNumber());
    netifUsageMap.put("deviceName", centralNetifUsage.getDeviceName());
    netifUsageMap.put("netifName", centralNetifUsage.getNetifName());
    netifUsageMap.put("category", centralNetifUsage.getCategory());
    netifUsageMap.put("usagedBandwidth", centralNetifUsage.getUsagedBandwidth());
    netifUsageMap.put("totalBandwidth", centralNetifUsage.getTotalBandwidth());
    netifUsageMap.put("usage", centralNetifUsage.getUsage());

    return netifUsageMap;
  }

}
