package com.machloop.fpc.npm.appliance.dao;

import com.machloop.fpc.npm.appliance.data.NetworkTopologyDO;

/**
 * @author guosk
 *
 * create at 2021年7月10日, fpc-manager
 */
public interface NetworkTopologyDao {

  NetworkTopologyDO queryNetworkTopology();

  NetworkTopologyDO queryNetworkTopologyByNetworkId(String networkId);

  int saveOrUpdateNetworkTopology(NetworkTopologyDO networkTopologyDO);

}
