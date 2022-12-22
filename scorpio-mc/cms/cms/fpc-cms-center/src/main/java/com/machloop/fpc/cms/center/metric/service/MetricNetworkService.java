package com.machloop.fpc.cms.center.metric.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2021年4月22日, fpc-manager
 */
public interface MetricNetworkService {

  /**
   * 所有网络统计
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @return
   */
  List<Map<String, Object>> queryMetricNetworks(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 单网络概览
   * @param queryVO
   * @return
   */
  Map<String, Object> queryNetworkDashboard(MetricQueryVO queryVO, HttpServletRequest request);

  /**
   * 查询网络总负载
   * @param queryVO
   * @return
   */
  Map<String, Object> queryNetworksTotalPayload(MetricQueryVO queryVO);

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

}
