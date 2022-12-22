package com.machloop.fpc.cms.baseline.setting.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.fpc.cms.baseline.setting.service.BaselineSettingSyncService;

import reactor.util.function.Tuple4;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月3日, fpc-baseline
 */
@Component
public class AlertSettingSyncTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlertSettingSyncTask.class);

  @Autowired
  private List<BaselineSettingSyncService> syncServices;

  @Scheduled(fixedRateString = "${task.setting.sync.schedule.fixedrate.ms}")
  public void run() {
    for (BaselineSettingSyncService suncService : syncServices) {
      Tuple4<String, Integer, Integer, Integer> sync = suncService.sync();
      LOGGER.debug("current sync {} result: add: {}, update: {}, delete: {}.", sync.getT1(),
          sync.getT2(), sync.getT3(), sync.getT4());
    }
  }
}
