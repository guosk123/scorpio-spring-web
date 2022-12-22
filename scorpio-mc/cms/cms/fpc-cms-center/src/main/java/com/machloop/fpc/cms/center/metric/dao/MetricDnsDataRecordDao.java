package com.machloop.fpc.cms.center.metric.dao;

import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
public interface MetricDnsDataRecordDao {
  Page<Map<String, Object>> queryDnsLogRecordAsGraph(PageRequest page, LogRecordQueryVO queryVO);

  long countDnsLogStatistics(LogRecordQueryVO queryVO, String sortProperty);
}
