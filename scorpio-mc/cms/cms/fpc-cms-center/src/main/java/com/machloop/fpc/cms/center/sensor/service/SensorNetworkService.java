package com.machloop.fpc.cms.center.sensor.service;

import java.util.List;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月22日, fpc-cms-center
 */
public interface SensorNetworkService {

  List<SensorNetworkBO> querySensorNetworks();

  SensorNetworkBO querySensorNetwork(String id);

  List<SensorNetworkBO> getNetworksInSensorList();

  SensorNetworkBO saveSensorNetwork(SensorNetworkBO sensorNetworkBO, String operatorId);

  List<SensorNetworkBO> batchSaveSensorNetworks(List<SensorNetworkBO> sensorNetworkBO,
      String operatorId);

  SensorNetworkBO updateSensorNetwork(String id, SensorNetworkBO sensorNetworkBO,
      String operatorId);

  SensorNetworkBO deleteSensorNetwork(String id, String operatorId);

  void deleteSensorNetworkByFpcNetworkId(String networkId, String operatorId);

}
