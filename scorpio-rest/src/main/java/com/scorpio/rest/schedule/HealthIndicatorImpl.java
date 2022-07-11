package com.scorpio.rest.schedule;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author liumeng
 *
 * create at 2019年1月10日, alpha-webapp
 */
@Component
public class HealthIndicatorImpl implements HealthIndicator {

  private static final Logger LOGGER = LoggerFactory.getLogger(HealthIndicatorImpl.class);

  private final Set<HealthChecker> checkerSet = Sets.newLinkedHashSet();

  private final Map<HealthChecker,
      Integer> failureCountRepo = Maps.newLinkedHashMapWithExpectedSize(16);

  @Value("${healthchecker.watchdog.interval.second}")
  private long intervalSecond;

  @Value("${healthchecker.watchdog.bark.threshold}")
  private int barkThreshold;

  @PostConstruct
  public void initial() {
    new Thread(() -> {

      while (!GracefulShutdownHelper.isShutdownNow()) {

        // 检查各个Healchecker的健康状态
        for (HealthChecker checker : checkerSet) {

          long current = System.nanoTime();
          LOGGER.info("checker time: {}, schedule time: {}, difference: {}.", current,
              checker.getTimestamp(), (current - checker.getTimestamp()) / Math.pow(10, 9));
          Status isOk = checker.health();

          Integer failureCount = failureCountRepo.get(checker);
          if (failureCount == null || isOk == Status.UP) {
            failureCount = 0;
          } else {
            failureCount++;
          }

          // 如果连续N次检查失败，则退出VM
          checkFailureCount(failureCount, checker);

          failureCountRepo.put(checker, failureCount);
        }
        LOGGER.info("health check is running, {}", failureCountRepo.values());

        // 间隔3秒检查一次
        try {
          Thread.sleep(intervalSecond * 1000);
        } catch (InterruptedException e) {
          LOGGER.info("health check sleep has been interrupt.");
          Thread.currentThread().interrupt();
        }
      }

      LOGGER.info("health indicator task quit.");

    }, "health-indicator-task").start();
  }

  public void watchDog() {

  }

  /**
   * @see org.springframework.boot.actuate.health.HealthIndicator#health()
   */
  @Override
  public Health health() {

    boolean isOk = true;
    for (Integer failureCount : failureCountRepo.values()) {

      // 连续两次失效则健康状态置为outOfService
      if (failureCount > 0) {
        isOk = false;
        break;
      }
    }

    return isOk ? Health.up().build() : Health.outOfService().build();
  }

  public void addChecker(HealthChecker checker) {
    if (!checkerSet.contains(checker)) {
      checkerSet.add(checker);
      LOGGER.info("Health checker added, checker name: {}", checker.getClass().getSimpleName());
    }
  }

  private void checkFailureCount(Integer failureCount, HealthChecker checker) {
    if (failureCount >= barkThreshold) {
      LOGGER.warn("Health checker for {} has fail for {} times, VM will exit.",
          checker.getClass().getSimpleName(), barkThreshold);
      System.exit(0);
    }
  }
}
