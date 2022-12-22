package com.machloop.fpc.manager.metric.service;

import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.manager.metric.bo.MetricIpDetectionsLayoutsBO;
import com.machloop.fpc.manager.metric.vo.MetricFlowLogQueryVO;

/**
 * @author chenxiao
 * create at 2022/8/22
 */
public interface MetricIpDetectionsService {


  Page<Map<String, Object>> queryFlowLogsAsHistogram(MetricFlowLogQueryVO queryVO, PageRequest page,
      String queryProperty);

  Page<Map<String, Object>> queryDnsLogRecordAsGraph(PageRequest page, LogRecordQueryVO queryVO);

  Map<String, Object> queryIpDetectionsLayouts(String operatorId);

  MetricIpDetectionsLayoutsBO updateIpDetectionsLayouts(
      MetricIpDetectionsLayoutsBO metricIpDetectionsLayoutsBO, String operatorId);

  Map<String, Object> queryFlowLogsStatistics(MetricFlowLogQueryVO queryVO, String queryProperty);

  Map<String, Object> queryDnsLogRecordStatistics(LogRecordQueryVO queryVO, String sortProperty);
}
