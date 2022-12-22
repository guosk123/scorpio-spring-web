package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.center.appliance.data.FilterRuleDO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/8/18 10:58 AM,cms
 * @version 1.0
 */
public interface FilterRuleDao {

  Page<FilterRuleDO> queryFilterRules(PageRequest page);

  List<String> queryFilterRules(boolean onlyLocal);

  FilterRuleDO queryFilterRuleByName(String name);

  FilterRuleDO queryFilterRUleByAssignId(String assignId);

  int queryFilterMaxPriority();

  int queryFilterMaxLocalPriority();

  int queryFilterLocalPriority(String priorId);

  String queryFilterPriorId(Integer priority);

  List<FilterRuleDO> queryFilterRule(Integer page, Integer pageSize);

  List<FilterRuleDO> queryFilterRuleByIds(List<String> filterRuleIds);

  FilterRuleDO saveOrRecoverFilterRule(FilterRuleDO filterRuleDO);

  List<FilterRuleDO> queryFilterRule();

  FilterRuleDO queryFilterRule(String id);

  int updateFilterRule(String id, Integer priority, String operatorId);

  int saveFilterRules(List<FilterRuleDO> filterRuleDOList, String operatorId);

  int updateFilterRule(FilterRuleDO filterRuleDO);

  int updateFilterRuleState(List<String> idList, String state, String operatorId);

  int deleteNetworkRule(List<String> idList, String operatorId);

  List<FilterRuleDO> queryAssignFilterPolicyIds(Date beforeTime);
}
