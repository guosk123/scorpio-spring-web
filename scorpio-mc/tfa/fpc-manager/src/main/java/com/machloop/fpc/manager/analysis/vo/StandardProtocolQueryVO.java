package com.machloop.fpc.manager.analysis.vo;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
public class StandardProtocolQueryVO {

  private String protocol;
  private String port;
  private String source;

  @Override
  public String toString() {
    return "StandardProtocolQueryVO [protocol=" + protocol + ", port=" + port + ", source=" + source
        + "]";
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
}
