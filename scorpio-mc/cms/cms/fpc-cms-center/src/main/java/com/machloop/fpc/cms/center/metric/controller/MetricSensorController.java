package com.machloop.fpc.cms.center.metric.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.metric.service.MetricSensorService;
import com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年1月25日, fpc-cms-center
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/metric")
public class MetricSensorController {

  @Autowired
  private MetricSensorService metricSensorService;

  @GetMapping("/sensor-status/index-data")
  @Secured("PERM_USER")
  public Map<String, Object> queryMetricIndexData(MetricSensorQueryVO queryVO) {

    Map<String, Object> result = metricSensorService.queryIndexData(queryVO);
    return result;
  }

  @GetMapping("/sensor-status/free-space")
  @Secured("PERM_USER")
  public List<Map<String, Object>> querySensorFreeSpace(MetricSensorQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricSensorService.querySensorFreeSpace(queryVO);
  }

  @GetMapping("/sensor-status/disk-io")
  @Secured("PERM_USER")
  public List<Map<String, Object>> querySensorTopTrend(MetricSensorQueryVO queryVO) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricSensorService.queryDiskIOTopTrend(queryVO);
  }

  @GetMapping("/sensor-status/usage-rate")
  @Secured("PERM_USER")
  public List<Map<String, Object>> querySensorUsageRate(MetricSensorQueryVO queryVO) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricSensorService.queryUsageRateTopTrend(queryVO);
  }

  @GetMapping("/sensor/network-flow")
  @Secured("PERM_USER")
  public List<Map<String, Object>> querySensorNetworkTopTrend(MetricSensorQueryVO queryVO) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    if (StringUtils.equals(queryVO.getMetric(), CenterConstants.TCP_ESTABLISH_SUCCESS_RATE)) {
      return metricSensorService.queryEstablishSuccessRateTopTrend(queryVO);
    } else {
      return metricSensorService.querySensorNetworkTopTrend(queryVO);
    }
  }
}
