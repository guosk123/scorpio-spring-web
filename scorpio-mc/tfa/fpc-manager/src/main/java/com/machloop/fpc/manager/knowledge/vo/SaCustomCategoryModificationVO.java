package com.machloop.fpc.manager.knowledge.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

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
