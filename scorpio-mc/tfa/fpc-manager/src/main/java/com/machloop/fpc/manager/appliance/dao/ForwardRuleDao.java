package com.machloop.fpc.manager.appliance.dao;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.data.ForwardRuleDO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/7 17:19,IntelliJ IDEA
 *
 */
public interface ForwardRuleDao {

  Page<ForwardRuleDO> queryForwardRules(Pageable page);

  ForwardRuleDO queryForwardRule(String id);

  ForwardRuleDO queryForwardRuleByName(String name);

  ForwardRuleDO saveForwardRule(ForwardRuleDO forwardRuleDO);

  int updateForwardRule(ForwardRuleDO forwardRuleDO);

  int deleteForwardRule(String id, String operatorId);

  List<ForwardRuleDO> queryForwardRules();
}
