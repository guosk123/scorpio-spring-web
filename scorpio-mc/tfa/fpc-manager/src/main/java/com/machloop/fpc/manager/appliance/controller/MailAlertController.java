package com.machloop.fpc.manager.appliance.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.util.DateUtils;

import com.machloop.fpc.manager.appliance.service.MailAlertService;
import com.machloop.fpc.manager.appliance.vo.MailAlertQueryVO;

/**
 * @author minjiajun
 *
 * create at 2022年10月27日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class MailAlertController {

  @Autowired
  private MailAlertService mailAlertService;

  @GetMapping("/mail-alerts")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryMailAlerts(MailAlertQueryVO queryVO,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {

    Sort sort = new Sort(Sort.Direction.DESC, "timestamp");
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return mailAlertService.queryMailAlerts(queryVO, page);
  }
  
  @GetMapping("/mail-alerts/as-statistics")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryMailAlertsStatistics(MailAlertQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    Map<String, Object> result = mailAlertService.queryMailAlertStatistics(queryVO);
    return result;
  }

}
