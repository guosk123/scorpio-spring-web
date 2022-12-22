package com.machloop.fpc.manager.appliance.service;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.appliance.bo.ForwardRuleBO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/7 17:15,IntelliJ IDEA
 *
 */
public interface ForwardRuleService {

  Page<ForwardRuleBO> queryForwardRules(PageRequest page);

  ForwardRuleBO queryForwardRule(String id);

  ForwardRuleBO saveForwardRule(ForwardRuleBO forwardRuleBO, String id);

  ForwardRuleBO updateForwardRule(String id, ForwardRuleBO forwardRuleBO, String id1);

  ForwardRuleBO deleteForwardRule(String id, String id1, boolean b);

  List<ForwardRuleBO> queryForwardRules();
}
