package com.machloop.fpc.manager.knowledge.data;

/**
 * @author mazhiyuan
 *
 * create at 2020年5月21日, fpc-manager
 */
public class SaApplicationDO {
  private String applicationId;

  private String categoryId;
  private String subCategoryId;
  private String name;
  private String nameText;
  private String description;
  private String descriptionText;

  @Override
  public String toString() {
    return "SaApplicationDO [applicationId=" + applicationId + ", categoryId=" + categoryId
        + ", subCategoryId=" + subCategoryId + ", name=" + name + ", nameText=" + nameText
        + ", description=" + description + ", descriptionText=" + descriptionText + "]";
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNameText() {
    return nameText;
  }

  public void setNameText(String nameText) {
    this.nameText = nameText;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescriptionText() {
    return descriptionText;
  }

  public void setDescriptionText(String descriptionText) {
    this.descriptionText = descriptionText;
  }
}