package com.machloop.fpc.cms.center.central.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.central.data.CentralDiskDO;

/**
 * 本机及下级硬盘状态统计
 * @author guosk
 *
 * create at 2022年5月5日, fpc-cms-center
 */
public interface CentralDiskDao {

  List<CentralDiskDO> queryCentralDisks(String deviceType, String deviceSerialNumber,
      String raidNo);

  List<Map<String, Object>> countCentralDiskByState();

  void batchSaveOrUpdateCentralDisks(List<CentralDiskDO> diskList);

  int deleteCentralDisks(String deviceType, String deviceSerialNumber);

}
