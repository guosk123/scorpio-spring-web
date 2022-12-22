package com.machloop.fpc.npm.appliance.dao;

import java.util.List;

import com.machloop.fpc.npm.appliance.data.NetworkInsideIpDO;

/**
 * @author guosk
 *
 * create at 2020年11月11日, fpc-manager
 */
public interface NetworkInsideIpDao {

  List<NetworkInsideIpDO> queryNetworkInsideIps(String networkId);

  void mergeNetworkInsideIps(List<NetworkInsideIpDO> networkInsideIps);

  int deleteNetworkInsideIp(String networkId);

}
