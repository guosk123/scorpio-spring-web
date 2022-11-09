package com.scorpio.indicator.hikari;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;
import com.zaxxer.hikari.metrics.PoolStats;

/**
 * @author guosk
 *
 * create at 2022年9月21日, fpc-manager
 */
public class HikariCPMetricsTrackerFactory implements MetricsTrackerFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(HikariCPMetricsTrackerFactory.class);

  private static final ConcurrentMap<String, PoolStats> pollCollectors = new ConcurrentHashMap<>();

  private static volatile MonitorPoolState monitorPoolState;

  @Override
  public IMetricsTracker create(String poolName, PoolStats poolStats) {
    pollCollectors.put(poolName, poolStats);

    synchronized (this) {
      if (monitorPoolState == null) {
        monitorPoolState = new MonitorPoolState();
        monitorPoolState.start();
      }

      return new HikariCPMetricsTracker();
    }
  }

  class MonitorPoolState extends Thread {

    @Override
    public void run() {
      while (!GracefulShutdownHelper.isShutdownNow()) {
        pollCollectors.forEach((pName, pState) -> {
          LOGGER.info(
              "hikaricp poolName: {}, current state: [Active connections: {}, Idle connections: {}, "
                  + "Pending threads: {}, Current connections: {}, Max connections: {}, Min connections:{}]",
              pName, pState.getActiveConnections(), pState.getIdleConnections(),
              pState.getPendingThreads(), pState.getTotalConnections(), pState.getMaxConnections(),
              pState.getMinConnections());
        });

        try {
          TimeUnit.SECONDS.sleep(Constants.HALF_MINUTE_SECONDS);
        } catch (InterruptedException e) {
          LOGGER.warn("monitorPoolState thread is interrupted");
        }
      }
    }
  }

}
