package com.machloop.fpc.cms.center.knowledge.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月18日, fpc-manager
 */
public class SaCustomApplicationBO implements LogAudit {
  private String id;

  private String assignId;
  private String name;
  private String applicationId;
  private String categoryId;
  private String subCategoryId;
  private String l7ProtocolId;
  private String rule;
  private String description;

  private String createTime;
  private String updateTime;

  private String operatorId;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加自定义SA规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改自定义SA规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除自定义SA规则：");
        break;
      default:
        return "";
    }
    builder.append("名称=").append(name).append(";");
    builder.append("应用ID=").append(applicationId).append(";");
    builder.append("类型ID=").append(categoryId).append(";");
    builder.append("子类型ID=").append(subCategoryId).append(";");
    builder.append("应用层协议ID=").append(l7ProtocolId).append(";");
    builder.append("规则=").append(rule).append(";");
    builder.append("描述=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "SaCustomApplicationBO [id=" + id + ", assignId=" + assignId + ", name=" + name
        + ", applicationId=" + applicationId + ", categoryId=" + categoryId + ", subCategoryId="
        + subCategoryId + ", l7ProtocolId=" + l7ProtocolId + ", rule=" + rule + ", description="
        + description + ", createTime=" + createTime + ", updateTime=" + updateTime
        + ", operatorId=" + operatorId + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getSubCategoryId() {
    return subCategoryId;
  }

  public void setSubCategoryId(String subCategoryId) {
    this.subCategoryId = subCategoryId;
  }

  public String getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(String l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }
}
