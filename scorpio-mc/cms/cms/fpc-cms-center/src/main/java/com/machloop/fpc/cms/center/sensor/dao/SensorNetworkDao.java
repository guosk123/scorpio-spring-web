package com.machloop.fpc.cms.center.sensor.dao;

import java.util.List;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public interface SensorNetworkDao {

  List<SensorNetworkDO> querySensorNetworks();

  List<SensorNetworkDO> querySensorNetworksBySensorIdList(List<String> sensorIdList);

  SensorNetworkDO querySensorNetwork(String id);

  SensorNetworkDO querySensorNetworkByNetworkInSensorId(String id);

  SensorNetworkDO saveSensorNetwork(SensorNetworkDO sensorNetworkDO);

  void batchSaveSensorNetworks(List<SensorNetworkDO> sensorNetworkDOList);

  int updateSensorNetwork(String id, SensorNetworkDO sensorNetworkDO, String operatorId);

  int deleteSensorNetwork(String id, String operatorId);

  int deleteSensorNetworkByFpcNetworkId(String networkId, String operatorId);

}
