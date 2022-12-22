package com.machloop.fpc.cms.center.appliance.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

public class HostGroupCreationVO {

  @Length(min = 1, max = 30, message = "名称不能为空，最多可输入30个字符")
  private String name;
  @NotEmpty(message = "ip地址不能为空")
  private String ipAddress;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "HostGroupCreationVO [name=" + name + ", ipAddress=" + ipAddress + ", description="
        + description + "]";
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
