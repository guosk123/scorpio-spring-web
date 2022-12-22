package com.machloop.fpc.manager.metric.service;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author chenxiao
 * create at 2022/8/24
 */
public interface MetricNetworkSegmentationService {
    List<Map<String, Object>> queryMetricNetworkSegmentationL3Devices(MetricQueryVO queryVO,
                                                                      String sortProperty, String sortDirection);

    List<Map<String, Object>> queryMetricNetworkSegmentationIpConversations(MetricQueryVO queryVO,
                                                                            String sortProperty, String sortDirection);

    List<Map<String, Object>> queryMetricNetworkSegmentationApplications(MetricQueryVO queryVO,
                                                                         String sortProperty, String sortDirection);

    List<Map<String, Object>> queryMetricNetworkSegmentationPorts(MetricQueryVO queryVO, String sortProperty, String sortDirection);

    List<Map<String, Object>> queryMetricNetworkSegmentationServices(MetricQueryVO queryVO, String sortProperty, String sortDirection);
}
