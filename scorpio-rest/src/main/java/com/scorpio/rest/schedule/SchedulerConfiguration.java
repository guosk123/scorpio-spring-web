package com.scorpio.rest.schedule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @author guosk
 *
 * create at 2022年7月4日, alpha-zurich-rest
 */
@Configuration
public class SchedulerConfiguration implements SchedulingConfigurer {

  @Value("${spring.scheduler.thread.number}")
  private int poolSize;

  private ScheduledTaskRegistrar scheduledTaskRegistrar;

  /**
   * @see org.springframework.scheduling.annotation.SchedulingConfigurer#configureTasks(
   *                          org.springframework.scheduling.config.ScheduledTaskRegistrar)
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
