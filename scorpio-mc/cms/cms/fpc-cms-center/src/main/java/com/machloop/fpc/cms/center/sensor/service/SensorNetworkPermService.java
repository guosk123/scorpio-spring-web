package com.machloop.fpc.cms.center.sensor.service;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkPermBO;

/**
 * @author guosk
 *
 * create at 2022年3月10日, fpc-cms-center
 */
public interface SensorNetworkPermService {

  Page<SensorNetworkPermBO> querySensorNetworkPerms(Pageable page);

  SensorNetworkPermBO queryCurrentUserNetworkPerms();

  int updateSensorNetworkPerms(SensorNetworkPermBO sensorNetworkPerm, String operatorId);

  int deleteSensorNetworkPermByNetwork(String networkId);

  int deleteSensorNetworkPermByNetworkGroup(String networkGroupId);

}
