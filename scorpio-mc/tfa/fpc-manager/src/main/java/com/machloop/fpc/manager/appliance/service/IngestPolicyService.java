package com.machloop.fpc.manager.appliance.service;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.bo.IngestPolicyBO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public interface IngestPolicyService {

  Page<IngestPolicyBO> queryIngestPolicys(Pageable page);

  List<IngestPolicyBO> queryIngestPolicys();

  IngestPolicyBO queryIngestPolicy(String id);

  IngestPolicyBO queryIngestPolicyByCmsIngestPolicyId(String cmsIngestPolicyId);

  IngestPolicyBO saveIngestPolicy(IngestPolicyBO ingestPolicyBO, String operatorId);

  IngestPolicyBO updateIngestPolicy(String id, IngestPolicyBO ingestPolicyBO, String operatorId);

  IngestPolicyBO deleteIngestPolicy(String id, String operatorId, boolean forceDelete);

}
