package com.machloop.fpc.manager.system.controller;


import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.system.service.MetricRestApiRecordService;
import com.machloop.fpc.manager.system.vo.MetricRestApiQueryVO;

@RestController
@RequestMapping("/webapi/fpc-v1/system")
public class MetricRestApiController {

  @Autowired
  private MetricRestApiRecordService metricRestApiRecordService;


  @GetMapping("/restapis")
  @Secured({"PERM_SYS_USER"})
  public Page<Map<String, Object>> queryRestApiRecords(
      @RequestParam(required = false, defaultValue = "") String startTime,
      @RequestParam(required = false, defaultValue = "") String endTime,
      MetricRestApiQueryVO queryVO,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(
          name = "sortProperty", required = false, defaultValue = "timestamp") String sortProperty,
      @RequestParam(
          name = "sortDirection", required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }

    PageRequest page = new PageRequest(pageNumber, pageSize);

    return metricRestApiRecordService.queryMetricRestApiRecords(queryVO, page, sortProperty,
        sortDirection);

  }

  @GetMapping("/restapis/as-user")
  @Secured({"PERM_SYS_USER"})
  public List<Map<String, Object>> queryUserTop(MetricRestApiQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    return metricRestApiRecordService.queryUserTop(queryVO);
  }

  @GetMapping("/restapis/as-api")
  @Secured({"PERM_SYS_USER"})
  public List<Map<String, Object>> queryApiTop(MetricRestApiQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    return metricRestApiRecordService.queryApiTop(queryVO);
  }

  @GetMapping("/restapis/user/as-list")
  @Secured({"PERM_SYS_USER"})
  public List<Map<String, Object>> queryUserList(MetricRestApiQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    return metricRestApiRecordService.queryUserList(queryVO);
  }

  @GetMapping("/restapis/api/as-list")
  @Secured({"PERM_SYS_USER"})
  public List<Map<String, Object>> queryApiList(MetricRestApiQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    return metricRestApiRecordService.queryApiList(queryVO);
  }
}
