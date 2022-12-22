package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.manager.appliance.data.SendPolicyDO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public interface SendPolicyDao {
  List<SendPolicyDO> querySendPolicies();

  SendPolicyDO querySendPolicy(String id);

  List<SendPolicyDO> querySendPoliciesStateOn();

  SendPolicyDO querySendPolicyByName(String name);

  void saveSendPolicy(SendPolicyDO sendPolicyDO);

  void updateSendPolicy(SendPolicyDO sendPolicyDO);

  void deleteSendPolicy(String id, String operatorId);

  void changeSendPolicyState(String id, String state, String operatorId);

  List<SendPolicyDO> querySendPoliciesByExternelReceiverId(String id);

  List<SendPolicyDO> querySendPoliciesBySendRuleId(String id);

  void updateSendPolicyTimeByExternalReceiverId(String id, String operatorId);

  void updateSendPolicyTimeBySendRuleId(String id, String operatorId);


  SendPolicyDO querySendPolicyBySendPolicyInCmsId(String sendPolicyInCmsId);

  List<String> querySendPolicyIds(boolean onlyLocal);

  List<String> queryAssignSendPolicyIds(Date beforeTime);
}
