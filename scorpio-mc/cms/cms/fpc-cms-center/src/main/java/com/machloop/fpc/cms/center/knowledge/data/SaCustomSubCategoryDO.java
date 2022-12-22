package com.machloop.fpc.cms.center.knowledge.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2021年1月4日, fpc-manager
 */
public class SaCustomSubCategoryDO extends BaseOperateDO {

  private String assignId;
  private String name;
  private String categoryId;
  private String subCategoryId;
  private String description;

  @Override
  public String toString() {
    return "SaCustomSubCategoryDO [assignId=" + assignId + ", name=" + name + ", categoryId="
        + categoryId + ", subCategoryId=" + subCategoryId + ", description=" + description + "]";
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
