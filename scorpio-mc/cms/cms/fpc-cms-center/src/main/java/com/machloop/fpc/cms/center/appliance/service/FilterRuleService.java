package com.machloop.fpc.cms.center.appliance.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.center.appliance.bo.FilterRuleBO;

/**
 * @author chenshimiao
 *
 * create at 2022/8/18 10:53 AM,cms
 * @version 1.0
 */
public interface FilterRuleService {

  Page<FilterRuleBO> queryFilterRules(PageRequest page);

  List<FilterRuleBO> queryFilterRule();

  FilterRuleBO queryFilterRule(String id);

  List<String> exportFilterRules();

  FilterRuleBO saveFilterRule(FilterRuleBO filterRuleBO, String before, String operatorId);

  FilterRuleBO updateFilterRule(String id, FilterRuleBO filterRuleBO, String operatorId);

  List<FilterRuleBO> updateFilterRulePriority(List<String> idList, Integer page, Integer pageSize,
      String operator, String operatorId);

  List<FilterRuleBO> updateFilterRuleState(List<String> idList, String state, String operatorId);

  List<FilterRuleBO> deleteFilterRule(List<String> idList, String operatorId, boolean forceDelete);

  void importFilterRule(MultipartFile file, String id);
}
