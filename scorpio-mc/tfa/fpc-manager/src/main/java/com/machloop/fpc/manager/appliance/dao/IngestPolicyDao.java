package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.data.IngestPolicyDO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public interface IngestPolicyDao {

  Page<IngestPolicyDO> queryIngestPolicys(Pageable page);

  List<String> queryAssignIngestPolicys(Date beforeTime);

  List<IngestPolicyDO> queryIngestPolicys();

  IngestPolicyDO queryIngestPolicy(String id);

  IngestPolicyDO queryIngestPolicyByName(String name);

  IngestPolicyDO queryIngestPolicyByCmsIngestPolicyId(String cmsIngestPolicyId);

  List<String> queryIngestPolicyIds(boolean onlyLocal);

  IngestPolicyDO saveOrRecoverIngestPolicy(IngestPolicyDO ingestPolicyDO);

  int updateIngestPolicy(IngestPolicyDO ingestPolicyDO);

  int deleteIngestPolicy(String id, String operatorId);

}
