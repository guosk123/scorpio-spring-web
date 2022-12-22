package com.machloop.fpc.manager.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.metric.data.MetricNetworkDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public interface MetricNetworkDataRecordDao {

  /**
   * 汇总时间范围内各个网络的统计数据
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @param metrics 统计指标类型集合
   * @param networkIds 网络ID集合
   * @return
   */
  List<MetricNetworkDataRecordDO> queryMetricNetworks(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, List<String> metrics, List<String> networkIds);

  /**
   * 查询单个网络的趋势图
   * @param queryVO
   * @param extendedBound
   * @param metrics 统计指标类型集合
   * @return
   */
  List<MetricNetworkDataRecordDO> queryMetricNetworkHistograms(MetricQueryVO queryVO,
      boolean extendedBound, List<String> metrics);

  /**
   * 查询单个网络的趋势图
   * @param queryVO
   * @param extendedBound
   * @param aggsFields 统计指标集合（具体的字段）
   * @return
   */
  List<Map<String, Object>> queryMetricNetworkHistogramsWithAggsFields(MetricQueryVO queryVO,
      boolean extendedBound, List<String> aggsFields);

  /**
   * 查询所有主网络统计信息
   * @param queryVO
   * @param networkIds
   * @param extendedBound
   * @param metrics 统计指标类型集合
   * @return
   */
  List<MetricNetworkDataRecordDO> queryAllNetworkHistograms(MetricQueryVO queryVO,
      List<String> networkIds, boolean extendedBound, List<String> metrics);
}
