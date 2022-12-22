package com.machloop.fpc.manager.metric.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.global.service.SlowQueryService;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.manager.metric.bo.MetricIpDetectionsLayoutsBO;
import com.machloop.fpc.manager.metric.service.MetricIpDetectionsService;
import com.machloop.fpc.manager.metric.vo.MetricFlowLogQueryVO;
import com.machloop.fpc.manager.metric.vo.MetricIpDetectionsLayoutsModificationVO;

/**
 * @author chenxiao
 * create at 2022/8/22
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/metric")
public class MetricIpDetectionsController {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(MetricIpDetectionsController.class);

  @Autowired
  private MetricIpDetectionsService metricIpDetectionsService;

  @Autowired
  private SlowQueryService slowQueryService;

  @GetMapping("/ip-detections/flow-logs")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryFlowLogsAsHistogram(@Validated MetricFlowLogQueryVO queryVO,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(required = false, defaultValue = "") String sortProperty,
      @RequestParam(required = false, defaultValue = "") String queryProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    Sort sort = new Sort(new Sort.Order(Sort.Direction.fromString(sortDirection), sortProperty));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    return metricIpDetectionsService.queryFlowLogsAsHistogram(queryVO, page, queryProperty);
  }

  @GetMapping("/ip-detections/flow-logs/as-statistics")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryFlowLogsStatistics(@Validated MetricFlowLogQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "") String queryProperty) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return metricIpDetectionsService.queryFlowLogsStatistics(queryVO, queryProperty);
  }

  @GetMapping("/ip-detections/protocol-dns-logs")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryDnsLogRecordAsGraph(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(required = false, defaultValue = "totalCounts") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @Validated LogRecordQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())
        && StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    Sort sort = new Sort(new Sort.Order(Sort.Direction.fromString(sortDirection), sortProperty));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    return metricIpDetectionsService.queryDnsLogRecordAsGraph(page, queryVO);
  }

  @GetMapping("/ip-detections/protocol-dns-logs/as-statistics")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryDnsLogRecordStatistics(@Validated LogRecordQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "totalCounts") String sortProperty) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())
        && StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    return metricIpDetectionsService.queryDnsLogRecordStatistics(queryVO, sortProperty);
  }

  @GetMapping("/ip-detections/layouts")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryIpDetectionsLayouts() {

    return metricIpDetectionsService
        .queryIpDetectionsLayouts(LoggedUserContext.getCurrentUser().getId());
  }

  @PutMapping("/ip-detections/layouts")
  @Secured({"PERM_USER"})
  public void updateIpDetectionsLayouts(
      @Validated MetricIpDetectionsLayoutsModificationVO metricIpDetectionsLayoutsModificationVO) {

    MetricIpDetectionsLayoutsBO metricIpDetectionsLayoutsBO = new MetricIpDetectionsLayoutsBO();
    BeanUtils.copyProperties(metricIpDetectionsLayoutsModificationVO, metricIpDetectionsLayoutsBO);

    MetricIpDetectionsLayoutsBO metricIpDetectionsLayouts = metricIpDetectionsService
        .updateIpDetectionsLayouts(metricIpDetectionsLayoutsBO,
            LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, metricIpDetectionsLayouts);

  }

}
