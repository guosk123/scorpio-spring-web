package com.machloop.fpc.cms.center.metric.dao;

import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricDashboardSettingsDO;

/**
 * @author chenxiao
 * create at 2022/7/15
 */
public interface MetricDashboardSettingsDao {

  Map<String, Object> queryDashboardSettings(String operatorId);

  MetricDashboardSettingsDO queryDashboardSettingsByOperatorId(String operatorId);

  MetricDashboardSettingsDO saveDashboardSettings(
      MetricDashboardSettingsDO metricDashboardSettingsDO);

  int updateDashboardSettings(MetricDashboardSettingsDO metricDashboardSettingsDO);
}
