package com.machloop.fpc.manager.metric.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.metric.bo.MetricDashboardSettingsBO;
import com.machloop.fpc.manager.metric.service.MetricServiceService;
import com.machloop.fpc.manager.metric.vo.MetricDashboardSettingsModificationVO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2021年4月26日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-v1/metric")
public class MetricServiceController {

  @Autowired
  private MetricServiceService metricServiceService;

  @GetMapping("/services")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryMetricServices(MetricQueryVO queryVO,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "isFollow", required = false) String isFollow,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    PageRequest page = new PageRequest(pageNumber, pageSize);

    return metricServiceService.queryMetricServices(queryVO, page, sortProperty, sortDirection,
        name, isFollow, LoggedUserContext.getCurrentUser().getId());
  }

  @GetMapping("/services/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricServiceHistograms(MetricQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricServiceService.queryMetricServiceHistograms(queryVO, true);
  }

  @GetMapping("/services/dashboard")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryServiceDashboard(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricServiceService.queryServiceDashboard(queryVO, request);
  }

  @GetMapping("/services/payload")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryServicesPayloadStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricServiceService.queryPayloadStatistics(queryVO, request);
  }

  @GetMapping("/services/performance")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryServicesPerformanceStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricServiceService.queryPerformanceStatistics(queryVO, request);
  }

  @GetMapping("/services/tcp")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryServicesTcpStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricServiceService.queryTcpStatistics(queryVO, request);
  }

  @GetMapping("/services/dashboard-settings")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryDashboardSettings() {
    return metricServiceService.queryDashboardSettings(LoggedUserContext.getCurrentUser().getId());
  }

  @PutMapping("/services/dashboard-settings")
  @Secured({"PERM_USER"})
  public void updateDashboardSettings(
      @Validated MetricDashboardSettingsModificationVO metricDashboardSettingsVO) {
    MetricDashboardSettingsBO metricDashboardSettingsBO = new MetricDashboardSettingsBO();
    BeanUtils.copyProperties(metricDashboardSettingsVO, metricDashboardSettingsBO);

    MetricDashboardSettingsBO metricDashboardSettings = metricServiceService
        .updateDashboardSettings(metricDashboardSettingsBO,
            LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, metricDashboardSettings);

  }


}
