package com.machloop.fpc.manager.boot.configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * @author guosk
 *
 * create at 2022年5月13日, fpc-manager
 */
@Component
public class GracefulShutdownTomcat
    implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GracefulShutdownTomcat.class);

  private volatile Connector connector;
  private final int waitTime = 10;

  @Override
  public void customize(Connector connector) {
    this.connector = connector;
  }

  @Override
  public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
    this.connector.pause();
    Executor executor = this.connector.getProtocolHandler().getExecutor();
    if (executor instanceof ThreadPoolExecutor) {
      try {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
        threadPoolExecutor.shutdown();
        if (!threadPoolExecutor.awaitTermination(waitTime, TimeUnit.SECONDS)) {
          LOGGER.warn("Tomcat thread pool did not shutdown gracefully within " + waitTime
              + " seconds. Proceeding with forceful shutdown");
        }
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }

}
