package com.machloop.fpc.manager.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.metric.data.MetricLocationDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public interface MetricLocationDataRecordDao {

  List<Map<String, Object>> queryMetricLocationRawdatas(MetricQueryVO queryVO);

  List<MetricLocationDataRecordDO> queryMetricLocations(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 统计地区的时间曲线
   * @param queryVO
   * @param aggsField
   * @param combinationConditions
   * @return
   */
  List<Map<String, Object>> queryMetricLocationHistograms(MetricQueryVO queryVO, String aggsField,
      List<Map<String, Object>> combinationConditions);
}
