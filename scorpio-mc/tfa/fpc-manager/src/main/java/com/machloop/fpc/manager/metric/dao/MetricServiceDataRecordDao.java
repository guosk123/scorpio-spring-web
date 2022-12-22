package com.machloop.fpc.manager.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.metric.data.MetricServiceDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public interface MetricServiceDataRecordDao {

  /**
   * 汇总时间范围内各个业务的统计数据
   * @param queryVO
   * @param page 分页
   * @param sortProperty
   * @param sortDirection
   * @param metrics 统计指标类型集合
   * @param serviceNetworks 业务、网络组合条件
   * @return
   */
  Page<MetricServiceDataRecordDO> queryMetricServices(MetricQueryVO queryVO, Pageable page,
      String sortProperty, String sortDirection, List<String> metrics,
      List<Tuple2<String, String>> serviceNetworks);

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
