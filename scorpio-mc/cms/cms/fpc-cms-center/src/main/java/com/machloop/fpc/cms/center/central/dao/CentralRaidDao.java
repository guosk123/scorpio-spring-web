package com.machloop.fpc.cms.center.central.dao;

import java.util.List;

import com.machloop.fpc.cms.center.central.data.CentralRaidDO;

/**
 * 本机及下级设备RAID信息
 * @author guosk
 *
 * create at 2022年5月5日, fpc-cms-center
 */
public interface CentralRaidDao {

  List<CentralRaidDO> queryRaidBySerialNumber(String deviceType, String serialNumber);

  void batchSaveOrUpdateCentralRaids(List<CentralRaidDO> raidList);

  int deleteCentralRaids(String deviceType, String serialNumber);

}
