package com.machloop.fpc.cms.center.knowledge.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月18日, fpc-manager
 */
public class SaCustomApplicationDO extends BaseOperateDO {

  private String assignId;
  private String name;
  private String applicationId;
  private String categoryId;
  private String subCategoryId;
  private String l7ProtocolId;
  private String rule;
  private String description;

  @Override
  public String toString() {
    return "SaCustomApplicationDO [assignId=" + assignId + ", name=" + name + ", applicationId="
        + applicationId + ", categoryId=" + categoryId + ", subCategoryId=" + subCategoryId
        + ", l7ProtocolId=" + l7ProtocolId + ", rule=" + rule + ", description=" + description
        + "]";
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
}
