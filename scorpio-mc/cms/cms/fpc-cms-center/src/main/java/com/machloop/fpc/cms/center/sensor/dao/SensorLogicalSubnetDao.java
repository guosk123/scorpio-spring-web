package com.machloop.fpc.cms.center.sensor.dao;

import java.util.Date;
import java.util.List;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public interface SensorLogicalSubnetDao {

  List<SensorLogicalSubnetDO> querySensorLogicalSubnets();

  List<SensorLogicalSubnetDO> querySensorLogicalSubnetsByNetwork(String networkId);

  List<SensorLogicalSubnetDO> querySensorLogicalSubnets(Date beforeTime);

  List<String> querySensorLogicalSubnetIds(boolean onlyLocal);

  List<SensorLogicalSubnetDO> queryAssignLogicalSubnets(Date beforeTime);

  List<String> queryAssignLogicalSubnetIds(Date beforeTime);

  SensorLogicalSubnetDO queryLogicalSubnetByAssignId(String assignId);

  SensorLogicalSubnetDO querySensorLogicalSubnet(String id);

  SensorLogicalSubnetDO querySensorLogicalSubnetByName(String networkGroupsName);

  /**
   * recover应用场景：
   *  在cms上新建子网s，其中包含a、b、c三个网络，这三个网络分别属于不同的探针a、b、c，子网s下发下去后4台设备中子网s的id均相等。此时执行以下步骤：
   *  步骤1、编辑子网s，删除其中的网络c，下发下去后在探针c上会删除子网s。
   *  步骤2、在cms上又将网络c添加进了子网s，此时在探针c上应该新建一个子网s，但为了保证子网id相等，所以要恢复步骤1探针c已经删除的子网s，此时使用recover方法
   */
  SensorLogicalSubnetDO saveOrRecoverSensorLogicalSubnet(SensorLogicalSubnetDO sensorLogicalSubnetDO);

  int updateSensorLogicalSubnet(String id, SensorLogicalSubnetDO sensorLogicalSubnetDO,
      String operatorId);

  int deleteSensorLogicalSubnet(String id, String operatorId);

}
