package com.machloop.fpc.cms.center.central.controller;

import java.util.List;
import java.util.Map;

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
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Direction;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.alpha.webapp.system.bo.AlarmBO;
import com.machloop.alpha.webapp.system.bo.AlarmCountBO;
import com.machloop.alpha.webapp.system.bo.LogBO;
import com.machloop.alpha.webapp.system.vo.AlarmQueryVO;
import com.machloop.alpha.webapp.system.vo.LogQueryVO;
import com.machloop.fpc.cms.center.central.service.CentralMessageService;

@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/central")
public class CentralMessageController {

  @Autowired
  private CentralMessageService centralMessageService;

  @GetMapping("/logs")
  @Secured({"PERM_USER", "PERM_SYS_USER", "PERM_AUDIT_USER"})
  public Page<Map<String, Object>> queryLogs(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(
          name = "sortProperty", required = false, defaultValue = "arise_time") String sort,
      @RequestParam(
          name = "sortDirection", required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(name = "nodeType", required = false, defaultValue = "0") String nodeType,
      @Validated LogQueryVO query) {
    PageRequest page = new PageRequest(pageNumber, pageSize,
        new Sort(Sort.Direction.fromString(sortDirection), sort));

    Page<LogBO> logPage = centralMessageService.queryLogs(page,
        LoggedUserContext.getCurrentUser().getRoles(), query, nodeType);
    List<Map<String, Object>> logList = Lists.newArrayListWithExpectedSize(logPage.getNumber());
    logPage.forEach(log -> logList.add(logToMap(log)));

    return new PageImpl<>(logList, page, logPage.getTotalElements());
  }

  @GetMapping("/alarms")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public Page<Map<String, Object>> queryAlarms(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(
          name = "sortProperty", required = false, defaultValue = "arise_time") String sort,
      @RequestParam(
          name = "sortDirection", required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(name = "nodeType", required = false, defaultValue = "0") String nodeType,
      @Validated AlarmQueryVO query) {
    PageRequest page = new PageRequest(pageNumber, pageSize,
        new Sort(new Order(Direction.ASC, "status"),
            new Order(Sort.Direction.fromString(sortDirection), sort)));

    Page<AlarmBO> alarmPage = centralMessageService.queryAlarms(page, query, nodeType);
    List<Map<String, Object>> alarmList = Lists.newArrayListWithExpectedSize(alarmPage.getNumber());
    alarmPage.forEach(alarm -> alarmList.add(alarmToMap(alarm)));

    return new PageImpl<>(alarmList, page, alarmPage.getTotalElements());
  }

  @GetMapping("/alarms/group-by-level")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> countAlarmGroupByLevel() {
    List<AlarmCountBO> alarmCountBOList = centralMessageService.countAlarmsGroupByLevelWithoutCms();

    List<Map<String, Object>> alarmCountList = Lists
        .newArrayListWithExpectedSize(alarmCountBOList.size());
    for (AlarmCountBO alarmCountBO : alarmCountBOList) {
      Map<String,
          Object> alarmCountMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      alarmCountMap.put("level", alarmCountBO.getLevel());
      alarmCountMap.put("count", alarmCountBO.getCount());
      alarmCountList.add(alarmCountMap);
    }

    return alarmCountList;
  }

  private Map<String, Object> logToMap(LogBO logBO) {
    Map<String, Object> logMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    logMap.put("id", logBO.getId());
    logMap.put("nodeId", logBO.getNodeId());
    logMap.put("level", logBO.getLevel());
    logMap.put("category", logBO.getCategory());
    logMap.put("component", logBO.getComponent());
    logMap.put("content", logBO.getContent());
    logMap.put("source", logBO.getSource());
    logMap.put("ariseTime", logBO.getAriseTime());

    return logMap;
  }

  private Map<String, Object> alarmToMap(AlarmBO alarmBO) {
    Map<String, Object> alarmMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    alarmMap.put("id", alarmBO.getId());
    alarmMap.put("nodeId", alarmBO.getNodeId());
    alarmMap.put("level", alarmBO.getLevel());
    alarmMap.put("category", alarmBO.getCategory());
    alarmMap.put("keyword", alarmBO.getKeyword());
    alarmMap.put("solver", alarmBO.getSolver());
    alarmMap.put("status", alarmBO.getStatus());
    alarmMap.put("component", alarmBO.getComponent());
    alarmMap.put("content", alarmBO.getContent());
    alarmMap.put("reason", alarmBO.getReason());
    alarmMap.put("solveTime", alarmBO.getSolveTime());
    alarmMap.put("ariseTime", alarmBO.getAriseTime());

    return alarmMap;
  }

}
