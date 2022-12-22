package com.machloop.fpc.cms.center.appliance.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
public class SendPolicyQueryVO {

  @NotEmpty(message = "外发策略名称不能为空")
  private String name;

  private String externalReceiverId;

  private String sendRuleId;

  @Override
  public String toString() {
    return "SendPolicyQueryVO{" + "name='" + name + '\'' + ", externalReceiverId='"
        + externalReceiverId + '\'' + ", sendRuleId='" + sendRuleId + '\'' + '}';
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
}
