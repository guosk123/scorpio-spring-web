package com.machloop.fpc.cms.center.appliance.dao;

import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.ServiceFollowDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public interface ServiceFollowDao {

  List<ServiceFollowDO> queryUserFollowService(String userId);

  ServiceFollowDO saveServiceFollow(ServiceFollowDO serviceFollowDO);

  int deleteServiceFollow(String serviceId);

  int deleteServiceFollow(String userId, String serviceId, String networkId, String networkGroupId);

}
