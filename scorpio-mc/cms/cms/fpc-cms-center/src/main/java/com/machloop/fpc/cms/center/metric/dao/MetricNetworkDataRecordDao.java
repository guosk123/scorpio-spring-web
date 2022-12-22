package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricNetworkDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public interface MetricNetworkDataRecordDao {

  /**
   * 根据网络分组统计网络数据
   * @param queryVO
   * @param metrics 统计指标类型集合
   * @param sortProperty
   * @param sortDirection
   * @return
   */
  List<MetricNetworkDataRecordDO> queryMetricNetworks(MetricQueryVO queryVO, List<String> metrics,
      String sortProperty, String sortDirection);

  /**
   * 不分组统计网络数据
   * @param queryVO
   * @param metrics
   * @return
   */
  MetricNetworkDataRecordDO queryMetricNetwork(MetricQueryVO queryVO, List<String> metrics);

  /**
   * 查询单个网络（组）的趋势图
   * @param queryVO
   * @param extendedBound
   * @param metrics 统计指标类型集合
   * @return
   */
  List<MetricNetworkDataRecordDO> queryMetricNetworkHistograms(MetricQueryVO queryVO,
      boolean extendedBound, List<String> metrics);

  /**
   * 查询单个网络（组）的趋势图
   * @param queryVO
   * @param extendedBound
   * @param aggsFields 统计指标集合（具体的字段）
   * @return
   */
  List<Map<String, Object>> queryMetricNetworkHistogramsWithAggsFields(MetricQueryVO queryVO,
      boolean extendedBound, List<String> aggsFields);

}
