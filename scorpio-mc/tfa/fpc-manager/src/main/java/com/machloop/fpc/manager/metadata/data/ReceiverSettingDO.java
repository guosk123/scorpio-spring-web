package com.machloop.fpc.manager.metadata.data;

import com.machloop.alpha.common.base.BaseOperateDO;

public class ReceiverSettingDO extends BaseOperateDO {

  private String name;
  private String protocolTopic;
  private String httpAction;
  private String httpActionExculdeUriSuffix;
  private String receiverId;
  private String receiverType;
  private String state;

  @Override
  public String toString() {
    return "FdrSettingDO [name=" + name + ", protocolTopic=" + protocolTopic + ", httpAction="
        + httpAction + ", httpActionExculdeUriSuffix=" + httpActionExculdeUriSuffix
        + ", receiverId=" + receiverId + ", receiverType=" + receiverType + ", state=" + state
        + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getProtocolTopic() {
    return protocolTopic;
  }

  public void setProtocolTopic(String protocolTopic) {
    this.protocolTopic = protocolTopic;
  }

  public String getHttpAction() {
    return httpAction;
  }

  public void setHttpAction(String httpAction) {
    this.httpAction = httpAction;
  }

  public String getHttpActionExculdeUriSuffix() {
    return httpActionExculdeUriSuffix;
  }

  public void setHttpActionExculdeUriSuffix(String httpActionExculdeUriSuffix) {
    this.httpActionExculdeUriSuffix = httpActionExculdeUriSuffix;
  }

  public String getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(String receiverId) {
    this.receiverId = receiverId;
  }

  public String getReceiverType() {
    return receiverType;
  }

  public void setReceiverType(String receiverType) {
    this.receiverType = receiverType;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

}
