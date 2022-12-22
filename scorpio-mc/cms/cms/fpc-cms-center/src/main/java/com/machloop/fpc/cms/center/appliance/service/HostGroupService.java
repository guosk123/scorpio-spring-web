package com.machloop.fpc.cms.center.appliance.service;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.bo.HostGroupBO;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月5日, fpc-manager
 */
public interface HostGroupService {

  Page<HostGroupBO> queryHostGroups(Pageable page, String name, String description);

  List<HostGroupBO> queryHostGroups();

  HostGroupBO queryHostGroup(String id);

  List<String> exportHostGroups();
  
  int importHostGroups(MultipartFile file, String operatorId);

  HostGroupBO saveHostGroup(HostGroupBO hostInsideBO, String operatorId);

  HostGroupBO updateHostGroup(String id, HostGroupBO hostInsideBO, String operatorId);

  HostGroupBO deleteHostGroup(String id, String operatorId, boolean forceDelete);

}
