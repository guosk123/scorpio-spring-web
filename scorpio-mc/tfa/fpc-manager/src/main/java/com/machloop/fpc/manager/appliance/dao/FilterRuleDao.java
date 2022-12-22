package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.appliance.data.FilterRuleDO;

/**
 * @author chenshimiao
 *
 * create at 2022/8/9 9:18 AM,cms
 * @version 1.0
 */
public interface FilterRuleDao {
  Page<FilterRuleDO> queryFilterRules(PageRequest page);

  FilterRuleDO queryFilterRuleByName(String name);

  FilterRuleDO queryFilterRuleByCmsFilterPolicyId(String networkPolicyInCmsId);

  List<FilterRuleDO> queryFilterRule();

  List<String> queryFilterRule(Date beforeTime);

  List<FilterRuleDO> queryFilterRule(Integer page, Integer pageSize);

  FilterRuleDO queryFilterRule(String id);

  List<String> queryFilterRule(boolean onlyLocal);

  int queryFilterMaxPriority();

  FilterRuleDO queryFilterRuleByCmsFilterRuleId(String storageRuleInCmsId);

  FilterRuleDO saveOrRecoverFilterRule(FilterRuleDO filterRuleDO);

  int saveFilterRules(List<FilterRuleDO> filterRuleDOList, String operatorId);

  int updateFilterRule(String id, Integer priority, String operatorId);

  int updateFilterRule(FilterRuleDO filterRuleDO);

  int updateFilterRuleState(List<String> idList, String state, String operatorId);

  int deleteNetworkRule(List<String> idList, String operatorId);
}
