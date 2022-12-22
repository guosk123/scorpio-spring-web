package com.machloop.fpc.cms.center.knowledge.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2021年1月29日, fpc-manager
 */
public class SaHierarchyDO extends BaseDO {

  private String type;
  private String categoryId;
  private String subCategoryId;
  private String applicationId;
  private Date createTime;
  private String operatorId;

  @Override
  public String toString() {
    return "SaHierarchyDO [type=" + type + ", categoryId=" + categoryId + ", subCategoryId="
        + subCategoryId + ", applicationId=" + applicationId + ", createTime=" + createTime
        + ", operatorId=" + operatorId + "]";
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
