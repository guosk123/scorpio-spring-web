package com.machloop.fpc.npm.appliance.dao;

import java.util.List;

import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskPolicyDO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public interface PacketAnalysisTaskPolicyDao {
  List<PacketAnalysisTaskPolicyDO> queryPacketAnalysisTaskPolicyByPolicyIdAndPolicyType(
      String policyId, String policyType);

  void savePacketAnalysisTaskPolicy(String packetAnalysisTaskId, String policyId, String policyType,
      String operatorId);

  void deletePacketAnalysisTaskPolicyByPacketAnalysisTaskId(String packetAnalysisTaskId);

  void mergePacketAnalysisTaskPolicies(List<PacketAnalysisTaskPolicyDO> policyList);

  List<PacketAnalysisTaskPolicyDO> queryPolicyIdsByIdAndPolicyType(String id, String policyType);

  void deletePacketAnalysisTaskPolicyByPolicyId(String policyId, String policyType);

  List<PacketAnalysisTaskPolicyDO> queryPolicyIdsByPolicyType(String policyType);
}
