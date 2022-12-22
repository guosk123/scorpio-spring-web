package com.machloop.fpc.manager.appliance.controller;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.AlertMessageBO;
import com.machloop.fpc.manager.appliance.service.AlertMessageService;
import com.machloop.fpc.manager.appliance.vo.AlertMessageQueryVO;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class AlertMessageController {

  @Autowired
  private AlertMessageService alertMessageService;

  @GetMapping("/alert-messages")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryAlertMessages(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      AlertMessageQueryVO queryVO) {
    Sort sort = new Sort(new Order(Sort.Direction.DESC, "arise_time"),
        new Order(Sort.Direction.DESC, "solve_time"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO
          .setStartTime(DateUtils.toStringFormat(DateUtils.parseISO8601Date(queryVO.getStartTime()),
              "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTime(DateUtils.toStringFormat(DateUtils.parseISO8601Date(queryVO.getEndTime()),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (StringUtils.isNotBlank(queryVO.getSolveTimeBegin())) {
      queryVO.setSolveTimeBegin(
          DateUtils.toStringFormat(DateUtils.parseISO8601Date(queryVO.getSolveTimeBegin()),
              "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (StringUtils.isNotBlank(queryVO.getSolveTimeEnd())) {
      queryVO.setSolveTimeEnd(
          DateUtils.toStringFormat(DateUtils.parseISO8601Date(queryVO.getSolveTimeEnd()),
              "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    Page<AlertMessageBO> alertMessages = alertMessageService.queryAlertMessages(page, queryVO);

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(alertMessages.getSize());
    for (AlertMessageBO alertMessage : alertMessages) {
      resultList.add(alertMessageBO2Map(alertMessage));
    }

    return new PageImpl<>(resultList, page, alertMessages.getTotalElements());
  }

  @GetMapping("/alert-messages/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryAlertMessage(
      @NotEmpty(message = "告警ID不能为空") @PathVariable String id) {
    AlertMessageBO alertMessageBO = alertMessageService.queryAlertMessage(id);

    return alertMessageBO2Map(alertMessageBO);
  }

  @GetMapping("/alert-messages/as-count")
  @Secured({"PERM_USER"})
  public long countAlertMessage(String startTime, String endTime,
      @RequestParam(name = "networkId", required = false) String networkId,
      @RequestParam(name = "serviceId", required = false) String serviceId) {
    Date startTimeDate = null;
    if (StringUtils.isNotBlank(startTime)) {
      startTimeDate = DateUtils.parseISO8601Date(startTime);
    }
    Date endTimeDate = null;
    if (StringUtils.isNotBlank(endTime)) {
      endTimeDate = DateUtils.parseISO8601Date(endTime);
    }

    return alertMessageService.countAlertMessages(startTimeDate, endTimeDate, networkId, serviceId);
  }

  @GetMapping("/alert-messages/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryAlertMessageAsHistogram(AlertMessageQueryVO queryVO) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return alertMessageService.queryAlertMessageAsHistogram(queryVO);
  }

  @GetMapping("/alert-messages/as-analysis")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> analysisAlertMessage(String startTime, String endTime,
      String metrics,
      @RequestParam(name = "interval", defaultValue = "60", required = false) int interval,
      @RequestParam(name = "sourceType", required = false) String sourceType,
      @RequestParam(name = "sourceValue", required = false) String sourceValue,
      @RequestParam(name = "networkId", required = false) String networkId,
      @RequestParam(name = "serviceId", required = false) String serviceId) {
    Date startTimeDate = DateUtils.parseISO8601Date(startTime);
    Date endTimeDate = DateUtils.parseISO8601Date(endTime);

    return alertMessageService.analysisAlertMessage(startTimeDate, endTimeDate, interval, metrics,
        sourceType, sourceValue, networkId, serviceId);
  }

  @PutMapping("/alert-messages/{id}/solve")
  @Secured({"PERM_USER"})
  public void solveAlertMessage(@PathVariable("id") @NotEmpty(message = "告警ID不能为空") String id,
      @RequestParam(required = false, defaultValue = "") String reason) {
    AlertMessageBO alertMessageBO = alertMessageService.solveAlertMessage(id, reason,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, alertMessageBO);
  }

  private static Map<String, Object> alertMessageBO2Map(AlertMessageBO alertMessage) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", alertMessage.getId());
    map.put("alertId", alertMessage.getAlertId());
    map.put("networkId", alertMessage.getNetworkId());
    map.put("serviceId", alertMessage.getServiceId());
    map.put("name", alertMessage.getName());
    map.put("category", alertMessage.getCategory());
    map.put("level", alertMessage.getLevel());
    map.put("alertDefine", alertMessage.getAlertDefine());
    map.put("components", alertMessage.getComponents());
    map.put("ariseTime", alertMessage.getAriseTime());
    map.put("status", alertMessage.getStatus());
    map.put("solver", alertMessage.getSolver());
    map.put("solveTime", alertMessage.getSolveTime());
    map.put("reason", alertMessage.getReason());

    return map;
  }

}
