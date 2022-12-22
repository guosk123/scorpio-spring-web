package com.machloop.fpc.cms.center.appliance.dao;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.data.IngestPolicyDO;

import java.util.Date;
import java.util.List;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public interface IngestPolicyDao {

  Page<IngestPolicyDO> queryIngestPolicys(Pageable page);

  List<IngestPolicyDO> queryIngestPolicys();

  List<String> queryIngestPolicyIds(boolean onlyLocal);

  IngestPolicyDO queryIngestPolicy(String id);

  IngestPolicyDO queryIngestPolicyByName(String name);

  IngestPolicyDO queryIngestPolicyByAssignId(String assignId);

  List<IngestPolicyDO> queryAssignIngestPolicyIds(Date beforeTime);

  IngestPolicyDO saveIngestPolicy(IngestPolicyDO ingestPolicyDO);

  int updateIngestPolicy(IngestPolicyDO ingestPolicyDO);

  int deleteIngestPolicy(String id, String operatorId);

}
