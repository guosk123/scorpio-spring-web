package com.machloop.fpc.npm.appliance.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public class LogicalSubnetModificationVO {

  @Length(min = 1, max = 30, message = "子网名称不能为空，最多可输入30个字符")
  private String name;
  @NotEmpty(message = "所属网络不能为空")
  private String networkId;
  private int bandwidth;
  @NotEmpty(message = "子网类型不能为空")
  private String type;
  @NotEmpty(message = "子网配置不能为空")
  private String configuration;
  @Length(max = 255, message = "描述信息最多可输入255个字符")
  private String description;

  @Override
  public String toString() {
    return "LogicalSubnetModificationVO [name=" + name + ", networkId=" + networkId + ", bandwidth="
        + bandwidth + ", type=" + type + ", configuration=" + configuration + ", description="
        + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public int getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
