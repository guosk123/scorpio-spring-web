package com.scorpio.indicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.scorpio.helper.HotPropertiesHelper;

/**
 * Scheduled配置Cron时，时间向回跳将导致定时器无法执行，此Task用于判断这种情况，由WatchDog进行处理。
 */
@Component
public class TimerCronHealthCheckTask implements HealthChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(TimerCronHealthCheckTask.class);

  @Autowired
  private HealthIndicatorImpl healthIndicator;

  private volatile long timestamp;

  @Scheduled(cron = "${task.health.timercron.schedule.cron}")
  public void run() {
    healthIndicator.addChecker(this);
    timestamp = System.nanoTime();
    LOGGER.debug("keepalive timestamp {}.", timestamp);
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public long getThreshold() {
    return Long
        .parseLong(HotPropertiesHelper.getProperty("healthchecker.timercron.threshold.second"));
  }
}
