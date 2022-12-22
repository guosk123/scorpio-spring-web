package com.machloop.fpc.cms.center.broker.dao;

import java.util.List;

import com.machloop.fpc.cms.center.broker.data.FpcNetworkDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public interface FpcNetworkDao {

  List<FpcNetworkDO> queryFpcNetworks(String fpcSerialNumber);

  List<FpcNetworkDO> queryFpcNetworkByCms(List<String> cmsSerialNumbers);

  List<FpcNetworkDO> queryFpcNetworksByReportState(String reportState);

  FpcNetworkDO queryFpcNetworkByFpcNetworkId(String fpcNetworkId);
  
  List<FpcNetworkDO> queryFpcNetworkByFpcNetworkIds(List<String> fpcNetworkIdList);

  int batchSaveFpcNetworks(List<FpcNetworkDO> networks);

  int updateFpcNetwork(FpcNetworkDO networkDO);

  int updateFpcNetworkReportState(List<String> networkIds, String reportState);

  int deleteFpcNetwork(List<String> networkIds);

  int deleteFpcNetwork(String fpcSerialNumber);

}
