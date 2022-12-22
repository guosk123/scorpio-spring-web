package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricIpConversationDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2021年4月22日, fpc-manager
 */
public interface MetricIpConversationDataRecordDao {

  List<Map<String, Object>> queryMetricIpConversationRawdatas(MetricQueryVO queryVO);

  List<MetricIpConversationDataRecordDO> queryMetricIpConversations(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);

  List<Map<String, Object>> graphMetricIpConversations(MetricQueryVO queryVO,
      Integer minEstablishedSessions, Integer minTotalBytes, String sortProperty,
      String sortDirection);

  /**
   * 统计IP对 TOP
   * @param queryVO
   * @param aggsField 聚合字段
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @param networkIds 主网络ID集合
   * @return
   */
  List<Map<String, Object>> countMetricIpConversations(MetricQueryVO queryVO, String aggsField,
      String sortProperty, String sortDirection);

  /**
   * 统计IP通讯对的时间曲线
   * @param queryVO
   * @param aggsField
   * @param combinationConditions 组合条件
   * @return
   */
  List<Map<String, Object>> queryMetricIpConversationHistograms(MetricQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions);

  List<Map<String, Object>> queryMetricNetworkSegmentationIpConversations(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);
}
