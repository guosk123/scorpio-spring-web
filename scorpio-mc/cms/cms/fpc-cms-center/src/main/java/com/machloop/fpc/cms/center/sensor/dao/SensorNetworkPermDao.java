package com.machloop.fpc.cms.center.sensor.dao;

import java.util.List;

import com.machloop.fpc.cms.center.sensor.data.SensorNetworkPermDO;

/**
 * @author guosk
 *
 * create at 2022年3月10日, fpc-cms-center
 */
public interface SensorNetworkPermDao {

  List<SensorNetworkPermDO> querySensorNetworkPerms(List<String> userIds);

  int updateSensorNetworkPerms(List<SensorNetworkPermDO> sensorNetworkPerms);

  int deleteSensorNetworkPermByUser(String userId);

  int deleteSensorNetworkPermByNetwork(String networkId);

  int deleteSensorNetworkPermByNetworkGroup(String networkGroupId);

}
