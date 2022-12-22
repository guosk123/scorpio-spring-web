package com.machloop.fpc.cms.center.central.service;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.central.bo.CentralNetifUsage;
import com.machloop.fpc.cms.center.central.bo.CentralNetifBO;
import com.machloop.fpc.cms.center.central.data.CentralNetifDO;

public interface CentralNetifService {

  /**
   * 查询接口信息
   * @param monitoredSerialNumber
   * @param categorys
   * @return
   */
  List<CentralNetifBO> queryCentralNetifProfiles(String deviceType, String monitoredSerialNumber,
      String... categorys);

  /**
   * 按接口查询流量
   * @param monitoredSerialNumber
   * @param netifName
   * @param categoryList
   * @param interval
   * @param startTime
   * @param endTime
   * @return
   */
  List<CentralNetifBO> queryCentralNetifs(String deviceType, String monitoredSerialNumber,
      String netifName, List<String> categoryList, int interval, Date startTime, Date endTime);

  /**
   * 查询所有fpc的接收总流量曲线图
   * @param interval
   * @param startTime
   * @param endTime
   * @return
   */
  List<CentralNetifBO> queryTotalReceivingNetifs(int interval, Date startTime, Date endTime);

  /**
   * 查询接口使用率排行
   * @param number
   * @return
   */
  List<CentralNetifUsage> queryNetifUsagesByRanking(int number);

  /**
   * 统计本地设备的接口流量数据
   * @param metricTime
   * @return
   */
  int statisticCentralNetifs(Date metricTime);

  /**
   * 采集fpc上报的接口流量数据
   * @param netifMetricList
   */
  void collectCentralNetifs(List<CentralNetifDO> centralNetifList);

  /**
   * 聚合
   */
  void rollupCentralNetifs();

  /**
   * 清除历史数据
   * @param clearTime
   * @param interval
   * @return
   */
  int clearHisCentralNetifs(Date clearTime, int interval);

  /**
   * 根据monitoredSerialNumber删除指定设备的接口数据
   * @param monitoredSerialNumber
   * @return
   */
  int deleteCentralNetifs(String deviceType, String monitoredSerialNumber);

}
