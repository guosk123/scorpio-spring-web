package com.machloop.fpc.manager.helper;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.helper.HotPropertiesHelper;

/**
 * @author liyongjun
 *
 * create at 2020年1月14日, fpc-manager
 */
@Component
public class TcpClientHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(TcpClientHelper.class);

  @Value("${capture.tcp.ip}")
  private String ip;

  @Value("${capture.tcp.port}")
  private int port;

  public Socket connect() {
    Socket client = null;
    try {
      String timeoutStr = HotPropertiesHelper.getProperty("capture.tcp.time.out.ms");
      Integer timeout = StringUtils.isNotBlank(timeoutStr) ? Integer.valueOf(timeoutStr) : 150000;
      
      client = new Socket(ip, port);
      client.setSoTimeout(Integer.valueOf(timeout));
      client.setKeepAlive(true);
      return client;
    } catch (IOException e) {
      LOGGER.warn("failed to connect tcp , ", e);
      close(client);
    }

    return null;
  }

  public void close(Socket client) {
    if (client != null) {
      try {
        client.close();
      } catch (IOException e) {
        LOGGER.warn("failed to close tcp connection, ", e);
      }
    }
  }

}
