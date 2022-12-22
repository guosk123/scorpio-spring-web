package com.machloop.fpc.cms.npm.analysis.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/13 10:47 AM,cms
 * @version 1.0
 */
public interface SuricataAlertStatisticsService {

  List<Map<String, Object>> queryAlertStatistics(String dsl, int topCount);

  List<Map<String, Object>> queryAlterStatistics(String type, Date startTime, Date endTime);

  List<Map<String, Object>> queryAlertDateHistogram(String dsl, int interval);
}
