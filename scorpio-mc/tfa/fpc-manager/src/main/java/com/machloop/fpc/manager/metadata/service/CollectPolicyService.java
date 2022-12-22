package com.machloop.fpc.manager.metadata.service;

import java.util.List;

import com.machloop.fpc.manager.metadata.vo.CollectPolicyVO;

public interface CollectPolicyService {

  List<CollectPolicyVO> queryCollectPolicys();

  CollectPolicyVO queryCollectPolicy(String id);

  CollectPolicyVO saveCollectPolicy(CollectPolicyVO collectPolicyVO);

  CollectPolicyVO updateCollectPolicy(CollectPolicyVO collectPolicyVO);

  CollectPolicyVO changeCollectPolicyState(String id, String state, String operatorId);

  CollectPolicyVO deleteCollectPolicy(String id, String operatorId);

}
