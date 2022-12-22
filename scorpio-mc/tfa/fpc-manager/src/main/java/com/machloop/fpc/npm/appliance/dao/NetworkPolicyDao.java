package com.machloop.fpc.npm.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.npm.appliance.data.NetworkPolicyDO;

/**
 * @author guosk
 *
 * create at 2020年11月11日, fpc-manager
 */
public interface NetworkPolicyDao {

  List<NetworkPolicyDO> queryNetworkPolicys();

  List<String> queryNetworkPolicyOfNetworkIdAndPolicyId(Date beforeTime);

  List<NetworkPolicyDO> queryNetworkPolicyByPolicyId(String policyId, String policyType);

  NetworkPolicyDO queryNetworkPolicyByPolicyId(String id);

  List<NetworkPolicyDO> queryNetworkPolicyByPolicyType(String policyType);

  List<NetworkPolicyDO> queryNetworkPolicyByNetworkId(String networkId);

  List<NetworkPolicyDO> queryNetworkPolicyByNetworkIdAndPolicyType(String networkId,
      String policyType);

  NetworkPolicyDO queryNetworkPolicyByNetworkId(String networkId, String policyType);

  NetworkPolicyDO saveNetworkPolicy(String networkId, String policyId, String policyType,
      String operatorId);

  int saveNetworkPolicy(List<NetworkPolicyDO> networkPolicyDOList, String operatorId);

  void mergeNetworkPolicys(List<NetworkPolicyDO> networkPolicys);

  int updateNetworkPolicy(String networkId, String policyId, String policyType, String operatorId);

  int updateNetworkPolicyByPolicyId(String id, String networkId, String policyId,
      String operatorId);

  int deleteNetworkPolicyByNetworkId(String networkId);

  int deleteNetworkPolicyByPolicyId(String policyId, String policyType);

  void mergeNetworkPolicysOfSend(List<NetworkPolicyDO> networkPolicyDOList);

  void deleteNetworkPolicyByNetworkIdAndPolicyType(String networkId, String policyType);

  int deleteNetworkPolicyByFilterRule(boolean onlyLocal);
}
