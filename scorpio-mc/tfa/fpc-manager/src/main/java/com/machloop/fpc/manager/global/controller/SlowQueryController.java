package com.machloop.fpc.manager.global.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.fpc.manager.global.service.SlowQueryService;

/**
 * @author mazhiyuan
 *
 * create at 2020年10月20日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-v1/global")
public class SlowQueryController {

  @Autowired
  private SlowQueryService slowQueryService;

  @PostMapping("/slow-queries/heartbeat")
  @Secured({"PERM_USER"})
  public void heartbeat(@RequestParam String queryId) {
    slowQueryService.heartbeat(CsvUtils.convertCSVToList(queryId));
  }

  @PostMapping("/slow-queries/cancel")
  @Secured({"PERM_USER"})
  public void cancel(@RequestParam String queryId) {
    slowQueryService.cancelQueries(CsvUtils.convertCSVToList(queryId));
  }
}
