package com.machloop.fpc.npm.appliance.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月16日, fpc-manager
 */
public class NetflowModificationVO {

  @NotEmpty(message = "修改接口时传入的id不能为空")
  private String id;
  private String alias;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String deviceType;
  private double netifSpeed;
  private String description;

  @Override
  public String toString() {
    return "NetflowModificationVO [id=" + id + ", alias=" + alias + ", description=" + description
        + ", netifSpeed=" + netifSpeed + ", deviceType=" + deviceType + "]";
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public double getNetifSpeed() {
    return netifSpeed;
  }

  public void setNetifSpeed(double netifSpeed) {
    this.netifSpeed = netifSpeed;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
