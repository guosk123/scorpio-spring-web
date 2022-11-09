package com.scorpio.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfiguration implements SchedulingConfigurer {

  @Value("${spring.scheduler.thread.number}")
  private int poolSize;

  private ScheduledTaskRegistrar scheduledTaskRegistrar;

  /**
   * @see SchedulingConfigurer#configureTasks(
   *                          ScheduledTaskRegistrar)
   */
  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

    threadPoolTaskScheduler.setPoolSize(poolSize);
    threadPoolTaskScheduler.setThreadNamePrefix("scheduled-");
    threadPoolTaskScheduler.initialize();

    taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    scheduledTaskRegistrar = taskRegistrar;
  }

  public ScheduledTaskRegistrar getScheduledTaskRegistrar() {
    return scheduledTaskRegistrar;
  }

}
