package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.appliance.data.DomainWhiteListDO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/12/8 11:25 AM,cms
 * @version 1.0
 */
public interface DomainWhiteListDao {
  Page<DomainWhiteListDO> queryDomainWhiteList(PageRequest page, String name, String domain);

  List<DomainWhiteListDO> queryDomainWhiteList();

  DomainWhiteListDO queryDomainWhiteList(String id);

  DomainWhiteListDO queryDomainWhiteByDomainWhiteListInCmsId(String domainWhiteListInCmsId);

  List<String> queryDomainWhiteListById(Date beforeTime);

  List<String> queryDomainWhiteListInCmsId();

  DomainWhiteListDO queryDomainWhiteByName(String name);

  int queryCountDomainWhiteList();

  List<String> queryDomainWhiteListName();

  List<DomainWhiteListDO> queryDomainWhiteListByNameAndDomain(String name, String domain);

  List<DomainWhiteListDO> saveDomainWhite(List<DomainWhiteListDO> domainWhiteListDOList,
      String operatorId);

  DomainWhiteListDO saveOrRecoverDomainWhite(DomainWhiteListDO domainWhiteListDO,
      String operatorId);

  int updateDomainWhiteList(DomainWhiteListDO domainWhiteListDO);

  int updateBatchDomainWhiteList(List<DomainWhiteListDO> existDomainWhiteListDO, String operatorId);

  int deleteDomainWhiteListAll(boolean onlyLocal, String operatorId);

  int deleteDomainWhiteList(String id, String operatorId);

  int deleteDOmainWHiteLIstByNameAndDomain(String name, String domain, String operatorId);
}
