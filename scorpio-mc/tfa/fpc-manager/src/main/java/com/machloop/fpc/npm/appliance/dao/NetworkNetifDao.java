package com.machloop.fpc.npm.appliance.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.npm.appliance.data.NetworkNetifDO;

/**
 * @author guosk
 *
 * create at 2020年11月11日, fpc-manager
 */
public interface NetworkNetifDao {

  List<NetworkNetifDO> queryAllNetworkNetifs();

  List<NetworkNetifDO> queryNetworkNetifs(String networkId);

  /**
   * 根据网络统计每个网络配置接口数量和总带宽
   * @return
   */
  List<Map<String, Object>> staticNetifUsageByNetwork();

  void mergeNetworkNetifs(List<NetworkNetifDO> networkNetifs);

  int deleteNetworkNetif(String networkId);

}
