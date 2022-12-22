package com.machloop.fpc.manager.restapi.vo;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

/**
 * @author ChenXiao
 * create at 2022/10/28
 */
public class SendRuleRestAPIVO {

  @NotEmpty(message = "外发规则名称不能为空")
  private String name;

  private List<Map<String, Object>> sendRuleContent;

  @Override
  public String toString() {
    return "SendRuleRestAPIVO{" + "name='" + name + '\'' + ", sendRuleContent=" + sendRuleContent
        + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Map<String, Object>> getSendRuleContent() {
    return sendRuleContent;
  }

  public void setSendRuleContent(List<Map<String, Object>> sendRuleContent) {
    this.sendRuleContent = sendRuleContent;
  }
}
