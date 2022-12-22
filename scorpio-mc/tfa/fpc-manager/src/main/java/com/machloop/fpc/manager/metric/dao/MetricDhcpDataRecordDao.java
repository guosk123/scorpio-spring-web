package com.machloop.fpc.manager.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.metric.data.MetricDhcpDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author guosk
 *
 * create at 2020年12月10日, fpc-manager
 */
public interface MetricDhcpDataRecordDao {

  List<Map<String, Object>> queryMetricDhcpRawdatas(MetricQueryVO queryVO);

  List<MetricDhcpDataRecordDO> queryMetricDhcps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String type);

  /**
   * 统计多个客户端/服务端/消息类型的时间曲线
   * @param queryVO
   * @param termFields:
   * 服务端聚合时: {serverIpAddress：需要筛选的"服务端IP"集合,serverMacAddress:需要筛选的"服务端mac"集合}
   * 客户端聚合时: {clientIpAddress：需要筛选的"客户端IP"集合,clientMacAddress:需要筛选的"客户端mac"集合}
   * 消息类型聚合时：{messageType：需要筛选的消息类型集合}
   * @param aggsField 聚合字段
   * @param combinationConditions 组合条件
   * @return
   */
  List<Map<String, Object>> queryMetricDhcpHistograms(MetricQueryVO queryVO,
      List<Tuple2<String, Boolean>> termFields, String aggsField,
      List<Map<String, Object>> combinationConditions);

}
