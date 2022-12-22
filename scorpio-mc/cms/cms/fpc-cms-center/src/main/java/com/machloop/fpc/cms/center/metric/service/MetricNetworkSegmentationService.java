package com.machloop.fpc.cms.center.metric.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author ChenXiao
 * create at 2022/12/8
 */
public interface MetricNetworkSegmentationService {

  List<Map<String, Object>> queryMetricNetworkSegmentationL3Devices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);

  List<Map<String, Object>> queryMetricNetworkSegmentationIpConversations(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);

  List<Map<String, Object>> queryMetricNetworkSegmentationApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);

  List<Map<String, Object>> queryMetricNetworkSegmentationPorts(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);

  List<Map<String, Object>> queryMetricNetworkSegmentationServices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);
}
