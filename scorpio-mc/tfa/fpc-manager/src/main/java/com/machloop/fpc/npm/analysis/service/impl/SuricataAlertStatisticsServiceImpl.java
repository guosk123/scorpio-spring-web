package com.machloop.fpc.npm.analysis.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.machloop.fpc.npm.analysis.dao.SuricataAlertStatisticsDao;
import com.machloop.fpc.npm.analysis.service.SuricataAlertStatisticsService;

/**
 * @author guosk
 *
 * create at 2022年4月11日, fpc-manager
 */
@Service
public class SuricataAlertStatisticsServiceImpl implements SuricataAlertStatisticsService {

  @Autowired
  private SuricataAlertStatisticsDao suricataAlertStatisticsDao;

  /**
   * @see com.machloop.fpc.npm.analysis.service.SuricataAlertStatisticsService#queryAlertStatistics(java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> queryAlertStatistics(String dsl, int topCount) {

    return suricataAlertStatisticsDao.queryAlertStatistics(dsl, topCount);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.SuricataAlertStatisticsService#queryAlertStatistics(java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public List<Map<String, Object>> queryAlertStatistics(String type, Date startTime, Date endTime) {

    return suricataAlertStatisticsDao.queryAlertStatistics(type, startTime, endTime);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.service.SuricataAlertStatisticsService#queryAlertDateHistogram(java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> queryAlertDateHistogram(String dsl, int interval) {

    return suricataAlertStatisticsDao.queryAlertDateHistogram(dsl, interval);
  }

}
