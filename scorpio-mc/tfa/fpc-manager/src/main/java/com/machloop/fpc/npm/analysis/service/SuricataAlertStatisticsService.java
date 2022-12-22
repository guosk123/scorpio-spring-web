package com.machloop.fpc.npm.analysis.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author guosk
 *
 * create at 2022年4月11日, fpc-manager
 */
public interface SuricataAlertStatisticsService {

  List<Map<String, Object>> queryAlertStatistics(String dsl, int topCount);

  List<Map<String, Object>> queryAlertStatistics(String type, Date startTime, Date endTime);

  List<Map<String, Object>> queryAlertDateHistogram(String dsl, int interval);

}
