package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public class SendRuleBO implements LogAudit {

  private String id;

  private String name;

  private String sendRuleInCmsId;

  private String sendRuleContent;


  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加外发规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改外发规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除外发规则：");
        break;
      default:
        return "";
    }

    builder.append("外发规则名称=").append(name).append(";");
    builder.append("外发规则内容=").append(sendRuleContent).append("。");
    return builder.toString();
  }

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