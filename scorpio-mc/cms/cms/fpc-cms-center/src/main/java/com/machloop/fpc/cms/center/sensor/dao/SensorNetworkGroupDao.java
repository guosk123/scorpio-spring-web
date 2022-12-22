package com.machloop.fpc.cms.center.sensor.dao;

import java.util.List;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public interface SensorNetworkGroupDao {

  List<SensorNetworkGroupDO> querySensorNetworkGroups();

  List<SensorNetworkGroupDO> querySensorNetworkGroupsByNetwork(String networkId);

  SensorNetworkGroupDO querySensorNetworkGroup(String id);

  SensorNetworkGroupDO querySensorNetworkGroupByName(String networkGroupsName);

  SensorNetworkGroupDO saveSensorNetworkGroup(SensorNetworkGroupDO sensorNetworkGroupDO);

  int updateSensorNetworkGroup(String id, SensorNetworkGroupDO sensorNetworkGroupsDO,
      String operatorId);

  int deleteSensorNetworkGroup(String id, String operatorId);

}
