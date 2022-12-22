package com.machloop.fpc.manager.metric.controller;

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
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.metric.service.MetricNetworkService;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2021年4月26日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-v1/metric")
public class MetricNetworkController {

  @Autowired
  private MetricNetworkService metricNetworkService;

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

    return metricNetworkService.queryMetricNetworks(queryVO, sortProperty, sortDirection);
  }

  @GetMapping("/networks/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricNetworksHistograms(MetricQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(queryVO.getNetworkId())) {
      result = metricNetworkService.queryALlNetworkHistograms(queryVO, true);
    } else {
      result = metricNetworkService.queryMetricNetworkHistograms(queryVO, true);
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

    return metricNetworkService.queryTcpStatistics(queryVO, request);
  }

}
