package com.scorpio.rest.schedule;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled配置Cronn时，时间向回跳将导致定时器无法执行，此Task用于判断这种情况，由WatchDog进行处理。
 * 
 * @author liumeng
 *
 * create at 2019年6月20日, alpha-common
 */
@Component
public class TimerCronHealthCheckTask implements HealthChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(TimerCronHealthCheckTask.class);

  @Value("${healthchecker.timercron.threshold.second}")
  private long threshold;

  @Autowired
  private HealthIndicatorImpl healthIndicator;

  private volatile long timestamp;

  @Scheduled(cron = "${task.health.timercron.schedule.cron}")
  public void run() {
    healthIndicator.addChecker(this);
    timestamp = System.nanoTime();
    LOGGER.info("keepalive timestamp {}, time: {}.", timestamp, new Date());
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public long getThreshold() {
    return threshold;
  }
}
