package com.machloop.fpc.cms.center.appliance.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.cms.center.appliance.vo.FlowLogQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author mazhiyuan
 *
 * create at 2020年2月19日, fpc-manager
 */
public interface FlowLogDao {

  Page<Map<String, Object>> queryFlowLogs(String queryId, Pageable page, FlowLogQueryVO queryVO,
      String columns, List<String> flowIds);

  List<Map<String, Object>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO, String columns,
      List<String> flowIds, Sort sort, int size);

  List<Map<String, Object>> queryFlowLogs(String queryId, String startTime, String endTime,
      String l7ProtocolId, List<String> flowIds, int size);

  Tuple2<String, List<String>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO,
      List<String> flowIds, Sort sort, int size);

  List<Map<String, Object>> queryFlowLogsByIds(String queryId, String tableName, String columns,
      Sort sort, List<String> ids);

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

  long countFlowLogs(String queryId, FlowLogQueryVO queryVO, List<String> flowIds);

  Tuple2<String, List<String>> queryFlowLogsByFlowIds(String queryId, FlowLogQueryVO queryVO,
      List<String> flowIds, Sort sort, int count);
}
