package com.machloop.fpc.cms.center.appliance.bo;

import org.apache.commons.lang3.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
public class ExternalReceiverBO implements LogAudit {

  private String id;

  private String name;
  private String receiverContent;
  private String receiverType;
  private String assignId;


  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加外发服务器：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改外发服务器：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除外发服务器：");
        break;
      default:
        return "";
    }

    builder.append("外发服务器名称=").append(name).append(";");
    builder.append("外发服务器内容=").append(receiverContent).append(";");
    builder.append("发送方式=").append(receiverType).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "ExternalReceiverBO{" + "id='" + id + '\'' + ", name='" + name + '\''
        + ", receiverContent='" + receiverContent + '\'' + ", receiverType='" + receiverType + '\''
        + ", assignId='" + assignId + '\'' + '}';
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

  public String getReceiverContent() {
    return receiverContent;
  }

  public void setReceiverContent(String receiverContent) {
    this.receiverContent = receiverContent;
  }

  public String getReceiverType() {
    return receiverType;
  }

  public void setReceiverType(String receiverType) {
    this.receiverType = receiverType;
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }
}
