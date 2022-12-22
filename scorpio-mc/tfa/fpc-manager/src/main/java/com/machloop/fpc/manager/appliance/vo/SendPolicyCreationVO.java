package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public class SendPolicyCreationVO {

  @NotEmpty(message = "外发策略名称不能为空")
  private String name;

  private String externalReceiverId;

  private String sendRuleId;

  private String state;

  private String networkId;

  private String packetAnalysisTaskId;

  @Override
  public String toString() {
    return "SendPolicyCreationVO{" + "name='" + name + '\'' + ", externalReceiverId='"
        + externalReceiverId + '\'' + ", sendRuleId='" + sendRuleId + '\'' + ", state='" + state
        + '\'' + ", networkId='" + networkId + '\'' + ", packetAnalysisTaskId='"
        + packetAnalysisTaskId + '\'' + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getExternalReceiverId() {
    return externalReceiverId;
  }

  public void setExternalReceiverId(String externalReceiverId) {
    this.externalReceiverId = externalReceiverId;
  }

  public String getSendRuleId() {
    return sendRuleId;
  }

  public void setSendRuleId(String sendRuleId) {
    this.sendRuleId = sendRuleId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getPacketAnalysisTaskId() {
    return packetAnalysisTaskId;
  }

  public void setPacketAnalysisTaskId(String packetAnalysisTaskId) {
    this.packetAnalysisTaskId = packetAnalysisTaskId;
  }
}
