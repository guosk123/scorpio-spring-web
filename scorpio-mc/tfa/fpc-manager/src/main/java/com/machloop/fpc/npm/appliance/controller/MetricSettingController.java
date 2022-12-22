package com.machloop.fpc.npm.appliance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.npm.appliance.bo.MetricSettingBO;
import com.machloop.fpc.npm.appliance.service.MetricSettingService;

/**
 * @author guosk
 *
 * create at 2021年4月6日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class MetricSettingController {

  @Autowired
  private MetricSettingService metricSettingService;

  @GetMapping("/metric-settings")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricSettings(
      @RequestParam(name = "sourceType") String sourceType, String networkId, String serviceId,
      String packetFileId) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    metricSettingService.queryMetricSettings(sourceType, networkId, serviceId, packetFileId)
        .forEach(metricSetting -> {
          Map<String, Object> metricSettingMap = Maps
              .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          metricSettingMap.put("metric", metricSetting.getMetric());
          metricSettingMap.put("value", metricSetting.getValue());

          result.add(metricSettingMap);
        });

    return result;
  }

  @PutMapping("/metric-settings")
  @Secured({"PERM_USER"})
  public void updateMetricSettings(@RequestParam("metricSettings") String metricSettings) {
    List<MetricSettingBO> metricSettingList = JsonHelper.deserialize(metricSettings,
        new TypeReference<List<MetricSettingBO>>() {
        }, false);

    metricSettingService.updateMetricSettings(metricSettingList,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("修改统计分析指标配置：" + metricSettings);
  }

}
