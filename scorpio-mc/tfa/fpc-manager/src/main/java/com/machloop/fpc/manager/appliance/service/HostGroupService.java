package com.machloop.fpc.manager.appliance.service;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.bo.HostGroupBO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月5日, fpc-manager
 */
public interface HostGroupService {

  Page<HostGroupBO> queryHostGroups(Pageable page, String name, String description);

  List<HostGroupBO> queryHostGroups();

  List<String> exportHostGroups(String name);

  HostGroupBO queryHostGroup(String id);

  HostGroupBO queryHostGroupByCmsHostGroupId(String cmsHostGroupId);

  void importHostGroups(MultipartFile file, String id);

  HostGroupBO saveHostGroup(HostGroupBO hostInsideBO, String operatorId);

  HostGroupBO updateHostGroup(String id, HostGroupBO hostInsideBO, String operatorId);

  HostGroupBO deleteHostGroup(String id, String operatorId, boolean forceDelete);


}
