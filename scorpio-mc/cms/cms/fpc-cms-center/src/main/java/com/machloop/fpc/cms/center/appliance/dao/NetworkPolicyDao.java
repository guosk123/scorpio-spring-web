package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.NetworkPolicyDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年12月1日, fpc-cms-center
 */
public interface NetworkPolicyDao {

  List<NetworkPolicyDO> queryNetworkPolicyByPolicyType(String policyType);

  List<String> queryNetworkPolicyIds(Date beforeTime);

  List<NetworkPolicyDO> queryNetworkPolicys();

  NetworkPolicyDO queryNetworkPolicy(String id);

  List<NetworkPolicyDO> queryNetworkPolicyByPolicyId(String policyId, String policyType);

  NetworkPolicyDO queryNetworkPolicyByPolicyId(String id);

  List<NetworkPolicyDO> queryNetworkPolicyByNetworkIdAndPolicyType(String networkId,
      String applianceNetworkPolicyStorage);

  NetworkPolicyDO queryNetworkPolicyByNetworkId(String networkId, String policyType);

  List<NetworkPolicyDO> queryNetworkPolicyByNetworkId(String networkId);

  List<String> queryAssignNetworkPolicyIds(Date beforeTime);

  NetworkPolicyDO queryNetworkPolicyByAssignId(String assignId);

  int updateNetworkPolicy(NetworkPolicyDO networkPolicyDO);

  int updateNetworkPolicy(String networkId, String policyId, String policyType, String operatorId);

  NetworkPolicyDO saveNetworkPolicy(NetworkPolicyDO networkPolicyDO);

  List<NetworkPolicyDO> saveNetworkPolicy(List<NetworkPolicyDO> networkPolicyDOList,
      String operatorId);

  int deleteNetworkPolicy(String id, String operatorId);

  int deleteNetworkPolicyByPolicyId(String policyId, String policyType);

  void mergeNetworkPolicys(List<NetworkPolicyDO> policyList);

  void deleteNetworkPolicyByNetworkIdAndPolicySource(String networkId, String policySource);

  List<NetworkPolicyDO> queryNetworkPolicyByNetworkIdAndPolicySource(String networkId,
      String policyType, String policySource);

  List<String> queryNetworkPolicyIdsExceptSendPolicy(Date beforeTime);

  List<String> queryNetworkPolicyIdsOfSendPolicy(Date beforeTime);

  List<NetworkPolicyDO> queryNetworkPolicyByNetworkIdAndPolicySourceOfGroup(String networkId,
      String policyType);

  int updateNetworkPolicyByPolicyId(String id, String networkId, String policyId, String cmsAssignment);

  void deleteNetworkPolicyByPolicyTypeAndPolicySource(String policyType, String id);

  List<NetworkPolicyDO> queryNetworkPolicyByPolicyTypeAndPolicySource(String policyType,
      String policySource);
}
