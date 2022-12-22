package com.machloop.fpc.manager.knowledge.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2021年1月4日, fpc-manager
 */
public class SaCustomSubCategoryBO implements LogAudit {
  private String id;

  private String name;
  private String subCategoryId;
  private String categoryId;
  private String applicationIds;
  private String subCategoryInCmsId;
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
        builder.append("添加自定义SA子分类：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改自定义SA子分类：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除自定义SA子分类：");
        break;
      default:
        return "";
    }
    builder.append("名称=").append(name).append(";");
    builder.append("子分类ID=").append(subCategoryId).append(";");
    builder.append("所属分类ID=").append(categoryId).append(";");
    builder.append("应用ID={").append(applicationIds).append("};");
    builder.append("描述=").append(description).append("。");
    return builder.toString();

  }

  @Override
  public String toString() {
    return "SaCustomSubCategoryBO [id=" + id + ", name=" + name + ", subCategoryId=" + subCategoryId
        + ", categoryId=" + categoryId + ", applicationIds=" + applicationIds
        + ", subCategoryInCmsId=" + subCategoryInCmsId + ", description=" + description
        + ", createTime=" + createTime + ", updateTime=" + updateTime + ", operatorId=" + operatorId
        + "]";
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

  public String getSubCategoryId() {
    return subCategoryId;
  }

  public void setSubCategoryId(String subCategoryId) {
    this.subCategoryId = subCategoryId;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getApplicationIds() {
    return applicationIds;
  }

  public void setApplicationIds(String applicationIds) {
    this.applicationIds = applicationIds;
  }

  public String getSubCategoryInCmsId() {
    return subCategoryInCmsId;
  }

  public void setSubCategoryInCmsId(String subCategoryInCmsId) {
    this.subCategoryInCmsId = subCategoryInCmsId;
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
