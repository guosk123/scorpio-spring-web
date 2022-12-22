package com.machloop.fpc.cms.center.central.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.central.bo.FpcStorageSpaceUsage;
import com.machloop.fpc.cms.center.central.data.CentralSystemDO;

/**
 * 本机CMS和下级CMS统计信息，以及探针上报的系统空间统计指标
 * @author guosk
 *
 * create at 2022年5月5日, fpc-cms-center
 */
public interface CentralSystemDao {

  List<CentralSystemDO> queryCentralSystems(String deviceType, String monitoredSerialNumber,
      int interval, Date startTime, Date endTime);

  /**
   * 获取探针分区容量
   * @param monitoredSerialNumbers
   * @return
   */
  List<CentralSystemDO> querySensorSpace(List<String> monitoredSerialNumbers);

  List<Date> queryCentralSystemsMetricTime(String deviceType, String monitoredSerialNumber,
      List<Date> metricTimeList);

  CentralSystemDO queryCentralSystem(String deviceType, String monitoredSerialNumber);

  CentralSystemDO queryMaxDataOldestTime(String deviceType, List<String> monitoredSerialNumberList);

  /**
   * 存储空间使用率排行
   * @return
   */
  List<FpcStorageSpaceUsage> queryStorageSpaceUsagesByRanking();

  void saveCentralSystems(List<CentralSystemDO> centralSystemDOList);

  /**
   * 将统计数据存入历史表
   * @param monitorMetricDO
   * @return
   */
  CentralSystemDO saveCentralSystem(CentralSystemDO centralSystemDO);

  /**
   * 更新实时表
   * @param monitorMetricDO
   * @return
   */
  int saveOrUpdateCentralSystem(CentralSystemDO centralSystemDO);

  /**
   * 聚合数据
   * @param startTime
   * @param endTime
   * @return
   */
  int rollupCentralSystem(Date startTime, Date endTime, String deviceType,
      String monitoredSerialNumber);

  /**
   * 定时清除表数据
   * @param beforeTime
   * @param interval
   * @return
   */
  int deleteCentralSystem(Date beforeTime, int interval);

  /**
   * 删除设备时清除设备系统状态
   * @param monitoredSerialNumber
   * @return
   */
  int deleteCentralSystem(String deviceType, String monitoredSerialNumber);

  int deleteCentralSystem(String deviceType, String monitoredSerialNumber, Date metricTime,
      int interval);

}
