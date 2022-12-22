package com.machloop.fpc.cms.center.broker.service.subordinate;

import java.util.List;

import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月21日, fpc-cms-center
 */
public interface FpcNetworkService {

  List<FpcNetworkBO> queryAllNetworks();

  List<FpcNetworkBO> queryNetworks(String deviceType, String deviceSerialNumber);

  void deleteNetworkByFpc(String fpcSerialNumber);

  int deleteNetworkByLinkage(List<String> fpcNetworkIds, String operatorId);

}
