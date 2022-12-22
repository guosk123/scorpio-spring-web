package com.machloop.fpc.cms.center.sensor.service;

import java.util.List;
import com.machloop.fpc.cms.center.appliance.bo.MetricSettingBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public interface SensorLogicalSubnetService {

  List<SensorLogicalSubnetBO> querySensorLogicalSubnets();

  SensorLogicalSubnetBO querySensorLogicalSubnet(String id);

  SensorLogicalSubnetBO saveSensorLogicalSubnet(SensorLogicalSubnetBO sensorLogicalSubnetBO,
      List<MetricSettingBO> metricSettings, String operatorId);

  SensorLogicalSubnetBO updateSensorLogicalSubnet(String id,
      SensorLogicalSubnetBO sensorLogicalSubnetBO, String operatorId);

  void removeNetworkFromSubnet(String networkId, String operatorId);

  SensorLogicalSubnetBO deleteSensorLogicalSubnet(String id, String operatorId,
      boolean forceDelete);

}
