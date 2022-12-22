package com.machloop.fpc.cms.center.appliance.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.cms.center.appliance.vo.FlowLogQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年2月19日, fpc-manager
 */
public interface FlowLogService {

  Page<Map<String, Object>> queryFlowLogs(String queryId, Pageable page, FlowLogQueryVO queryVO,
      String columns);

  /**
   * 根据flow_id查询单个会话的所有流日志，合并返回
   * @param queryId
   * @param queryVO
   * @return
   */
  List<Map<String, Object>> queryFlowLogByFlowId(String queryId, FlowLogQueryVO queryVO,
      String columns);

  /**
   * 根据多个flow_id查询会话的流日志，合并返回
   * @param queryId
   * @param flowIds
   * @param page
   * @param queryVO
   * @param columns
   * @return
   */
  Page<Map<String, Object>> queryFlowLogsByFlowIds(String queryId, List<String> flowIds,
      PageRequest page, FlowLogQueryVO queryVO, String columns);

  List<Map<String, Object>> queryFlowLogsGroupByFlow(String startTime, String endTime,
      String l7ProtocolName, List<String> flowIds);

  Map<String, Object> queryFlowLogStatistics(String queryId, FlowLogQueryVO queryVO);

  void exportFlowLogs(String queryId, FlowLogQueryVO queryVO, String columns, int terminateAfter,
      int timeout, double samplingRate, Sort sort, String fileType, int count, OutputStream out)
      throws IOException;
}
