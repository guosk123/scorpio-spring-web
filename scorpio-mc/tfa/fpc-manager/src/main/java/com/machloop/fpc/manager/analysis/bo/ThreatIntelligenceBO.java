package com.machloop.fpc.manager.analysis.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月16日, fpc-manager
 */
public class ThreatIntelligenceBO implements LogAudit {

  private String id;
  private String type;
  private String typeText;
  private String content;
  private String threatCategory;
  private String description;
  private String timestamp;

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改威胁情报：");
        break;
      default:
        return "";
    }
    builder.append("情报类型=").append(typeText).append(";");
    builder.append("情报内容=").append(content).append(";");
    builder.append("描述=").append(description).append(";");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "ThreatIntelligenceBO [id=" + id + ", type=" + type + ", typeText=" + typeText
        + ", content=" + content + ", threatCategory=" + threatCategory + ", description="
        + description + ", timestamp=" + timestamp + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
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

  public String getThreatCategory() {
    return threatCategory;
  }

  public void setThreatCategory(String threatCategory) {
    this.threatCategory = threatCategory;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }
}
