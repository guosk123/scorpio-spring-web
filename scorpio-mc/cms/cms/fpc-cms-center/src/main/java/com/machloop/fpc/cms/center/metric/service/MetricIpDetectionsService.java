package com.machloop.fpc.cms.center.metric.service;

import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.cms.center.metric.bo.MetricIpDetectionsLayoutsBO;
import com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
public interface MetricIpDetectionsService {

  Page<Map<String, Object>> queryFlowLogsAsHistogram(MetricFlowLogQueryVO queryVO, PageRequest page,
      String queryProperty);

  Page<Map<String, Object>> queryDnsLogRecordAsGraph(PageRequest page, LogRecordQueryVO queryVO);

  Map<String, Object> queryIpDetectionsLayouts(String id);

  MetricIpDetectionsLayoutsBO updateIpDetectionsLayouts(
      MetricIpDetectionsLayoutsBO metricIpDetectionsLayoutsBO, String id);

  Map<String, Object> queryFlowLogsStatistics(MetricFlowLogQueryVO queryVO, String queryProperty);

  Map<String, Object> queryDnsLogRecordStatistics(LogRecordQueryVO queryVO, String sortProperty);
}
