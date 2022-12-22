package com.machloop.fpc.cms.center.appliance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
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
import com.machloop.fpc.cms.center.appliance.bo.BaselineSettingBO;
import com.machloop.fpc.cms.center.appliance.service.BaselineService;

/**
 * @author guosk
 *
 * create at 2021年4月21日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class BaselineSettingController {

  @Autowired
  private BaselineService baselineService;

  @GetMapping("/baseline-settings")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryBaselineSettings(
      @RequestParam(name = "sourceType") String sourceType,
      @RequestParam(name = "networkId", required = false) String networkId,
      @RequestParam(name = "networkGroupId", required = false) String networkGroupId,
      String serviceId) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    baselineService.queryBaselineSettings(sourceType, networkId, networkGroupId, serviceId)
        .forEach(baselineSetting -> {
          Map<String, Object> baselineSettingMap = Maps
              .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          baselineSettingMap.put("sourceType", baselineSetting.getSourceType());
          baselineSettingMap.put("networkId", baselineSetting.getNetworkId());
          baselineSettingMap.put("networkGroupId", baselineSetting.getNetworkGroupId());
          baselineSettingMap.put("serviceId", baselineSetting.getServiceId());
          baselineSettingMap.put("category", baselineSetting.getCategory());
          baselineSettingMap.put("weightingModel", baselineSetting.getWeightingModel());
          baselineSettingMap.put("windowingModel", baselineSetting.getWindowingModel());
          baselineSettingMap.put("windowingCount", baselineSetting.getWindowingCount());
          baselineSettingMap.put("updateTime", baselineSetting.getUpdateTime());

          result.add(baselineSettingMap);
        });

    return result;
  }

  @PutMapping("/baseline-settings")
  @Secured({"PERM_USER"})
  public void updateBaselineSettings(@RequestParam("baselineSettings") String baselineSettings) {
    List<BaselineSettingBO> baselineSettingList = JsonHelper.deserialize(baselineSettings,
        new TypeReference<List<BaselineSettingBO>>() {
        }, false);

    baselineService.updateBaselineSettings(baselineSettingList,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("修改基线配置：" + baselineSettings);
  }

}
