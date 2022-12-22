package com.machloop.fpc.manager.metric.dao;

import java.util.Map;

import com.machloop.fpc.manager.metric.data.MetricIpDetectionsLayoutsDO;

/**
 * @author ChenXiao
 * create at 2022/11/15
 */
public interface MetricIpDetectionsLayoutsDao {
  Map<String, Object> queryIpDetectionsLayouts(String operatorId);

  MetricIpDetectionsLayoutsDO saveIpDetectionsLayouts(
      MetricIpDetectionsLayoutsDO metricIpDetectionsLayoutsDO);

  int updateIpDetectionsLayouts(MetricIpDetectionsLayoutsDO metricIpDetectionsLayoutsDO);
}
