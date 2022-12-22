package com.machloop.fpc.manager.knowledge.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月20日, fpc-manager
 */
public class DecryptSettingCreationVO {
  @NotEmpty(message = "ip地址不能为空")
  private String ipAddress;
  @NotEmpty(message = "端口不能为空")
  private String port;
  @NotEmpty(message = "协议不能为空")
  private String protocol;

  @Override
  public String toString() {
    return "DecryptSettingCreationVO [ipAddress=" + ipAddress + ", port=" + port + ", protocol="
        + protocol + "]";
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }
}
