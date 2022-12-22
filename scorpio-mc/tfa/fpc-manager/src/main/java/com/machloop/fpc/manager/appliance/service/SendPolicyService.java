package com.machloop.fpc.manager.appliance.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.appliance.bo.SendPolicyBO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public interface SendPolicyService {
  List<Map<String, Object>> querySendPolicies();

  Map<String, Object> querySendPolicy(String id);

  List<Map<String, Object>> querySendPoliciesStateOn();

  SendPolicyBO saveSendPolicy(SendPolicyBO sendPolicyBO, String operatorId);

  SendPolicyBO updateSendPolicy(SendPolicyBO sendPolicyBO, String id, String operatorId);

  SendPolicyBO deleteSendPolicy(String id, String operatorId, boolean forceDelete);

  SendPolicyBO changeSendPolicyState(String id, String state, String operatorId);
}
