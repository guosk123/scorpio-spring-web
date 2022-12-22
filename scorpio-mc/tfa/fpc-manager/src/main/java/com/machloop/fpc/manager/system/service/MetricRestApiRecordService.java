package com.machloop.fpc.manager.system.service;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.system.vo.MetricRestApiQueryVO;

public interface MetricRestApiRecordService {


  Page<Map<String, Object>> queryMetricRestApiRecords(MetricRestApiQueryVO queryVO,
      PageRequest page, String sortProperty, String sortDirection);

  List<Map<String, Object>> queryUserTop(MetricRestApiQueryVO queryVO);

  List<Map<String, Object>> queryApiTop(MetricRestApiQueryVO queryVOt);

  List<Map<String, Object>> queryUserList(MetricRestApiQueryVO queryVO);

  List<Map<String, Object>> queryApiList(MetricRestApiQueryVO queryVO);
}
