package com.scorpio.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 优雅关机
 *
 *  Tomcat 9.0.33 or later 可以通过配置文件直接启用优雅关机：
 *  server.shutdown=graceful
 *  spring.lifecycle.timeout-per-shutdown-phase=20s
 */
public class GracefulShutdownHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(GracefulShutdownHelper.class);

  private static final long SHUTDOWN_WAITING = 3000l;

  private static volatile boolean shutdownNow = false;

  public static boolean isShutdownNow() {
    return shutdownNow;
  }

  public static void registShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {

        LOGGER.info("application will be shutdown.");

        shutdownNow = true;

        // 设置关闭状态后，等待3秒
        try {
          Thread.sleep(SHUTDOWN_WAITING);
        } catch (InterruptedException e) {
          LOGGER.info("waiting shutdown has been interrrupt.");
          Thread.currentThread().interrupt();
        }

        LOGGER.info("application has been shutdown.");
      }
    });
  }
}
