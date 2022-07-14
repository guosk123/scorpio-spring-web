package com.scorpio.appliance.task;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TestTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestTask.class);

  @Scheduled(cron = "${task.minute.schedule.cron}")
  public void run() {
    LOGGER.info("currentTime: {}", new Date());
  }

}
