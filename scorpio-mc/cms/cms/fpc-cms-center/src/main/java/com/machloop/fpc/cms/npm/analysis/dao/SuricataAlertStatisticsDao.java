package com.machloop.fpc.cms.npm.analysis.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/13 10:55 AM,cms
 * @version 1.0
 */
public interface SuricataAlertStatisticsDao {

  List<Map<String, Object>> queryAlertStatistics(String dsl, int topCount);

  List<Map<String, Object>> queryAlertStatistics(String type, Date startTime, Date endTime);

  List<Map<String, Object>> queryAlertDateHistogram(String dsl, int interval);
}
