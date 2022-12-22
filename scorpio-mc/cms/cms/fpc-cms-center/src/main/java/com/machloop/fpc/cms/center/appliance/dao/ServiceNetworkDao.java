package com.machloop.fpc.cms.center.appliance.dao;

import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.ServiceNetworkDO;


/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public interface ServiceNetworkDao {

  List<ServiceNetworkDO> queryServiceNetworks();

  List<ServiceNetworkDO> queryServiceNetworks(List<String> serviceIds);

  List<ServiceNetworkDO> queryServiceNetworks(String serviceId, String networkId);
  
  List<ServiceNetworkDO> queryServiceNetworkByNetworkGroupId(String networkGroupId);

  List<ServiceNetworkDO> queryExistServiceNetworkList(String serviceId, List<String> networkIdList);
  
  void mergeServiceNetworks(List<ServiceNetworkDO> serviceNetworks);

  void batchSaveServiceNetwork(List<ServiceNetworkDO> serviceNetworks);

  int deleteServiceNetwork(String serviceId);

}
