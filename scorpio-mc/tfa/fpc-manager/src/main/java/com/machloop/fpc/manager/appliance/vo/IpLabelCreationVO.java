package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * @author chenshimiao
 *
 * create at 2022/9/6 1:44 PM,cms
 * @version 1.0
 */
public class IpLabelCreationVO {

  @Length(min = 1, max = 30, message = "名称不能为空，最多可输入30个字符")
  private String name;
  @NotEmpty(message = "IP地址不能不为空")
  private String ipAddress;
  @Range(min = 1, max = 8, message = "分类选择不正确")
  @NotEmpty(message = "分类选择不能为空")
  private String category;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "IpLabelCreationVO [" + "name='" + name + ", ipAddress='" + ipAddress + ", category='"
        + category + ", description='" + description + ']';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
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
