package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public class SendRuleDO extends BaseOperateDO {


  private String id;

  private String name;

  private String sendRuleInCmsId;

  private String sendRuleContent;


  @Override
  public String toString() {
    return "SendRuleBO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", sendRuleInCmsId='"
        + sendRuleInCmsId + '\'' + ", sendRuleContent='" + sendRuleContent + '\'' + '}';
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSendRuleInCmsId() {
    return sendRuleInCmsId;
  }

  public void setSendRuleInCmsId(String sendRuleInCmsId) {
    this.sendRuleInCmsId = sendRuleInCmsId;
  }

  public String getSendRuleContent() {
    return sendRuleContent;
  }

  public void setSendRuleContent(String sendRuleContent) {
    this.sendRuleContent = sendRuleContent;
  }

}
