package com.machloop.fpc.manager.metadata.dao;

import java.util.List;

import com.machloop.fpc.manager.metadata.data.CollectPolicyDO;

public interface CollectPolicyDao {

  List<CollectPolicyDO> queryCollectPolicys();

  List<CollectPolicyDO> queryCollectAllPolicy();

  CollectPolicyDO queryCollectPolicyWithIpv6(String ipAddress);

  CollectPolicyDO queryCollectPolicy(String id);

  CollectPolicyDO queryCollectPolicyByName(String name);

  CollectPolicyDO queryCollectPolicyWithIpBetween(long ipStart, long ipEnd);

  CollectPolicyDO saveCollectPolicy(CollectPolicyDO collectPolicyDO);

  int updateCollectPolicy(CollectPolicyDO collectPolicyDO);

  int changeCollectPolicyState(String id, String state, String operatorId);

  int deleteCollectPolicy(String id, String operatorId);

}
