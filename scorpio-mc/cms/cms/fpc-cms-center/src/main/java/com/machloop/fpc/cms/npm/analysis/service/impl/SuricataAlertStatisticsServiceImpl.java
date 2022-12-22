package com.machloop.fpc.cms.npm.analysis.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.machloop.fpc.cms.npm.analysis.dao.SuricataAlertStatisticsDao;
import com.machloop.fpc.cms.npm.analysis.service.SuricataAlertStatisticsService;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/13 10:47 AM,cms
 * @version 1.0
 */
@Service
public class SuricataAlertStatisticsServiceImpl implements SuricataAlertStatisticsService {

  @Autowired
  private SuricataAlertStatisticsDao suricataAlertStatisticsDao;

  @Override
  public List<Map<String, Object>> queryAlertStatistics(String dsl, int topCount) {

    return suricataAlertStatisticsDao.queryAlertStatistics(dsl, topCount);
  }

  @Override
  public List<Map<String, Object>> queryAlterStatistics(String type, Date startTime, Date endTime) {

    return suricataAlertStatisticsDao.queryAlertStatistics(type, startTime, endTime);
  }

  @Override
  public List<Map<String, Object>> queryAlertDateHistogram(String dsl, int interval) {

    return suricataAlertStatisticsDao.queryAlertDateHistogram(dsl, interval);
  }
}
