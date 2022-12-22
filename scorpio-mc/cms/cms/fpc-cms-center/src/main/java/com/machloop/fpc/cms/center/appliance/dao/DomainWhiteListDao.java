package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.center.appliance.data.DomainWhiteListDO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/12/9 5:08 PM,cms
 * @version 1.0
 */
public interface DomainWhiteListDao {

  Page<DomainWhiteListDO> queryDomainWhiteList(PageRequest page, String name, String domain);

  List<DomainWhiteListDO> queryDomainWhiteList();

  DomainWhiteListDO queryDomainWhiteList(String id);

  DomainWhiteListDO queryDomainWhiteByName(String name);

  DomainWhiteListDO queryDomainWhiteListByAssignId(String assignId);

  List<String> queryDomainWhiteListIds(boolean onlyLocal);

  List<String> queryAssignDomainWhiteListIds(Date beforeTime);

  List<String> queryDomainWhiteListInCmsId();

  List<DomainWhiteListDO> queryDomainWhiteListByNameAndDomain(String name, String domain);

  int queryCountDomainWhiteList();

  List<String> queryDomainWhiteListName();

  List<DomainWhiteListDO> saveDomainWhite(List<DomainWhiteListDO> domainWhiteListDOList, String id);

  DomainWhiteListDO saveOrRecoverDomainWhite(DomainWhiteListDO domainWhiteListDO,
      String operatorId);

  int updateDomainWhiteList(DomainWhiteListDO domainWhiteListDO);

  int updateBatchDomainWhiteList(List<DomainWhiteListDO> existDomainWhiteListDO, String operatorId);

  int deleteDomainWhiteList(String id, String operatorId);

  int deleteDomainWhiteListAll(boolean onlyLocal, String cmsAssignment);

  int deleteDOmainWHiteLIstByNameAndDomain(String name, String domain, String operatorId);
}
