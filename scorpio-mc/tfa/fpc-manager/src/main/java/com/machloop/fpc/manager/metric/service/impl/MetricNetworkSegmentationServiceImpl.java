package com.machloop.fpc.manager.metric.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.machloop.fpc.manager.metric.dao.*;
import com.machloop.fpc.manager.metric.service.MetricNetworkSegmentationService;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author chenxiao
 * create at 2022/8/24
 */
@Service
public class MetricNetworkSegmentationServiceImpl implements MetricNetworkSegmentationService {


  @Autowired
  private MetricL3DeviceDataRecordDao metricL3DeviceDataRecordDao;

  @Autowired
  private MetricIpConversationDataRecordDao metricIpConversationDataRecordDao;

  @Autowired
  private MetricApplicationDataRecordDao metricApplicationDataRecordDao;

  @Autowired
  private MetricPortDataRecordDao metricPortDataRecordDao;

  @Autowired
  private MetricServiceDataRecordDao metricServiceDataRecordDao;


  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationL3Devices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {

    return metricL3DeviceDataRecordDao.queryMetricNetworkSegmentationL3Devices(queryVO,
        sortProperty, sortDirection);
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationIpConversations(
      MetricQueryVO queryVO, String sortProperty, String sortDirection) {

    return metricIpConversationDataRecordDao.queryMetricNetworkSegmentationIpConversations(queryVO,
        sortProperty, sortDirection);
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {

    return metricApplicationDataRecordDao.queryMetricNetworkSegmentationApplications(queryVO,
        sortProperty, sortDirection);
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationPorts(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {

    return metricPortDataRecordDao.queryMetricNetworkSegmentationPorts(queryVO, sortProperty,
        sortDirection);
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationServices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {

    return metricServiceDataRecordDao.queryMetricNetworkSegmentationServices(queryVO, sortProperty,
        sortDirection);
  }


}
