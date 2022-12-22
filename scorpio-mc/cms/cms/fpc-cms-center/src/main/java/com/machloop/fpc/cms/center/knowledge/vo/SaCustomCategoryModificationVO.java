package com.machloop.fpc.cms.center.knowledge.vo;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

public class SaCustomCategoryModificationVO {

  @NotEmpty(message = "名称不能为空")
  private String name;

  private String subCategoryIds;

  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "SaCustomCategoryModificationVO [name=" + name + ", subCategoryIds=" + subCategoryIds
        + ", description=" + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSubCategoryIds() {
    return subCategoryIds;
  }

  public void setSubCategoryIds(String subCategoryIds) {
    this.subCategoryIds = subCategoryIds;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
