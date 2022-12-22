package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
public class SendRuleDO extends BaseOperateDO {

  private String id;

  private String name;

  private String assignId;

  private String sendRuleContent;

  @Override
  public String toString() {
    return "SendRuleDO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", assignId='" + assignId
        + '\'' + ", sendRuleContent='" + sendRuleContent + '\'' + '}';
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

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getSendRuleContent() {
    return sendRuleContent;
  }

  public void setSendRuleContent(String sendRuleContent) {
    this.sendRuleContent = sendRuleContent;
  }
}
