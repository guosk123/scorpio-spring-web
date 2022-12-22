package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricL3DeviceDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2020年12月10日, fpc-manager
 */
public interface MetricL3DeviceDataRecordDao {

  List<Map<String, Object>> queryMetricL3DeviceRawdatas(MetricQueryVO queryVO);

  List<MetricL3DeviceDataRecordDO> queryMetricL3Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  List<Map<String, Object>> queryMetricL3DevicesEstablishedFail(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);

  /**
   * 统计三层主机TOP
   * @param queryVO
   * @param aggsField 聚合字段
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @param networkIds 主网络ID集合
   * @return
   */
  List<Map<String, Object>> countMetricL3Devices(MetricQueryVO queryVO, String aggsField,
      String sortProperty, String sortDirection);

  /**
   * 统计三层主机的时间曲线
   * @param queryVO
   * @param aggsField
   * @param combinationConditions
   * @return 需要统计的三层主机集合
   */
  List<Map<String, Object>> queryMetricL3DeviceHistograms(MetricQueryVO queryVO, String aggsField,
      List<Map<String, Object>> combinationConditions);

  List<Map<String, Object>> queryMetricNetworkSegmentationL3Devices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);
}
