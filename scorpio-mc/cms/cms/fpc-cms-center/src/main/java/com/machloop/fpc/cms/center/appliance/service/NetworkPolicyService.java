package com.machloop.fpc.cms.center.appliance.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.appliance.bo.NetworkPolicyBO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年12月1日, fpc-cms-center
 */
public interface NetworkPolicyService {

  List<Map<String, String>> queryNetworkPolicys(String policyType);

  NetworkPolicyBO queryNetworkPolicy(String id);

  NetworkPolicyBO updateNetworkPolicy(String id, NetworkPolicyBO networkPolicyBO,
      String operatorId);

  NetworkPolicyBO saveNetworkPolicy(NetworkPolicyBO networkPolicyBO, String operatorId);

  int saveNetworkPolicy(List<NetworkPolicyBO> networkPolicyBO, String operatorId);

  NetworkPolicyBO deleteNetworkPolicy(String id, String operatorId);

  int deleteNetworkPolicyByPriorId(String priorId, String priorType);
}
