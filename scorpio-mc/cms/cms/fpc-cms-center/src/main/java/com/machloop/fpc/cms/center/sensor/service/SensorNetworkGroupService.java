package com.machloop.fpc.cms.center.sensor.service;

import java.util.List;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkGroupBO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public interface SensorNetworkGroupService {

  public List<SensorNetworkGroupBO> querySensorNetworkGroups();

  SensorNetworkGroupBO querySensorNetworkGroup(String id);

  SensorNetworkGroupBO saveSensorNetworkGroup(SensorNetworkGroupBO sensorNetworkGroupsBO,
      String operatorId);

  SensorNetworkGroupBO updateSensorNetworkGroup(String id,
      SensorNetworkGroupBO sensorNetworkGroupsBO, String operatorId);

  void removeNetworkFromGroup(String networkId, String operatorId);

  SensorNetworkGroupBO deleteSensorNetworkGroup(String id, String operatorId);

}
