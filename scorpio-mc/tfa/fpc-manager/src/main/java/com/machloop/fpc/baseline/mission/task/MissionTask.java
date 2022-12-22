package com.machloop.fpc.baseline.mission.task;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.fpc.baseline.mission.MissionService;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月5日, fpc-baseline
 */
@Component
public class MissionTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(MissionTask.class);

  @Autowired
  private MissionService missionService;

  @Scheduled(cron = "${task.mission.hour.cron}")
  public void runPerHour() {
    try {
      // 延迟1min执行，保证告警基线计算时能够采集到时间范围内的最后一分钟数据
      TimeUnit.MINUTES.sleep(1);
      missionService.executeHourMissions();
    } catch (InterruptedException e) {
      LOGGER.warn("failed to execute hour missions.", e);
    }
    LOGGER.trace("hour mission executed.");
  }

  @Scheduled(cron = "${task.mission.minute.cron}")
  public void runPerMinute() {
    try {
      missionService.executeMinuteMissions();
    } catch (InterruptedException e) {
      LOGGER.warn("failed to execute minute missions.", e);
    }
    LOGGER.trace("minute mission executed.");
  }

  @Scheduled(cron = "${task.mission.five.minute.cron}")
  public void runPerFiveMinute() {
    try {
      missionService.executeFiveMinuteMissions();
    } catch (InterruptedException e) {
      LOGGER.warn("failed to execute five minute missions.", e);
    }
    LOGGER.trace("five minute mission executed.");
  }

  @Scheduled(fixedRateString = "${task.mission.retry.schedule.fixedrate.ms}")
  public void runRetry() {
    try {
      missionService.retryFailedMissions();
    } catch (InterruptedException e) {
      LOGGER.warn("failed to execute missions ertry.", e);
    }
    LOGGER.trace("mission retry executed.");
  }
}
