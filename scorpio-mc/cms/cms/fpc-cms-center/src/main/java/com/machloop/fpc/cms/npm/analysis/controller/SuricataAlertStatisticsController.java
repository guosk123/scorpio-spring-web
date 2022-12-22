package com.machloop.fpc.cms.npm.analysis.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.fpc.cms.npm.analysis.service.SuricataAlertStatisticsService;

/**
 * @author chenshimiao
 *
 * create at 2022/10/13 11:54 AM,cms
 * @version 1.0
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/suricata")
public class SuricataAlertStatisticsController {

  @Autowired
  private SuricataAlertStatisticsService suricataAlertStatisticsService;

  @RequestMapping("/alert-messages/statistics")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryAlertStatistics(
      @RequestParam(required = false, defaultValue = "10") int count, String dsl) {

    return suricataAlertStatisticsService.queryAlertStatistics(dsl, count);
  }

  @RequestMapping("/alert-messages/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryAlertDateHistogram(String dsl,
      @RequestParam(required = false, defaultValue = "60") int interval) {

    return suricataAlertStatisticsService.queryAlertDateHistogram(dsl, interval);
  }
}
