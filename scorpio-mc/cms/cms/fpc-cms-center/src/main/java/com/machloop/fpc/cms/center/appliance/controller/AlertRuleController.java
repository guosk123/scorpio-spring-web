package com.machloop.fpc.cms.center.appliance.controller;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.appliance.bo.AlertRuleBO;
import com.machloop.fpc.cms.center.appliance.service.AlertRuleService;
import com.machloop.fpc.cms.center.appliance.vo.AlertRuleCreationVO;
import com.machloop.fpc.cms.center.appliance.vo.AlertRuleModificationVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月19日, fpc-cms-center
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class AlertRuleController {

  @Autowired
  private AlertRuleService alertRuleService;

  @GetMapping("/alert-rules")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryAlertRules(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "category", required = false) String category,
      @RequestParam(name = "level", required = false) String level,
      @RequestParam(name = "networkId", required = false) String networkId,
      @RequestParam(name = "serviceId", required = false) String serviceId) {
    Sort sort = new Sort(new Order(Sort.Direction.DESC, "create_time"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    Page<AlertRuleBO> alertRules = alertRuleService.queryAlertRules(page, name, category, level,
        networkId, serviceId);

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(alertRules.getSize());
    for (AlertRuleBO alertRule : alertRules) {
      resultList.add(alertRuleBO2Map(alertRule));
    }

    return new PageImpl<>(resultList, page, alertRules.getTotalElements());
  }

  @GetMapping("/alert-rules/as-list")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryAllAlertRules(
      @RequestParam(name = "category", required = false) String category) {
    List<AlertRuleBO> alertRules = alertRuleService.queryAlertRulesByCategory(category);

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(alertRules.size());
    for (AlertRuleBO alertRule : alertRules) {
      resultList.add(alertRuleBO2Map(alertRule));
    }

    return resultList;
  }

  @GetMapping("/alert-rules/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryAlertRule(
      @NotEmpty(message = "警告配置ID不能为空") @PathVariable String id) {
    AlertRuleBO alertRuleBO = alertRuleService.queryAlertRule(id);

    return alertRuleBO2Map(alertRuleBO);
  }

  @PostMapping("/alert-rules")
  @Secured({"PERM_USER"})
  public void saveAlertRule(@Validated AlertRuleCreationVO alertRuleVO) {
    AlertRuleBO alertRuleBO = new AlertRuleBO();
    BeanUtils.copyProperties(alertRuleVO, alertRuleBO);

    alertRuleBO = alertRuleService.saveAlertRule(alertRuleBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, alertRuleBO);
  }

  @PutMapping("/alert-rules/{id}")
  @Secured({"PERM_USER"})
  public void updateAlertRule(@PathVariable @NotEmpty(message = "修改告警配置时传入的id不能为空") String id,
      @Validated AlertRuleModificationVO alertRuleVO) {
    AlertRuleBO alertRuleBO = new AlertRuleBO();
    BeanUtils.copyProperties(alertRuleVO, alertRuleBO);

    alertRuleBO = alertRuleService.updateAlertRule(id, alertRuleBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, alertRuleBO);
  }

  @PutMapping("/alert-rules/{id}/status")
  @Secured({"PERM_USER"})
  public void updateAlertRuleStatus(@PathVariable @NotEmpty(message = "重新开始任务时传入的id不能为空") String id,
      @RequestParam String status) {
    AlertRuleBO alertRuleBO = alertRuleService.updateAlertRuleStatus(id, status,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, alertRuleBO);
  }

  @DeleteMapping("/alert-rules/{id}")
  @Secured({"PERM_USER"})
  public void deleteAlertRule(@PathVariable @NotEmpty(message = "删除告警配置时传入的id不能为空") String id) {
    AlertRuleBO alertRuleBO = alertRuleService.deleteAlertRule(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, alertRuleBO);
  }

  private static Map<String, Object> alertRuleBO2Map(AlertRuleBO alertRuleBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", alertRuleBO.getId());
    map.put("name", alertRuleBO.getName());
    map.put("category", alertRuleBO.getCategory());
    map.put("level", alertRuleBO.getLevel());
    map.put("thresholdSettings", alertRuleBO.getThresholdSettings());
    map.put("trendSettings", alertRuleBO.getTrendSettings());
    map.put("advancedSettings", alertRuleBO.getAdvancedSettings());
    map.put("refire", alertRuleBO.getRefire());
    map.put("status", alertRuleBO.getStatus());
    map.put("networkIds", alertRuleBO.getNetworkIds());
    map.put("serviceIds", alertRuleBO.getServiceIds());
    map.put("description", alertRuleBO.getDescription());
    map.put("createTime", alertRuleBO.getCreateTime());

    return map;
  }

}
