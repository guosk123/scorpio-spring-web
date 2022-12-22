package com.machloop.fpc.cms.center.metric.dao;

import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricIpDetectionsLayoutsDO;

/**
 * @author ChenXiao
 * create at 2022/11/25
 */
public interface MetricIpDetectionsLayoutsDao {
  Map<String, Object> queryIpDetectionsLayouts(String operatorId);

  MetricIpDetectionsLayoutsDO saveIpDetectionsLayouts(
      MetricIpDetectionsLayoutsDO metricIpDetectionsLayoutsDO);

  int updateIpDetectionsLayouts(MetricIpDetectionsLayoutsDO metricIpDetectionsLayoutsDO);
}
