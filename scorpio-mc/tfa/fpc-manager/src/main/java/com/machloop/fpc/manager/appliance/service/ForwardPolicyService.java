package com.machloop.fpc.manager.appliance.service;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.appliance.bo.ForwardPolicyBO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/11 23:01,IntelliJ IDEA
 *
 */
public interface ForwardPolicyService {

  Page<ForwardPolicyBO> queryForwardPolicies(PageRequest page);

  ForwardPolicyBO queryForwardPolicy(String id);

  ForwardPolicyBO saveForwardPolicy(ForwardPolicyBO forwardPolicyBO, String id);

  ForwardPolicyBO updateForwardPolicy(String id, ForwardPolicyBO forwardPolicyBO,
      String operatorId);

  ForwardPolicyBO deleteForwardPolicy(String id, String operatorId, boolean forceDelete);


  ForwardPolicyBO changeForwardPolicy(String id, String state, String operatorId,
      boolean forceChange);
}
