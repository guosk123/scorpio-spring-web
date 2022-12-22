package com.machloop.fpc.manager.knowledge.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2021年1月4日, fpc-manager
 */
public class SaCustomCategoryBO implements LogAudit {
  private String id;

  private String name;
  private String categoryId;
  private String subCategoryIds;
  private String categoryInCmsId;
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
        builder.append("添加自定义SA分类：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改自定义SA分类：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除自定义SA分类：");
        break;
      default:
        return "";
    }
    builder.append("名称=").append(name).append(";");
    builder.append("分类ID=").append(categoryId).append(";");
    builder.append("子分类ID={").append(subCategoryIds).append("};");
    builder.append("描述=").append(description).append("。");
    return builder.toString();

  }

  @Override
  public String toString() {
    return "SaCustomCategoryBO [id=" + id + ", name=" + name + ", categoryId=" + categoryId
        + ", subCategoryIds=" + subCategoryIds + ", categoryInCmsId=" + categoryInCmsId
        + ", description=" + description + ", createTime=" + createTime + ", updateTime="
        + updateTime + ", operatorId=" + operatorId + "]";
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

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getSubCategoryIds() {
    return subCategoryIds;
  }

  public void setSubCategoryIds(String subCategoryIds) {
    this.subCategoryIds = subCategoryIds;
  }

  public String getCategoryInCmsId() {
    return categoryInCmsId;
  }

  public void setCategoryInCmsId(String categoryInCmsId) {
    this.categoryInCmsId = categoryInCmsId;
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
