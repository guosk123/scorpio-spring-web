package com.machloop.fpc.cms.center.boot.configuration;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.machloop.fpc.cms.center.broker.service.subordinate.impl.CentralApiServiceImpl;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * @author liyongjun
 *
 * create at 2019年12月10日, fpc-cms-center
 */
@Configuration
public class GrpcServerConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServerConfiguration.class);

  @Value("${fpc.cms.grpc.port}")
  private int port;

  @Autowired
  private CentralApiServiceImpl centralApiServiceImpl;

  private Server server;

  @PostConstruct
  public void init() {
    ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
    serverBuilder.addService(centralApiServiceImpl);
    server = serverBuilder.build();
    try {
      server.start();
      LOGGER.info("Server has started, listening on " + port);
    } catch (IOException e) {
      LOGGER.warn("failed to grpc server start." + e);
    }
  }

  @PreDestroy
  private void stop() {
    if (server != null) {
      LOGGER.info("grpc server will close.");
      server.shutdown();
      LOGGER.info("grpc server is closed.");
    }
  }

}
