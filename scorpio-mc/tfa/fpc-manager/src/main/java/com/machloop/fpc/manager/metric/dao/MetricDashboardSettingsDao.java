package com.machloop.fpc.manager.metric.dao;

import java.util.Map;

import com.machloop.fpc.manager.metric.data.MetricDashboardSettingsDO;

/**
 * @author chenxiao
 * create at 2022/7/14
 */
public interface MetricDashboardSettingsDao {
  Map<String, Object> queryDashboardSettings(String operatorId);

  MetricDashboardSettingsDO queryDashboardSettingsByOperatorId(String operatorId);

  MetricDashboardSettingsDO saveDashboardSettings(
      MetricDashboardSettingsDO metricDashboardSettingsDO);

  int updateDashboardSettings(MetricDashboardSettingsDO metricDashboardSettingsDO);
}
