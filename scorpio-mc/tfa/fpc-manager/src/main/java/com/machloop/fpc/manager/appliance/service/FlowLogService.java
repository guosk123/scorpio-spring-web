package com.machloop.fpc.manager.appliance.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;

/**
 * @author mazhiyuan
 *
 *         create at 2020年2月19日, fpc-manager
 */
public interface FlowLogService {

  String queryFlowLogs(FlowLogQueryVO queryVO, String queryTaskId, int terminateAfter, int timeout,
      double samplingRate, String sortProperty, String sortDirection, int pageSize,
      String searchAfter);

  List<Map<String, Object>> queryFlowLogs(String flowId, Date inclusiveTime);

  String queryFlowLogStatistics(FlowLogQueryVO queryVO, String queryTaskId, int histogramInterval,
      String termFieldName, int termSize, int terminateAfter, int timeout, double samplingRate);

  String queryFlowLogStatisticsGroupByIp(FlowLogQueryVO queryVO, String queryTaskId, int termSize,
      int timeout, String sortProperty, String sortDirection);

  void exportFlowLogs(FlowLogQueryVO queryVO, int terminateAfter, int timeout, double samplingRate,
      String sortProperty, String sortDirection, OutputStream out) throws IOException;

  void analyzeFlowPacket(String flowPacketId, Date startTime, Date endTime, String type,
      String parameter, HttpServletRequest request, HttpServletResponse response);

  String fetchFlowLogPacketFileUrls(String flowPacketId, Date startTime, Date endTime,
      String remoteAddr);

  void cancelFlowLogsQueryTask(String queryTaskId);

}
