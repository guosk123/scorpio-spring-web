package com.machloop.fpc.cms.center.appliance.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
public class SendRuleQueryVO {

  @NotEmpty(message = "外发规则名称不能为空")
  private String name;

  private String sendRuleContent;

  @Override
  public String toString() {
    return "SendRuleQueryVO{" + "name='" + name + '\'' + ", sendRuleContent='" + sendRuleContent
        + '\'' + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSendRuleContent() {
    return sendRuleContent;
  }

  public void setSendRuleContent(String sendRuleContent) {
    this.sendRuleContent = sendRuleContent;
  }
}
