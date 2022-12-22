package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public class SendPolicyDO extends BaseOperateDO {

  private String id;

  private String name;

  private String externalReceiverId;

  private String sendRuleId;

  private String sendPolicyInCmsId;

  private String state;

  private String networkId;

  private String packetAnalysisTaskId;

  @Override
  public String toString() {
    return "SendPolicyDO{" + "id='" + id + '\'' + ", name='" + name + '\''
        + ", externalReceiverId='" + externalReceiverId + '\'' + ", sendRuleId='" + sendRuleId
        + '\'' + ", sendPolicyInCmsId='" + sendPolicyInCmsId + '\'' + ", state='" + state + '\''
        + ", networkId='" + networkId + '\'' + ", packetAnalysisTaskId='" + packetAnalysisTaskId
        + '\'' + '}';
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
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

  public String getSendPolicyInCmsId() {
    return sendPolicyInCmsId;
  }

  public void setSendPolicyInCmsId(String sendPolicyInCmsId) {
    this.sendPolicyInCmsId = sendPolicyInCmsId;
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
