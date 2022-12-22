package com.machloop.fpc.manager.metric.service.impl;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.global.service.SlowQueryService;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.manager.metric.bo.MetricIpDetectionsLayoutsBO;
import com.machloop.fpc.manager.metric.dao.MetricDnsDataRecordDao;
import com.machloop.fpc.manager.metric.dao.MetricFlowlogDataRecordDao;
import com.machloop.fpc.manager.metric.dao.MetricIpDetectionsLayoutsDao;
import com.machloop.fpc.manager.metric.data.MetricIpDetectionsLayoutsDO;
import com.machloop.fpc.manager.metric.service.MetricIpDetectionsService;
import com.machloop.fpc.manager.metric.vo.MetricFlowLogQueryVO;

/**
 * @author chenxiao
 * create at 2022/8/22
 */
@Service
public class MetricIpDetectionsServiceImpl implements MetricIpDetectionsService {


  @Autowired
  private MetricFlowlogDataRecordDao metricFlowlogDataRecordDao;

  @Autowired
  private MetricDnsDataRecordDao metricDnsDataRecordDao;

  @Autowired
  private MetricIpDetectionsLayoutsDao metricIpDetectionsLayoutsDao;

  @Autowired
  private SlowQueryService slowQueryService;


  @Override
  public Page<Map<String, Object>> queryFlowLogsAsHistogram(MetricFlowLogQueryVO queryVO,
      PageRequest page, String queryProperty) {

    return metricFlowlogDataRecordDao.queryFlowLogsAsHistogram(queryVO, page, queryProperty);
  }

  @Override
  public Map<String, Object> queryFlowLogsStatistics(MetricFlowLogQueryVO queryVO,
      String queryProperty) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    long total = metricFlowlogDataRecordDao.countFlowLogsStatistics(queryVO, queryProperty);
    result.put("total", total);

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }
    return result;
  }

  @Override
  public Page<Map<String, Object>> queryDnsLogRecordAsGraph(PageRequest page,
      LogRecordQueryVO queryVO) {
    return metricDnsDataRecordDao.queryDnsLogRecordAsGraph(page, queryVO);
  }

  @Override
  public Map<String, Object> queryDnsLogRecordStatistics(LogRecordQueryVO queryVO,
      String sortProperty) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    long total = metricDnsDataRecordDao.countDnsLogStatistics(queryVO, sortProperty);
    result.put("total", total);
    return result;
  }

  @Override
  public Map<String, Object> queryIpDetectionsLayouts(String operatorId) {
    return metricIpDetectionsLayoutsDao.queryIpDetectionsLayouts(operatorId);
  }

  @Override
  public MetricIpDetectionsLayoutsBO updateIpDetectionsLayouts(
      MetricIpDetectionsLayoutsBO metricIpDetectionsLayoutsBO, String operatorId) {
    MetricIpDetectionsLayoutsDO metricIpDetectionsLayoutsDO = new MetricIpDetectionsLayoutsDO();
    BeanUtils.copyProperties(metricIpDetectionsLayoutsBO, metricIpDetectionsLayoutsDO);
    metricIpDetectionsLayoutsDO.setOperatorId(operatorId);

    Map<String, Object> exist = metricIpDetectionsLayoutsDao.queryIpDetectionsLayouts(operatorId);
    if (StringUtils.isBlank(MapUtils.getString(exist, "id"))) {
      metricIpDetectionsLayoutsDao.saveIpDetectionsLayouts(metricIpDetectionsLayoutsDO);
    } else {
      metricIpDetectionsLayoutsDO.setId(MapUtils.getString(exist, "id"));
      metricIpDetectionsLayoutsDao.updateIpDetectionsLayouts(metricIpDetectionsLayoutsDO);
    }
    BeanUtils.copyProperties(metricIpDetectionsLayoutsDO, metricIpDetectionsLayoutsBO);
    return metricIpDetectionsLayoutsBO;
  }
}
