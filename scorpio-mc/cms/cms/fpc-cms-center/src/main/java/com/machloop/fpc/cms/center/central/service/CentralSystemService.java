package com.machloop.fpc.cms.center.central.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.central.bo.CentralSystemBO;
import com.machloop.fpc.cms.center.central.data.CentralSystemDO;

public interface CentralSystemService {

  List<CentralSystemBO> queryCentralSystems(String deviceType, String monitoredSerialNumber,
      int interval, Date startTime, Date endTime);

  CentralSystemBO queryCentralSystem(String deviceType, String monitoredSerialNumber);

  /**
   * 统计数据存储使用率排行
   * @return
   */
  Map<String, Object> queryStorageSpaceUsagesByRanking(int number);

  /**
   * 根据networkId, networkGroupId获取最早报文时间
   */
  Map<String, Object> queryMaxDataOldestTime(String networkId, String networkGroupId);

  /**
   * 采集本地系统状态
   * @param metricTime
   * @return
   */
  int statisticCentralSystem(Date metricTime);

  /**
   * 采集下级设备的系统状态
   * @param deviceType
   * @param centralSystems
   */
  void collectCentralSystem(String deviceType, List<CentralSystemDO> centralSystems);

  /**
   * 聚合数据
   */
  void rollupCentralSystem();

  int clearHisCentralSystem(Date clearTime, int interval);

  int deleteCentralSystem(String deviceType, String monitoredSerialNumber);

}
