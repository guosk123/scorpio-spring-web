package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
public class SendPolicyDO extends BaseOperateDO {

  private String id;

  private String name;

  private String externalReceiverId;

  private String sendRuleId;

  private String assignId;

  private String state;

  private String networkId;

  @Override
  public String toString() {
    return "SendPolicyDO{" + "id='" + id + '\'' + ", name='" + name + '\''
        + ", externalReceiverId='" + externalReceiverId + '\'' + ", sendRuleId='" + sendRuleId
        + '\'' + ", assignId='" + assignId + '\'' + ", state='" + state + '\'' + ", networkId='"
        + networkId + '\'' + '}';
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

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
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
}
