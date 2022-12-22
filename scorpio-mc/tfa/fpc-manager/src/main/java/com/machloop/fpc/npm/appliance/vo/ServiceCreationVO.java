package com.machloop.fpc.npm.appliance.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author guosk
 *
 * create at 2020年11月12日, fpc-manager
 */
public class ServiceCreationVO {

  @Length(min = 1, max = 30, message = "业务名称不能为空，最多可输入30个字符")
  private String name;
  @NotEmpty(message = "网络不能为空")
  private String networkIds;
  @NotEmpty(message = "应用配置不能为空")
  private String application;
  @Length(max = 255, message = "描述信息最多可输入255个字符")
  private String description;

  @Override
  public String toString() {
    return "ServiceCreationVO [name=" + name + ", networkIds=" + networkIds + ", application="
        + application + ", description=" + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNetworkIds() {
    return networkIds;
  }

  public void setNetworkIds(String networkIds) {
    this.networkIds = networkIds;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
