package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricPortDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public interface MetricPortDataRecordDao {

  List<Map<String, Object>> queryMetricPortRawdatas(MetricQueryVO queryVO);

  List<MetricPortDataRecordDO> queryMetricPorts(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 统计端口的时间曲线
   * @param queryVO
   * @param aggsField
   * @param ports 需要统计端口集合
   * @return
   */
  List<Map<String, Object>> queryMetricPortHistograms(MetricQueryVO queryVO, String aggsField,
      List<String> ports);

  List<Map<String, Object>> queryMetricNetworkSegmentationPorts(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);
}
