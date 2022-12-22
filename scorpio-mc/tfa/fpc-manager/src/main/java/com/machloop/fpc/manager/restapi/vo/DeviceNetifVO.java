package com.machloop.fpc.manager.restapi.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author guosk
 *
 * create at 2021年9月8日, fpc-manager
 */
public class DeviceNetifVO {

  @NotEmpty(message = "修改接口时传入的类型不能为空")
  private String category;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "DeviceNetifModificationVO [category=" + category + ", description=" + description + "]";
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
