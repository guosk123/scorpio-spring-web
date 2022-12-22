package com.machloop.fpc.cms.center.metric.controller;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.global.service.SlowQueryService;
import com.machloop.fpc.cms.center.metric.service.MetricNetworkSegmentationService;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;

/**
 * @author ChenXiao
 * create at 2022/12/8
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/metric")
public class MetricNetworkSegmentationController {


  @Autowired
  private MetricNetworkSegmentationService metricNetworkSegmentationService;

  @Autowired
  private SlowQueryService slowQueryService;

  @Autowired
  private SensorNetworkGroupService sensorNetworkGroupService;


  @GetMapping("/network-segmentation/l3-devices")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricNetworkSegmentationL3Devices(
      @Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQueryVO(queryVO);

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));
    List<Map<String, Object>> result;

    result = metricNetworkSegmentationService.queryMetricNetworkSegmentationL3Devices(queryVO,
        sortProperty, sortDirection);

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/network-segmentation/ip-conversations")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricNetworkSegmentationIpConversations(
      @Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQueryVO(queryVO);

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    List<Map<String, Object>> result;

    result = metricNetworkSegmentationService.queryMetricNetworkSegmentationIpConversations(queryVO,
        sortProperty, sortDirection);


    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/network-segmentation/applications")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricNetworkSegmentationApplications(
      @Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQueryVO(queryVO);

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    List<Map<String, Object>> result;

    result = metricNetworkSegmentationService.queryMetricNetworkSegmentationApplications(queryVO,
        sortProperty, sortDirection);


    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/network-segmentation/ports")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricNetworkSegmentationPorts(
      @Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQueryVO(queryVO);

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    List<Map<String, Object>> result;

    result = metricNetworkSegmentationService.queryMetricNetworkSegmentationPorts(queryVO,
        sortProperty, sortDirection);


    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/network-segmentation/services")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricNetworkSegmentationServices(
      @Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQueryVO(queryVO);

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    List<Map<String, Object>> result;

    result = metricNetworkSegmentationService.queryMetricNetworkSegmentationServices(queryVO,
        sortProperty, sortDirection);


    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  private void enrichQueryVO(MetricQueryVO queryVO) {
    Set<String> networkIds = new HashSet<>();
    if (StringUtils.isNotEmpty(queryVO.getNetworkGroupId())) {
      CsvUtils.convertCSVToList(queryVO.getNetworkGroupId()).forEach(networkGroupId -> {
        networkIds.addAll(CsvUtils.convertCSVToList(sensorNetworkGroupService
            .querySensorNetworkGroup(networkGroupId).getNetworkInSensorIds()));
      });
    }
    if (StringUtils.isNotEmpty(queryVO.getNetworkId())) {
      networkIds.addAll(CsvUtils.convertCSVToList(queryVO.getNetworkId()));
    }
    queryVO.setNetworkIds(new ArrayList<>(networkIds));
  }

}
