package com.machloop.fpc.npm.analysis.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2021年7月15日, fpc-manager
 */
public class AbnormalEventRuleBO implements LogAudit {

  private String id;
  private int type;
  private String typeText;
  private String content;
  private String source;
  private String status;
  private String description;
  private String operatorId;
  private String timestamp;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加自定义异常事件：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改异常事件：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除自定义异常事件：");
        break;
      default:
        return "";
    }
    builder.append("事件类型=").append(type).append(";");
    builder.append("事件内容=").append(content).append(";");
    builder.append("启用状态=").append(status).append(";");
    builder.append("描述=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "AbnormalEventRuleBO [id=" + id + ", type=" + type + ", typeText=" + typeText
        + ", content=" + content + ", source=" + source + ", status=" + status + ", description="
        + description + ", operatorId=" + operatorId + ", timestamp=" + timestamp + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getTypeText() {
    return typeText;
  }

  public void setTypeText(String typeText) {
    this.typeText = typeText;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

}
