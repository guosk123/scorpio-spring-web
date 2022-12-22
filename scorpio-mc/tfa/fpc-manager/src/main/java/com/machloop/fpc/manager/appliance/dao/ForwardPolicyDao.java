package com.machloop.fpc.manager.appliance.dao;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.appliance.data.ForwardPolicyDO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/11 23:02,IntelliJ IDEA
 *
 */
public interface ForwardPolicyDao {
  Page<ForwardPolicyDO> queryForwardPolicies(PageRequest page);

  ForwardPolicyDO queryForwardPolicy(String id);

  ForwardPolicyDO queryForwardPolicyByName(String name);

  ForwardPolicyDO saveForwardPolicy(ForwardPolicyDO forwardPolicyDO);

  int updateForwardPolicy(ForwardPolicyDO forwardPolicyDO);

  int deleteForwardPolicy(String id, String operatorId);

  int changeForwardPolicy(String id, String state, String operatorId);

  List<ForwardPolicyDO> queryForwardPolicyByRuleId(String id);

  int deleteForwardPolicyByRuleId(String id, String operatorId);

  List<ForwardPolicyDO> queryForwardPolicys();
}
