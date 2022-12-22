package com.machloop.fpc.npm.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.npm.appliance.data.MetricSettingDO;

/**
 * @author guosk
 *
 * create at 2021年4月6日, fpc-manager
 */
public interface MetricSettingDao {

  List<MetricSettingDO> queryMetricSettings(String sourceType, String networkId, String serviceId,
      String packetFileId);

  List<String> queryAssignMetricSettingIds(String sourceType, String networkId, String serviceId,
      Date beforeTime);

  int batchSaveMetricSetting(List<MetricSettingDO> metricSettings);

  int updateMetricSetting(MetricSettingDO metricSettingDO);

  int deleteMetricSetting(String sourceType, String networkId, String serviceId,
      String packetFileId);

}
