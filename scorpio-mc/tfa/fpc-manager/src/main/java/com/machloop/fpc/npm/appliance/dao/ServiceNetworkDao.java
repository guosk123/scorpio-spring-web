package com.machloop.fpc.npm.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.npm.appliance.data.ServiceNetworkDO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public interface ServiceNetworkDao {

  List<ServiceNetworkDO> queryServiceNetworks();

  List<String> queryAssignServiceNetworkIds(Date beforeTime);

  List<ServiceNetworkDO> queryServiceNetworks(List<String> serviceIds);

  List<ServiceNetworkDO> queryServiceNetworks(String serviceId, String networkId);

  void mergeServiceNetworks(List<ServiceNetworkDO> serviceNetworks);

  void batchSaveServiceNetwork(List<ServiceNetworkDO> serviceNetworks);

  int deleteServiceNetwork(String serviceId);

}
