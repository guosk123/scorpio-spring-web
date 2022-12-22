package com.machloop.fpc.manager.metric.dao;

import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

/**
 * @author chenxiao
 * create at 2022/8/22
 */
public interface MetricDnsDataRecordDao {
  Page<Map<String, Object>> queryDnsLogRecordAsGraph(PageRequest page, LogRecordQueryVO queryVO);

  long countDnsLogStatistics(LogRecordQueryVO queryVO, String sortProperty);
}
