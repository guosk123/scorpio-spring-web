package com.machloop.fpc.manager.restapi;

import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.SystemServerIpService;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.AlertMessageBO;
import com.machloop.fpc.manager.appliance.service.AlertMessageService;
import com.machloop.fpc.manager.appliance.vo.AlertMessageQueryVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author guosk
 *
 * create at 2021年8月16日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class AlertMessageRestAPIController {

  private static String serverAddress;

  @Autowired
  private AlertMessageService alertMessageService;

  @Autowired
  private SystemServerIpService systemServerIpService;

  @Autowired
  private UserService userService;

  @GetMapping("/alert-messages")
  @RestApiSecured
  public RestAPIResultVO queryAlertMessages(@Validated AlertMessageQueryVO queryVO) {
    if (StringUtils.isAnyBlank(queryVO.getStartTime(), queryVO.getEndTime())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("查询时间段不能为空")
          .build();
    }

    try {
      queryVO
          .setStartTime(DateUtils.toStringFormat(DateUtils.parseISO8601Date(queryVO.getStartTime()),
              "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      queryVO.setEndTime(DateUtils.toStringFormat(DateUtils.parseISO8601Date(queryVO.getEndTime()),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    } catch (DateTimeParseException e) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("时间格式不合法")
          .build();
    }

    List<AlertMessageBO> alertMessages = alertMessageService.queryAlertMessages(queryVO);
    List<Map<String, Object>> resultList = alertMessages.stream()
        .map(alertMessage -> alertMessageBO2Map(alertMessage)).collect(Collectors.toList());

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    resultMap.put("data", resultList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  @PutMapping("/alert-messages/{id}/solve")
  @RestApiSecured
  public RestAPIResultVO solveAlertMessage(
      @PathVariable("id") @NotEmpty(message = "告警ID不能为空") String id,
      @RequestParam(required = false, defaultValue = "") String reason,
      HttpServletRequest request) {
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    // 解决告警
    AlertMessageBO alertMessageBO = null;
    try {
      alertMessageBO = alertMessageService.solveAlertMessage(id, reason, userBO.getId());
    } catch (BusinessException e) {
      return RestAPIResultVO.resultFailed(e);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, alertMessageBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(alertMessageBO);
  }

  private Map<String, Object> alertMessageBO2Map(AlertMessageBO alertMessage) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    map.put("timestamp", alertMessage.getAriseTime());
    map.put("networkId", alertMessage.getNetworkId());
    map.put("serviceId", alertMessage.getServiceId());
    map.put("name", alertMessage.getName());
    map.put("category", alertMessage.getCategory());
    map.put("level", alertMessage.getLevel());
    // 告警定义
    parseAlertDefine(map, alertMessage.getAlertDefine());
    // 告警组件
    List<Map<String, Object>> oldComponents = JsonHelper.deserialize(alertMessage.getComponents(),
        new TypeReference<List<Map<String, Object>>>() {
        }, false);
    List<Map<String, Object>> newComponents = oldComponents.stream().map(component -> {
      Map<String, Object> item = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      // 组件告警定义
      parseAlertDefine(item, JsonHelper.serialize(component.get("alertDefine")));
      // 组件触发内容
      String alertFireContext = JsonHelper.serialize(component.get("alertFireContext"));
      if (StringUtils.isNotBlank(alertFireContext)) {
        Map<String, Object> alertFireContextMap = JsonHelper.deserialize(alertFireContext,
            new TypeReference<Map<String, Object>>() {
            }, false);
        item.put("alertFireContext", alertFireContextMap);
      }

      return item;
    }).collect(Collectors.toList());
    map.put("components", newComponents);

    return map;
  }

  private void parseAlertDefine(Map<String, Object> map, String alertDefine) {
    if (StringUtils.isBlank(alertDefine)) {
      return;
    }

    JsonNode alertDefineJsonNode = JsonHelper.deserialize(alertDefine, JsonNode.class);

    map.put("name", alertDefineJsonNode.get("name").asText());
    map.put("category", alertDefineJsonNode.get("category").asText());
    map.put("level", alertDefineJsonNode.get("level").asText());

    // setting
    JsonNode settings = null;
    switch (alertDefineJsonNode.get("category").asText()) {
      case FpcConstants.ALERT_CATEGORY_THRESHOLD:
        settings = alertDefineJsonNode.get("thresholdSettings");
        break;
      case FpcConstants.ALERT_CATEGORY_TREND:
        settings = alertDefineJsonNode.get("trendSettings");
        break;
      case FpcConstants.ALERT_CATEGORY_ADVANCED:
        settings = alertDefineJsonNode.get("advancedSettings");
        break;
      default:
        break;
    }
    if (settings != null) {
      Map<String, Object> settingMap = JsonHelper.deserialize(settings,
          new TypeReference<Map<String, Object>>() {
          }, false);
      map.put("settings", settingMap);
    }

    // refire
    JsonNode refireJsonNode = alertDefineJsonNode.get("refire");
    if (refireJsonNode != null) {
      Map<String, Object> refireMap = JsonHelper.deserialize(refireJsonNode.toString(),
          new TypeReference<Map<String, Object>>() {
          }, false);
      map.put("refire", refireMap);
    }
  }

}
