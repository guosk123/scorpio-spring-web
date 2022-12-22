package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author ChenXiao
 * create at 2022/8/31
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
