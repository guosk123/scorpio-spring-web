package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricL7ProtocolDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public interface MetricL7ProtocolDataRecordDao {

  List<Map<String, Object>> queryMetricL7ProtocolRawdatas(MetricQueryVO queryVO);

  List<MetricL7ProtocolDataRecordDO> queryMetricL7Protocols(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);

  /**
   * 统计全部协议
   * @param queryVO
   * @param aggsField 聚合字段
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @return
   */
  List<Map<String, Object>> countMetricL7Protocols(MetricQueryVO queryVO, String aggsField,
      String sortProperty, String sortDirection);

  /**
   * 统计多个协议的时间曲线
   * @param queryVO
   * @param aggsField
   * @param l7ProtocolIds 需要统计的协议集合
   * @return
   */
  List<Map<String, Object>> queryMetricL7ProtocolHistograms(MetricQueryVO queryVO, String aggsField,
      List<String> l7ProtocolIds);
}
