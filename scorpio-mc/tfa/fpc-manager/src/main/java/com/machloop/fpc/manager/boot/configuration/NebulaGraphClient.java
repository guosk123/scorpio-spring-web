package com.machloop.fpc.manager.boot.configuration;

import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.Lists;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.exception.InvalidConfigException;
import com.vesoft.nebula.client.graph.exception.NotValidConnectionException;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;

// @Configuration
public class NebulaGraphClient {

  @Value("${nebula.graph.host}")
  private String host;
  @Value("${nebula.graph.port}")
  private int port;
  @Value("${nebula.graph.username}")
  private String username;
  @Value("${nebula.graph.password}")
  private String password;
  @Value("${nebula.graph.socket.timeout.ms}")
  private int socketTimeoutMs;

  private NebulaPool pool;

  @PostConstruct
  public void init() {
    try {
      NebulaPoolConfig nebulaPoolConfig = new NebulaPoolConfig();
      nebulaPoolConfig.setMaxConnSize(5);
      nebulaPoolConfig.setTimeout(socketTimeoutMs);
      nebulaPoolConfig.setIdleTime(socketTimeoutMs);

      pool = new NebulaPool();
      pool.init(Lists.newArrayList(new HostAddress(host, port)), nebulaPoolConfig);
    } catch (UnknownHostException | InvalidConfigException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  @PreDestroy
  public void preDestroy() {
    if (pool != null) {
      pool.close();
    }
  }

  public synchronized Session getSession() {
    Session session = null;
    try {
      session = pool.getSession(username, password, true);
    } catch (NotValidConnectionException | IOErrorException | AuthFailedException e) {
      e.printStackTrace();
    }

    return session;
  }

}
