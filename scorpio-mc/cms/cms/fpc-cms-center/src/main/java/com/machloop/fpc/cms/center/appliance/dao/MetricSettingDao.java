package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.MetricSettingDO;


/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public interface MetricSettingDao {

  List<MetricSettingDO> queryMetricSettings(String sourceType, String networkId, String serviceId,
      String packetFileId);

  List<MetricSettingDO> queryMetricSettings();

  List<String> queryMetricSettingIds(String sourceType, String networkId, String serviceId,
      Date beforeTime);

  List<String> queryAssignMetricSettingIds(String sourceType, Date beforeTime);

  int batchSaveMetricSetting(List<MetricSettingDO> metricSettings);

  int updateMetricSetting(MetricSettingDO metricSettingDO);

  int saveOrUpdateMetricSetting(MetricSettingDO metricSettingDO);

  int deleteMetricSetting(String sourceType, String networkId, String serviceId,
      String packetFileId);
}
