package com.scorpio.rest.schedule;

import org.springframework.boot.actuate.health.Status;

/**
 * @author liumeng
 *
 * create at 2019年1月10日, alpha-webapp
 */
public interface HealthChecker {

  default Status health() {
    boolean isOk = true;

    long threshold = getThreshold() * 1000 * 1000 * 1000;

    long interval = System.nanoTime() - getTimestamp();
    if (Math.abs(interval) >= threshold) {
      isOk = false;
    }
    return isOk ? Status.UP : Status.OUT_OF_SERVICE;
  }

  long getTimestamp();

  long getThreshold();
}
