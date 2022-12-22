package com.machloop.fpc.manager.analysis.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
public class StandardProtocolDO extends BaseOperateDO {

  private String l7ProtocolId;
  private String ipProtocol;
  private String port;
  private String source;
  private String description;

  @Override
  public String toString() {
    return "StandardProtocolDO [l7ProtocolId=" + l7ProtocolId + ", ipProtocol=" + ipProtocol
        + ", port=" + port + ", source=" + source + ", description=" + description + ", toString()="
        + super.toString() + "]";
  }

  public String getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(String l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public String getIpProtocol() {
    return ipProtocol;
  }

  public void setIpProtocol(String ipProtocol) {
    this.ipProtocol = ipProtocol;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
