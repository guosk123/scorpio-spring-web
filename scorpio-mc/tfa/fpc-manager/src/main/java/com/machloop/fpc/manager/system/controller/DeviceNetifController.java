package com.machloop.fpc.manager.system.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.statistics.bo.TimeseriesBO;
import com.machloop.fpc.manager.system.bo.DeviceNetifBO;
import com.machloop.fpc.manager.system.service.DeviceNetifService;
import com.machloop.fpc.manager.system.vo.DeviceNetifModificationVO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/system")
public class DeviceNetifController {

  @Autowired
  private DeviceNetifService deviceNetifService;

  @GetMapping("/device-netifs")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> queryDeviceNetifs() {

    List<DeviceNetifBO> deviceNetifList = deviceNetifService.queryDeviceNetifsWithBandwidth();
    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(deviceNetifList.size());
    for (DeviceNetifBO deviceNetifBO : deviceNetifList) {
      resultList.add(deviceNetifBO2Map(deviceNetifBO));
    }

    return resultList;
  }

  /**
   * 读取rrd中记录的管理口流量
   * @param netifName
   * @param category
   * @param interval
   * @param startTime
   * @param endTime
   * @return
   */
  @Deprecated
  @GetMapping("/device-netifs/{netifName}/as-histogram")
  @Secured({"PERM_SYS_USER"})
  public Map<String, Map<String, Object>> queryDeviceManagementPortNetifUsages(
      @PathVariable @NotEmpty(message = "接口名称不能为空") String netifName, @RequestParam String category,
      @RequestParam(name = "interval", required = false, defaultValue = "60") String interval,
      @RequestParam @NotEmpty(message = "查询开始时间不能为空") String startTime,
      @RequestParam @NotEmpty(message = "查询结束时间不能为空") String endTime) {

    Map<String, Map<String, Object>> resultMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 前端传入的netifName为“网口名”
    Date startTimeDate = DateUtils.parseISO8601Date(startTime);
    Date endTimeDate = DateUtils.parseISO8601Date(endTime);

    String txBytepsRrdName = netifName + FpcConstants.STAT_NETIF_RRD_TX_BYTEPS;
    String txPpsRrdName = netifName + FpcConstants.STAT_NETIF_RRD_TX_PPS;
    String rxBytepsRrdName = netifName + FpcConstants.STAT_NETIF_RRD_RX_BYTEPS;
    String rxPpsRrdName = netifName + FpcConstants.STAT_NETIF_RRD_RX_PPS;

    resultMap.put("txBps", timeseriesBO2Map(
        deviceNetifService.queryNetifUsage(txBytepsRrdName, interval, startTimeDate, endTimeDate)));
    resultMap.put("txPps", timeseriesBO2Map(
        deviceNetifService.queryNetifUsage(txPpsRrdName, interval, startTimeDate, endTimeDate)));
    resultMap.put("rxBps", timeseriesBO2Map(
        deviceNetifService.queryNetifUsage(rxBytepsRrdName, interval, startTimeDate, endTimeDate)));
    resultMap.put("rxPps", timeseriesBO2Map(
        deviceNetifService.queryNetifUsage(rxPpsRrdName, interval, startTimeDate, endTimeDate)));

    return resultMap;
  }

  /**
   * 修改接口用途
   * @param netifListJson
   */
  @PutMapping("/device-netifs")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public void updateDeviceNetifs(
      @RequestParam @NotEmpty(message = "业务口信息不能为空") String netifListJson) {

    List<DeviceNetifModificationVO> netifList = JsonHelper.deserialize(netifListJson,
        new TypeReference<List<DeviceNetifModificationVO>>() {
        }, false);
    List<DeviceNetifBO> netifBOList = Lists.newArrayListWithCapacity(netifList.size());
    for (DeviceNetifModificationVO netifVO : netifList) {
      DeviceNetifBO deviceNetifBO = new DeviceNetifBO();
      BeanUtils.copyProperties(netifVO, deviceNetifBO);

      netifBOList.add(deviceNetifBO);
    }

    netifBOList = deviceNetifService.batchUpdateDeviceNetifs(netifBOList,
        LoggedUserContext.getCurrentUser().getId());

    StringBuilder logContent = new StringBuilder("修改接口设置：");
    netifBOList.forEach(deviceNetifBO -> logContent
        .append(deviceNetifBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE)));

    LogHelper.auditOperate(logContent.toString());
  }

  private static Map<String, Object> deviceNetifBO2Map(DeviceNetifBO deviceNetif) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", deviceNetif.getId());
    map.put("name", deviceNetif.getName());
    map.put("state", deviceNetif.getState());
    map.put("category", deviceNetif.getCategory());
    map.put("categoryText", deviceNetif.getCategoryText());
    map.put("type", deviceNetif.getType());
    map.put("typeText", deviceNetif.getTypeText());
    map.put("ipv4Address", deviceNetif.getIpv4Address());
    map.put("ipv6Address", deviceNetif.getIpv6Address());
    map.put("ipv4Gateway", deviceNetif.getIpv4Gateway());
    map.put("ipv6Gateway", deviceNetif.getIpv6Gateway());
    map.put("bandwidth", deviceNetif.getBandwidth());
    map.put("specification", deviceNetif.getSpecification());
    map.put("description", deviceNetif.getDescription());
    map.put("updateTime", deviceNetif.getUpdateTime());
    map.put("metricTime", deviceNetif.getMetricTime());
    map.put("useMessage", deviceNetif.getUseMessage());

    return map;
  }

  private static Map<String, Object> timeseriesBO2Map(TimeseriesBO timeseries) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("startTime", timeseries.getStartTime());
    map.put("endTime", timeseries.getEndTime());
    map.put("startPoint", timeseries.getStartPoint());
    map.put("endPoint", timeseries.getEndPoint());
    map.put("dataPoint", timeseries.getDataPoint());

    return map;
  }

}
