package com.machloop.fpc.cms.center.appliance.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.appliance.bo.SendRuleBO;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
public interface SendRuleService {

  List<Map<String, Object>> querySendRules();

  Map<String, Object> querySendRule(String id);

  SendRuleBO saveSendRule(SendRuleBO sendRuleBO, String operatorId);

  SendRuleBO updateSendRule(SendRuleBO sendRuleBO, String id, String operatorId);

  SendRuleBO deleteSendRule(String id, String operatorId, boolean forceDelete);

  List<Map<String, Object>> querySendRuleTables(String index);

  Map<String, List<Map<String, Object>>> queryClickhouseTables();
}
