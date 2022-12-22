package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.data.HostGroupDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月5日, fpc-manager
 */
public interface HostGroupDao {

  Page<HostGroupDO> queryHostGroups(Pageable page, String name, String description);

  List<HostGroupDO> queryHostGroups();

  HostGroupDO queryHostGroup(String id);

  HostGroupDO queryHostGroupByName(String name);

  List<String> queryAssignHostGroups(Date beforeTime);

  List<String> queryHostGroupIds(boolean onlyLocal);

  HostGroupDO queryHostGroupByCmsHostGroupId(String cmsHostGroupId);

  void saveHostGroups(List<HostGroupDO> hostInsideDOList);

  int saveHostGroups(List<HostGroupDO> hostInsideDOList, String id);

  HostGroupDO saveOrRecoverHostGroup(HostGroupDO hostInsideDO);

  int updateHostGroup(HostGroupDO hostInsideDO);

  void refreshHostGroups(List<HostGroupDO> hostInsideDOList);

  int deleteHostGroup(String id, String operatorId);

  List<HostGroupDO> queryHostGroupByNameList(String name);
}
