package com.machloop.fpc.cms.center.appliance.dao;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.data.HostGroupDO;

import java.util.Date;
import java.util.List;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月5日, fpc-manager
 */
public interface HostGroupDao {

  Page<HostGroupDO> queryHostGroups(Pageable page, String name, String description);
  
  List<String> queryHostGroupIds(boolean onlyLocal);

  List<HostGroupDO> queryHostGroups();

  HostGroupDO queryHostGroup(String id);

  HostGroupDO queryHostGroupByName(String name);

  HostGroupDO queryHostGroupByAssignId(String assignId);

  List<HostGroupDO> queryAssignHostGroupIds(Date beforeTime);

  void saveHostGroups(List<HostGroupDO> hostInsideDOList);

  HostGroupDO saveHostGroup(HostGroupDO hostInsideDO);

  int batchSaveHostGroups(List<HostGroupDO> hostGroupList);
  
  int updateHostGroup(HostGroupDO hostInsideDO);

  void refreshHostGroups(List<HostGroupDO> hostInsideDOList);

  int deleteHostGroup(String id, String operatorId);

}
