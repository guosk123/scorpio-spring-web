package com.machloop.fpc.manager.system.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.statistics.bo.TimeseriesBO;
import com.machloop.fpc.manager.system.bo.DeviceNetifBO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public interface DeviceNetifService {

  List<DeviceNetifBO> queryDeviceNetifs();

  List<DeviceNetifBO> queryDeviceNetifsWithBandwidth();

  List<DeviceNetifBO> queryDeviceNetifsByCategories(String... category);

  TimeseriesBO queryNetifUsage(String netifName, String interval, Date startTime, Date endTime);

  TimeseriesBO queryNetifTotalUsage(String interval, Date startTime, Date endTime,
      List<DeviceNetifBO> deviceNetifBOList, String type);

  Map<String, TimeseriesBO> queryIngestRxNetifTotalUsage(String interval, Date startTime,
      Date endTime);

  List<DeviceNetifBO> batchUpdateDeviceNetifs(List<DeviceNetifBO> netifBOList, String operatorId);

  int monitorNetifState();

  int statisticNetifUsage(Date metricDate);

}
