package com.machloop.fpc.manager.analysis.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
public class StandardProtocolCreationVO {

  @Length(min = 1, max = 30, message = "应用层协议不能为空，最多可输入30个字符")
  private String l7ProtocolId;

  @Length(min = 1, max = 30, message = "传输层协议不能为空，最多可输入30个字符")
  private String ipProtocol;

  @NotEmpty(message = "端口号为空")
  private String port;

  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "StandardProtocolCreationVO [l7ProtocolId=" + l7ProtocolId + ", ipProtocol=" + ipProtocol
        + ", port=" + port + ", description=" + description + "]";
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
