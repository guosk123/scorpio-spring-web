package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.appliance.data.SendRuleDO;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
public interface SendRuleDao {

    List<SendRuleDO> querySendRules();

    SendRuleDO querySendRule(String id);

    SendRuleDO querySendRuleByName(String name);

    void saveSendRule(SendRuleDO sendRuleDO);

    void updateSendRule(SendRuleDO sendRuleDO);

    void deleteSendRule(String id, String operatorId);

    List<Map<String, Object>> querySendRuleTables(String index);

    Map<String, List<Map<String, Object>>> queryClickhouseTables();

    List<String> querySendRuleIds(boolean onlyLocal);

    SendRuleDO querySendRuleByAssignId(String assignId);

    List<SendRuleDO> queryAssignSendRuleIds(Date beforeTime);
}
