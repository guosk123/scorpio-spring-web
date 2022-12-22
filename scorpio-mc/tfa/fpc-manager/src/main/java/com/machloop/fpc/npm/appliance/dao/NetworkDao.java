package com.machloop.fpc.npm.appliance.dao;

import java.util.List;

import com.machloop.fpc.npm.appliance.data.NetworkDO;

/**
 * @author guosk
 *
 * create at 2020年11月10日, fpc-manager
 */
public interface NetworkDao {

  List<NetworkDO> queryNetworks();

  List<NetworkDO> queryNetworks(List<String> ids);

  List<NetworkDO> queryNetworksByReportState(String reportState);

  NetworkDO queryNetwork(String id);

  NetworkDO queryNetworkByName(String name);

  NetworkDO saveNetwork(NetworkDO networkDO);

  int updateNetwork(NetworkDO networkDO);

  int updateNetworkReportState(List<String> networkIds, String reportState);

  int deleteNetwork(String id, String operatorId);

}
