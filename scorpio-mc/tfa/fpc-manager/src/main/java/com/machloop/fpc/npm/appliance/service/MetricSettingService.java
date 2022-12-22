package com.machloop.fpc.npm.appliance.service;

import java.util.List;

import com.machloop.fpc.npm.appliance.bo.MetricSettingBO;

/**
 * @author guosk
 *
 * create at 2021年4月6日, fpc-manager
 */
public interface MetricSettingService {

  List<MetricSettingBO> queryMetricSettings(String sourceType, String networkId, String serviceId,
      String packetFileId);

  int saveDefaultMetricSettings(List<MetricSettingBO> metricSettings, String operatorId);

  int saveMetricSettings(List<MetricSettingBO> metricSettings, String operatorId);

  void updateMetricSettings(List<MetricSettingBO> metricSettings, String operatorId);

  int deleteMetricSetting(String sourceType, String networkId, String serviceId,
      String packetFileId);
}
