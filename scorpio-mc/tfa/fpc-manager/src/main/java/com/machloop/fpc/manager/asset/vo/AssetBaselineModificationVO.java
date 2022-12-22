package com.machloop.fpc.manager.asset.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月6日, fpc-manager
 */
public class AssetBaselineModificationVO {

  @NotEmpty(message = "ip地址不能为空")
  private String ipAddress;
  private String type;
  private String baseline;
  private String description;
  private String updateTime;

  @Override
  public String toString() {
    return "AssetBaselineModificationVO [ipAddress=" + ipAddress + ", type=" + type + ", baseline="
        + baseline + ", description=" + description + ", updateTime=" + updateTime + "]";
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getBaseline() {
    return baseline;
  }

  public void setBaseline(String baseline) {
    this.baseline = baseline;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

}
