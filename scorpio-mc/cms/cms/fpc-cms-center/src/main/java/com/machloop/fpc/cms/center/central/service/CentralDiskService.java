package com.machloop.fpc.cms.center.central.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.central.bo.CentralDiskBO;
import com.machloop.fpc.cms.center.central.bo.CentralRaidBO;

public interface CentralDiskService {

  List<CentralRaidBO> queryCentralRaids(String deviceType, String deviceSerialNumber);

  List<CentralDiskBO> queryCentralDisks(String deviceType, String deviceSerialNumber,
      String raidNo);

  List<Map<String, Object>> countCentralDiskByState();

  int deleteCentralDisk(String deviceType, String deviceSerialNumber);

}
