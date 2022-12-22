package com.machloop.fpc.npm.appliance.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.NetworkPolicyBO;
import com.machloop.fpc.npm.appliance.bo.NetworkTopologyBO;

/**
 * @author guosk
 *
 * create at 2020年11月10日, fpc-manager
 */
public interface NetworkService {

  List<NetworkBO> queryNetworks();

  List<NetworkBO> queryNetworks(List<String> ids);

  List<NetworkBO> queryNetworksWithDetail();

  List<Map<String, String>> queryNetworkPolicy(String policyType);

  List<Map<String, Object>> queryNetworkNetif();

  NetworkBO queryNetwork(String id);

  NetworkBO saveNetwork(NetworkBO networkBO, String operatorId);

  int saveNetworkPolicy(List<NetworkPolicyBO> networkPolicyBOList, String operatorId);

  NetworkBO updateNetwork(String id, NetworkBO networkBO, String operatorId);

  void updateNetworkPolicy(String id, String policyId, String policyType, String operatorId);

  int deleteNetworkPolicy(String policyId, String policyType);

  NetworkBO deleteNetwork(String id, String operatorId);

  /************************************************************
  *
  *************************************************************/

  NetworkTopologyBO queryNetworkTopology();

  NetworkTopologyBO updateNetworkTopology(NetworkTopologyBO networkTopologyBO, String operatorId);

  Map<String, List<String>> queryNetworkPolicies();
}
