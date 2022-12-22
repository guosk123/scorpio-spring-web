package com.machloop.fpc.manager.appliance.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.manager.appliance.dao.clickhouse.FlowLogDaoImpl.QueryCanceledException;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author mazhiyuan
 *
 * create at 2020年2月19日, fpc-manager
 */
public interface FlowLogDaoCk {

  /**
   * 
   * @param queryId
   * @param page
   * @param queryVO
   * @param columns
   * @param ids ID集合（id：reportTime_flowId）
   * @return
   */
  Page<Map<String, Object>> queryFlowLogs(String queryId, Pageable page, FlowLogQueryVO queryVO,
      String columns, List<String> ids);

  /**
   * 导出流日志时，获取flowId_reportTime集合
   * @param queryId
   * @param queryVO
   * @param columns
   * @param ids ID集合（id：reportTime_flowId）
   * @param sort
   * @param size
   * @return t1：表名；t2：id集合
   */
  Tuple2<String, List<String>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO,
      List<String> ids, Sort sort, int size);

  Tuple2<String, List<String>> queryFlowLogsByFlowIds(String queryId, FlowLogQueryVO queryVO,
      List<String> flowIds, Sort sort, int count);

  List<Map<String, Object>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO,
      String l7ProtocolId, List<String> flowIds, int size);

  List<Map<String, Object>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO,
      List<String> flowIds, Pageable page, String columns);

  /**
   * 根据flow_id查询单个会话的所有流日志
   * @param queryId
   * @param queryVO
   * @return
   */
  List<Map<String, Object>> queryFlowLogByFlowId(String queryId, FlowLogQueryVO queryVO,
      String columns);

  /**
   * 
   * @param queryId
   * @param queryVO
   * @param ids ID集合（id：reportTime_flowId）
   * @return
   */
  long countFlowLogs(String queryId, FlowLogQueryVO queryVO, List<String> ids);

  /**
   * 通过ID集合查询会话详单
   * @param queryId
   * @param tableName
   * @param columns
   * @param sort
   * @param ids ID集合（id：reportTime_flowId）
   * @return
   */
  List<Map<String, Object>> queryFlowLogsByIds(String queryId, String tableName, String columns,
      Sort sort, List<String> ids);

  List<Map<String, Object>> queryFlowLogGroupByStatistics(String queryId, FlowLogQueryVO queryVO,
      String termFieldName, int termSize) throws QueryCanceledException;

  List<Map<String, Object>> queryFlowLogDateHistogramStatistics(String queryId,
      FlowLogQueryVO queryVO, int histogramInterval) throws QueryCanceledException;

  List<Map<String, Object>> queryFlowLogStatisticsGroupByIp(String queryId, FlowLogQueryVO queryVO,
      int termSize, Sort sort);

  List<Object> queryFlowIds(String queryId, FlowLogQueryVO queryVO, List<String> ids, Sort sort,
      int size);
}
