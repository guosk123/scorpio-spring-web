package com.machloop.fpc.cms.center.appliance.vo;

import javax.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月19日, fpc-cms-center
 */
public class ServiceModificationVO {

  @Length(min = 1, max = 30, message = "业务名称不能为空，最多可输入30个字符")
  private String name;
  private String networkIds;
  private String networkGroupIds;
  @NotEmpty(message = "应用配置不能为空")
  private String application;
  @Length(max = 255, message = "描述信息最多可输入255个字符")
  private String description;

  @Override
  public String toString() {
    return "ServiceModificationVO [name=" + name + ", networkIds=" + networkIds + ", networkGroupIds="
        + networkGroupIds + ", application=" + application + ", description=" + description + "]";
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

  public String getNetworkGroupIds() {
    return networkGroupIds;
  }

  public void setNetworkGroupIds(String networkGroupIds) {
    this.networkGroupIds = networkGroupIds;
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
