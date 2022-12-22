package com.machloop.fpc.cms.center.broker.controller;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.cms.center.broker.bo.CollectMetricBO;
import com.machloop.fpc.cms.center.broker.service.subordinate.CollectMetricService;

@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/broker")
public class CollectMetricController {

  @Autowired
  private CollectMetricService collectMetricService;

  @GetMapping("/collect-metrics")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryCollectMetrics(
      @RequestParam @NotEmpty(message = "设备类型不能为空") String deviceType,
      @RequestParam @NotEmpty(message = "设备编号不能为空") String serialNumber,
      @RequestParam @NotEmpty(message = "数据统计类型不能为空") @Range(
          min = 0, max = 1, message = "数据统计类型不合法") @Digits(
              integer = 1, fraction = 0, message = "数据统计类型不合法") String type,
      @RequestParam @NotEmpty(message = "统计开始时间不能为空") String startTime,
      @RequestParam @NotEmpty(message = "统计结束时间不能为空") String endTime) {

    List<CollectMetricBO> collectMetrics = collectMetricService.queryCollectMetrics(deviceType,
        serialNumber, type, startTime, endTime);

    List<Map<String, Object>> collectMetricList = Lists
        .newArrayListWithExpectedSize(collectMetrics.size());
    collectMetrics
        .forEach(collectMetric -> collectMetricList.add(collectMetricToMap(collectMetric)));

    return collectMetricList;
  }

  private Map<String, Object> collectMetricToMap(CollectMetricBO collectMetricBO) {
    Map<String,
        Object> collectMetricMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    collectMetricMap.put("deviceType", collectMetricBO.getDeviceType());
    collectMetricMap.put("serialNumber", collectMetricBO.getDeviceSerialNumber());
    collectMetricMap.put("startTime", collectMetricBO.getStartTime());
    collectMetricMap.put("endTime", collectMetricBO.getEndTime());
    collectMetricMap.put("type", collectMetricBO.getType());
    collectMetricMap.put("collectAmount", collectMetricBO.getCollectAmount());
    collectMetricMap.put("entityAmount", collectMetricBO.getEntityAmount());

    return collectMetricMap;
  }

}
