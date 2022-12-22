package com.machloop.fpc.cms.center.appliance.service;

import java.util.List;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.bo.AlertRuleBO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月19日, fpc-cms-center
 */
public interface AlertRuleService {

  public static final String ALL_NETWORK = "allNetwork";

  Page<AlertRuleBO> queryAlertRules(Pageable page, String name, String category, String level,
      String networkId, String serviceId);

  List<String> queryAlertRulesBySource(String sourceType, String networkId, String serviceId);

  List<AlertRuleBO> queryAlertRulesByCategory(String category);

  AlertRuleBO queryAlertRule(String id);

  AlertRuleBO saveAlertRule(AlertRuleBO alertRuleBO, String operatorId);

  AlertRuleBO updateAlertRule(String id, AlertRuleBO alertRuleBO, String operatorId);

  AlertRuleBO updateAlertRuleStatus(String id, String status, String operatorId);

  void updateAlertRuleScope(String networkId, String operatorId);

  AlertRuleBO deleteAlertRule(String id, String operatorId, boolean forceDelete);

}
