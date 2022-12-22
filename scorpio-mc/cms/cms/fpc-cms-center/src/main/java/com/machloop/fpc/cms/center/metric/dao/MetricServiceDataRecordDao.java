package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricServiceDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public interface MetricServiceDataRecordDao {

  /**
   * 汇总时间范围内各个业务的统计数据
   * @param queryVO
   * @param metrics 统计指标类型集合
   * @param metricField 单个指标字段（metrics互斥）
   */
  List<MetricServiceDataRecordDO> queryMetricServices(MetricQueryVO queryVO, List<String> metrics,
      String metricField);

  /**
   * 不分组统计业务数据
   * @param queryVO
   * @param metrics
   * @return
   */
  MetricServiceDataRecordDO queryMetricService(MetricQueryVO queryVO, List<String> metrics);

  /**
   * 查询单个业务的趋势图
   * @param queryVO
   * @param extendedBound
   * @param metrics 统计指标类型集合
   * @return
   */
  List<MetricServiceDataRecordDO> queryMetricServiceHistograms(MetricQueryVO queryVO,
      boolean extendedBound, List<String> metrics);

  /**
   * 查询单业务的趋势图
   * @param queryVO
   * @param extendedBound
   * @param aggsFields 统计指标集合（具体的字段）
   * @return
   */
  List<Map<String, Object>> queryMetricServiceHistogramsWithAggsFields(MetricQueryVO queryVO,
      boolean extendedBound, List<String> aggsFields);

  List<Map<String, Object>> queryMetricNetworkSegmentationServices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);
}
