package com.machloop.fpc.manager.appliance.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.appliance.bo.DomainWhiteListBO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/12/8 11:15 AM,cms
 * @version 1.0
 */
public interface DomainWhiteListService {
  Page<DomainWhiteListBO> queryDomainWhiteList(PageRequest page, String name, String domain);

  List<DomainWhiteListBO> queryDomainWhiteList();

  List<String> exportDomainWhiteList(String name, String domain);

  DomainWhiteListBO queryDomainWhite(String id);

  DomainWhiteListBO saveDomainWhiteList(DomainWhiteListBO domainWhiteListBO, String operatorId);

  int importDomainWhiteList(MultipartFile file, String id);

  int importIssuedDomain(MultipartFile file, String id);

  DomainWhiteListBO updateDomainWhiteList(String id, DomainWhiteListBO domainWhiteListBO,
      String operatorId);

  DomainWhiteListBO deleteDomainWhite(String id, String operatorId, boolean forceDelete);

  List<DomainWhiteListBO> deleteDomainWhiteByNameAndDomain(String name, String domain,
      String operatorId);
}
