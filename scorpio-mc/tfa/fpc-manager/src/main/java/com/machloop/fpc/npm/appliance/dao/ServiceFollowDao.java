package com.machloop.fpc.npm.appliance.dao;

import java.util.List;

import com.machloop.fpc.npm.appliance.data.ServiceFollowDO;

/**
 * @author guosk
 *
 * create at 2021年5月21日, fpc-manager
 */
public interface ServiceFollowDao {

  List<ServiceFollowDO> queryUserFollowService(String userId);

  ServiceFollowDO saveServiceFollow(ServiceFollowDO serviceFollowDO);

  int deleteServiceFollow(String serviceId);

  int deleteServiceFollow(String userId, String serviceId, String networkId);

}
