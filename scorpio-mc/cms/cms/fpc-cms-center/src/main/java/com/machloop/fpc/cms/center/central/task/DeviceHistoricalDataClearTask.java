package com.machloop.fpc.cms.center.central.task;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.central.service.CentralNetifService;
import com.machloop.fpc.cms.center.central.service.CentralSystemService;

@Component
public class DeviceHistoricalDataClearTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeviceHistoricalDataClearTask.class);

  @Autowired
  private CentralNetifService centralNetifService;

  @Autowired
  private CentralSystemService centralSystemService;

  @Scheduled(cron = "${task.monitor.schedule.cron}")
  public void run() {

    Date now = DateUtils.now();
    Date beforeOneHour = new Date(now.getTime() - Constants.ONE_HOUR_SECONDS * 1000);
    Date beforeOneDay = DateUtils.beforeDayDate(now, 1);

    int clearCount = 0;
    // 清除一小时前的数据
    clearCount += centralNetifService.clearHisCentralNetifs(beforeOneHour,
        Constants.ONE_MINUTE_SECONDS);
    clearCount += centralSystemService.clearHisCentralSystem(beforeOneHour,
        Constants.ONE_MINUTE_SECONDS);

    // 清除一天前的数据
    clearCount += centralNetifService.clearHisCentralNetifs(beforeOneDay,
        Constants.FIVE_MINUTE_SECONDS);
    clearCount += centralSystemService.clearHisCentralSystem(beforeOneDay,
        Constants.FIVE_MINUTE_SECONDS);

    LOGGER.debug("clear device history data over, delete {} numbers of data this time.",
        clearCount);
  }

}
