package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

public class HostInsideCreationVO {

  @NotEmpty(message = "ip地址不能为空")
  private String ipAddress;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "HostInsideCreationVO [ipAddress=" + ipAddress + ", description=" + description
        + ", toString()=" + super.toString() + "]";
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
