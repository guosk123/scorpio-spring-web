package com.machloop.fpc.manager.appliance.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;

/**
 * @author mazhiyuan
 *
 *         create at 2020年2月19日, fpc-manager
 */
public interface FlowLogServiceCk {

  Page<Map<String, Object>> queryFlowLogs(String queryId, Pageable page, FlowLogQueryVO queryVO,
      String columns);

  /**
   * 根据flow_id查询会话的流日志，合并返回
   * @param queryId
   * @param page
   * @param queryVO
   * @param columns
   * @return
   */
  List<Map<String, Object>> queryFlowLogsByFlowIds(String queryId, PageRequest page,
      FlowLogQueryVO queryVO, String columns);

  List<Map<String, Object>> queryFlowLogsGroupByFlow(FlowLogQueryVO queryVO, String l7ProtocolName,
      List<String> flowIds);

  Map<String, Object> queryFlowLogStatistics(String queryId, FlowLogQueryVO queryVO);

  List<Map<String, Object>> queryFlowLogStatisticsGroupByIp(String queryId, FlowLogQueryVO queryVO,
      int termSize, Sort sort);

  void exportFlowLogs(String queryId, FlowLogQueryVO queryVO, String columns, int terminateAfter,
      int timeout, double samplingRate, Sort sort, String fileType, int count, OutputStream out)
      throws IOException;

  void analyzeFlowPacket(String flowPacketId, Date startTime, Date endTime, String type,
      String parameter, HttpServletRequest request, HttpServletResponse response);

  Map<String, Object> fetchFlowLogPacketFileUrls(String queryId, String fileType,
      FlowLogQueryVO queryVO, Sort sort);

  String fetchFlowLogPacketFileUrls(String flowPacketId, Date startTime, Date endTime,
      String remoteAddr);
}
