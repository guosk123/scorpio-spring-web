package com.scorpio.indicator;

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
import com.scorpio.Constants;
import com.scorpio.helper.GracefulShutdownHelper;
import com.scorpio.helper.HotPropertiesHelper;

@Component
public class HealthIndicatorImpl implements HealthIndicator {

  private static final Logger LOGGER = LoggerFactory.getLogger(HealthIndicatorImpl.class);

  private final Set<HealthChecker> checkerSet = Sets.newLinkedHashSet();

  private final Map<HealthChecker,
      Integer> failureCountRepo = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  @Value("${healthchecker.watchdog.interval.second}")
  private long intervalSecond;

  @PostConstruct
  public void initial() {
    new Thread(() -> {

      while (!GracefulShutdownHelper.isShutdownNow()) {

        // 检查各个Healchecker的健康状态
        for (HealthChecker checker : checkerSet) {

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
        LOGGER.debug("health check is running, {}", failureCountRepo.values());

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
   * @see HealthIndicator#health()
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
    if (failureCount >= Integer
        .parseInt(HotPropertiesHelper.getProperty("healthchecker.watchdog.bark.threshold"))) {
      LOGGER.warn("Health checker for {} has fail for {} times, VM will exit.",
          checker.getClass().getSimpleName(),
          HotPropertiesHelper.getProperty("healthchecker.watchdog.bark.threshold"));
      System.exit(0);
    }
  }
}
