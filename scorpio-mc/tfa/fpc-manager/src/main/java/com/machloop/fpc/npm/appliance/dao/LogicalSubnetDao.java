package com.machloop.fpc.npm.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.npm.appliance.data.LogicalSubnetDO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public interface LogicalSubnetDao {

  List<LogicalSubnetDO> queryLogicalSubnets();

  List<LogicalSubnetDO> queryAssignLogicalSubnets(Date beforeTime);

  List<String> queryLogicalSubnetIds(boolean onlyLocal);

  LogicalSubnetDO queryLogicalSubnet(String id);

  LogicalSubnetDO queryLogicalSubnetByCmsSubnetId(String cmsSubnetId, String networkId);

  LogicalSubnetDO queryLogicalSubnetByName(String name);

  List<LogicalSubnetDO> queryLogicalSubnetByNetworkId(String networkId);

  /**
   * recover应用场景：
   *  在cms上新建子网s，其中包含a、b、c三个网络，这三个网络分别属于不同的探针a、b、c，子网s下发下去后4台设备中子网s的id均相等。此时执行以下步骤：
   *  步骤1、编辑子网s，删除其中的网络c，下发下去后在探针c上会删除子网s。
   *  步骤2、在cms上又将网络c添加进了子网s，此时在探针c上应该新建一个子网s，但为了保证子网id相等，所以要恢复步骤1探针c已经删除的子网s，此时使用recover方法
   */
  LogicalSubnetDO saveOrRecoverLogicalSubnet(LogicalSubnetDO logicalSubnetDO);

  int updateLogicalSubnet(LogicalSubnetDO logicalSubnetDO);

  int deleteLogicalSubnet(String id, String operatorId);

  int deleteLogicalSubnetByNetworkId(String networkId, String operatorId);

}
