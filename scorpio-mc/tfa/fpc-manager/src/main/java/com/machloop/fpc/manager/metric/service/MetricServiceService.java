package com.machloop.fpc.manager.metric.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.metric.bo.MetricDashboardSettingsBO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2021年4月22日, fpc-manager
 */
public interface MetricServiceService {

  /**
   * 所有业务统计
   * @param queryVO
   * @param page
   * @param sortProperty
   * @param sortDirection
   * @param name
   * @param isFollow
   * @param currentUserId
   * @return
   */
  Page<Map<String, Object>> queryMetricServices(MetricQueryVO queryVO, Pageable page,
      String sortProperty, String sortDirection, String name, String isFollow,
      String currentUserId);

  /**
   * 单业务趋势图
   * @param queryVO
   * @param extendedBound
   * @return
   */
  List<Map<String, Object>> queryMetricServiceHistograms(MetricQueryVO queryVO,
      boolean extendedBound);

  /**
   * 单业务概览
   * @param queryVO
   * @return
   */
  Map<String, Object> queryServiceDashboard(MetricQueryVO queryVO, HttpServletRequest request);

  /**
   * 负载量统计
   * @param queryVO
   * @return
   */
  List<Map<String, Object>> queryPayloadStatistics(MetricQueryVO queryVO,
      HttpServletRequest request);

  /**
   * 性能统计
   * @param queryVO
   * @return
   */
  List<Map<String, Object>> queryPerformanceStatistics(MetricQueryVO queryVO,
      HttpServletRequest request);

  /**
   * TCP指标
   * @param queryVO
   * @return
   */
  List<Map<String, Object>> queryTcpStatistics(MetricQueryVO queryVO, HttpServletRequest request);

  Map<String, Object> queryDashboardSettings(String operatorId);

  MetricDashboardSettingsBO updateDashboardSettings(
      MetricDashboardSettingsBO metricDashboardSettingsBO, String operatorId);
}
