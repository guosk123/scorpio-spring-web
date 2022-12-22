package com.machloop.fpc.npm.appliance.service;

import java.util.List;

import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.MetricSettingBO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public interface LogicalSubnetService {

  List<LogicalSubnetBO> queryLogicalSubnets();

  LogicalSubnetBO queryLogicalSubnet(String id);

  LogicalSubnetBO queryLogicalSubnetByCmsSubnetId(String cmsSubnetId, String networkId);

  LogicalSubnetBO saveLogicalSubnet(LogicalSubnetBO logicalSubnetBO,
      List<MetricSettingBO> metricSettings, String operatorId);

  LogicalSubnetBO updateLogicalSubnet(String id, LogicalSubnetBO logicalSubnetBO,
      String operatorId);

  LogicalSubnetBO deleteLogicalSubnet(String id, String operatorId, boolean forceDelete);

}
