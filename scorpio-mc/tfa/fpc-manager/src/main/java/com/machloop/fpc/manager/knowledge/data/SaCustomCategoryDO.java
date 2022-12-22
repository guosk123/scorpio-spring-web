package com.machloop.fpc.manager.knowledge.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2021年1月4日, fpc-manager
 */
public class SaCustomCategoryDO extends BaseOperateDO {

  private String name;
  private String categoryId;
  private String categoryInCmsId;
  private String description;

  @Override
  public String toString() {
    return "SaCustomCategoryDO [name=" + name + ", categoryId=" + categoryId + ", categoryInCmsId="
        + categoryInCmsId + ", description=" + description + "]";
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

}
