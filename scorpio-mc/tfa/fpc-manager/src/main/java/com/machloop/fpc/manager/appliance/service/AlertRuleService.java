package com.machloop.fpc.manager.appliance.service;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.bo.AlertRuleBO;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
public interface AlertRuleService {

  Page<AlertRuleBO> queryAlertRules(Pageable page, String name, String category, String level,
      String networkId, String serviceId);

  List<String> queryAlertRulesBySource(String sourceType, String networkId, String serviceId);

  List<AlertRuleBO> queryAlertRulesByCategory(String category);

  AlertRuleBO queryAlertRule(String id);

  AlertRuleBO queryAlertRuleByCmsAlertRuleId(String cmsAlertRuleId);

  AlertRuleBO saveAlertRule(AlertRuleBO alertRuleBO, String operatorId);

  AlertRuleBO updateAlertRule(String id, AlertRuleBO alertRuleBO, String operatorId);

  AlertRuleBO updateAlertRuleStatus(String id, String status, String operatorId);

  AlertRuleBO deleteAlertRule(String id, String operatorId, boolean forceDelete);

}
