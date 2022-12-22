package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author mazhiyuan
 *
 * create at 2020年2月19日, fpc-manager
 */
public interface FlowLogDao {
  String queryFlowLogs(FlowLogQueryVO queryVO, String queryTaskId, int terminateAfter, int timeout,
      double samplingRate, String sortProperty, String sortDirection, int size, String searchAfter);

  Tuple2<String, List<Map<String, Object>>> queryFlowLogs(FlowLogQueryVO queryVO,
      int terminateAfter, int timeout, double samplingRate, String sortProperty,
      String sortDirection, int size, String scrollId);

  List<Map<String, Object>> queryFlowLogs(String flowId, Date startTime);

  String queryFlowLogStatistics(FlowLogQueryVO queryVO, String queryTaskId, int histogramInterval,
      String termFieldName, int termSize, int terminateAfter, int timeout, double samplingRate);

  String queryFlowLogStatisticsGroupByIp(FlowLogQueryVO queryVO, String queryTaskId, int termSize,
      int timeout, String sortProperty, String sortDirection);

  void cancelFlowLogsQueryTask(String queryTaskId);

  boolean clearScroll(String scrollId);

}
