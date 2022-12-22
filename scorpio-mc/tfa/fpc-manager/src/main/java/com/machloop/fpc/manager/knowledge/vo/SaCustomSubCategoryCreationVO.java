package com.machloop.fpc.manager.knowledge.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author guosk
 *
 * create at 2021年1月22日, fpc-manager
 */
public class SaCustomSubCategoryCreationVO {

  @NotEmpty(message = "名称不能为空")
  private String name;

  @NotEmpty(message = "所属分类不能为空")
  private String categoryId;

  private String applicationIds;

  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "SaCustomSubCategoryCreationVO [name=" + name + ", categoryId=" + categoryId
        + ", applicationIds=" + applicationIds + ", description=" + description + "]";
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

  public String getApplicationIds() {
    return applicationIds;
  }

  public void setApplicationIds(String applicationIds) {
    this.applicationIds = applicationIds;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
