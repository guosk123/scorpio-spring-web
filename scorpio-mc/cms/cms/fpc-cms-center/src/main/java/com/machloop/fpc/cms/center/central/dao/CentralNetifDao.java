package com.machloop.fpc.cms.center.central.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.central.bo.CentralNetifUsage;
import com.machloop.fpc.cms.center.central.data.CentralNetifDO;

/**
 * 本机及下级接口流量统计
 * @author guosk
 *
 * create at 2022年5月5日, fpc-cms-center
 */
public interface CentralNetifDao {

  /**
   * 查询接口信息
   * @param monitoredSerialNumber
   * @param categoryList
   * @return
   */
  List<CentralNetifDO> queryCentralNetifProfiles(String deviceType, String monitoredSerialNumber,
      List<String> categoryList);

  CentralNetifDO queryCentralNetifById(String id);

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
  List<CentralNetifDO> queryCentralNetifs(String deviceType, String monitoredSerialNumber,
      String netifName, List<String> categoryList, int interval, Date startTime, Date endTime);

  List<CentralNetifDO> queryCentralNetifs(String deviceType, String monitoredSerialNumber,
      List<Date> metricTimeList);

  /**
   * 查询各个设备上业务口总流量
   * @param monitoredSerialNumbers
   * @param interval
   * @param startTime
   * @param endTime
   * @return
   */
  List<CentralNetifDO> queryCentralNetifsBySerialNumbers(String deviceType,
      List<String> monitoredSerialNumbers, int interval, Date startTime, Date endTime);

  List<Date> queryCentralNetifMetricTime(String deviceType, String monitoredSerialNumber,
      List<Date> metricTimeList);

  /**
   * 查询所有fpc的接收总流量
   * @param interval
   * @param startTime
   * @param endTime
   * @return
   */
  List<CentralNetifDO> queryTotalReceivingNetifs(String deviceType,
      List<String> monitoredSerialNumbers, int interval, Date startTime, Date endTime);

  /**
   * 查询接口使用率排行
   * @return
   */
  List<CentralNetifUsage> queryNetifUsagesByRanking();

  /**
   * 将上报的接口流量数据保存到历史表
   * @param netifTraffics
   */
  void saveCentralNetifs(List<CentralNetifDO> netifTraffics);

  /**
   * 更新接口信息表
   * @param netifs
   */
  void saveOrUpdateCentralNetifs(List<CentralNetifDO> netifs);

  /**
   * 修改接口所属设备序列号
   * @param netif
   */
  int updateNetifBelongDeviceSerialNumber(String id, String monitoredSerialNumber);

  /**
   * 聚合接口流量
   * @param startTime
   * @param endTime
   * @return
   */
  int rollupCentralNetifs(Date startTime, Date endTime, String deviceType,
      String monitoredSerialNumber);

  /**
   * 定时清除表数据
   * @param beforeTime
   * @param interval
   * @return
   */
  int deleteCentralNetifs(Date beforeTime, int interval);

  /**
   * 删除设备时清除接口数据
   * @param monitoredSerialNumber
   * @return
   */
  int deleteCentralNetifs(String deviceType, String monitoredSerialNumber);

  int deleteCentralNetifs(String deviceType, String monitoredSerialNumber, Date metricTime,
      int interval);

}
