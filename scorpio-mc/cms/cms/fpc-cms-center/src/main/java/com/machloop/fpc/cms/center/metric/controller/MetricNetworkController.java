package com.machloop.fpc.cms.center.metric.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.metric.service.MetricFlowlogService;
import com.machloop.fpc.cms.center.metric.service.MetricNetworkService;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;

/**
 * @author guosk
 *
 * create at 2021年4月26日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/metric")
public class MetricNetworkController {

  @Autowired
  private MetricNetworkService metricNetworkService;

  @Autowired
  private MetricFlowlogService metricFlowlogService;

  @Autowired
  private SensorNetworkGroupService sensorNetworkGroupService;

  @GetMapping("/networks")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricNetworks(MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricNetworks(queryVO, sortProperty, sortDirection);
    } else {
      result = metricNetworkService.queryMetricNetworks(queryVO, sortProperty, sortDirection);
    }

    return result;
  }

  @GetMapping("/networks/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricNetworkHistograms(MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricNetworkHistograms(queryVO, sortProperty,
          sortDirection);
    }

    return result;
  }

  @GetMapping("/networks/dashboard")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryNetworksDashboard(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricNetworkService.queryNetworkDashboard(queryVO, request);
  }

  @GetMapping("/networks/as-total-payload")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryNetworksTotalPayload(MetricQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricNetworkService.queryNetworksTotalPayload(queryVO);
  }

  @GetMapping("/networks/payload")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetworksPayloadStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricNetworkService.queryPayloadStatistics(queryVO, request);
  }

  @GetMapping("/networks/performance")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetworksPerformanceStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricNetworkService.queryPerformanceStatistics(queryVO, request);
  }

  @GetMapping("/networks/tcp")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetworksTcpStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricNetworkService.queryTcpStatistics(queryVO, request);
  }

  private void enrichQuery(final MetricQueryVO queryVO) {
    // 获取实际要查询的网络
    if (StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
      List<String> networkIds = CsvUtils.convertCSVToList(sensorNetworkGroupService
          .querySensorNetworkGroup(queryVO.getNetworkGroupId()).getNetworkInSensorIds());
      queryVO.setNetworkIds(networkIds);
    }
  }

}
