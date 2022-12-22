package com.machloop.fpc.cms.center.appliance.dao;

import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.FilterRuleNetworkDO;

/**
 * @author chenshimiao
 *
 * create at 2022/9/29 1:52 PM,cms
 * @version 1.0
 */
public interface FilterRuleIpDao {

  FilterRuleNetworkDO queryIPOrIPGroupByFilterRuleId();

  List<FilterRuleNetworkDO> queryFilterRuleNetworks();

  List<FilterRuleNetworkDO> getIP(String id);

  List<FilterRuleNetworkDO> queryFilterRuleNetworkByNetworkGroupId(String id);

  void saveFilterRuleIp(List<FilterRuleNetworkDO> filterRuleIPOrIPHostGroupDO);

  int deleteFilterRuleByFilterRuleId(String filterRuleId);
}
