package com.machloop.fpc.cms.center.global.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.fpc.cms.center.global.service.SlowQueryService;

/**
 * @author mazhiyuan
 *
 * create at 2020年10月20日, fpc-manager
 */
@Component
public class DeathQueryCancelTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeathQueryCancelTask.class);

  @Autowired
  private SlowQueryService slowQueryService;

  @Scheduled(fixedRateString = "${task.query.cancel.schedule.fixedrate.ms}")
  public void run() {
    slowQueryService.cancelDeathQueries();
    LOGGER.trace("finish to cancel death queries task.");
  }
}
