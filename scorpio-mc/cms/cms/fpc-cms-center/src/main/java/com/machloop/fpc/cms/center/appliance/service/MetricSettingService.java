package com.machloop.fpc.cms.center.appliance.service;

import java.util.List;

import com.machloop.fpc.cms.center.appliance.bo.MetricSettingBO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
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
