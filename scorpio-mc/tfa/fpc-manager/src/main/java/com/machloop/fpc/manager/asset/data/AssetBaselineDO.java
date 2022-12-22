package com.machloop.fpc.manager.asset.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月6日, fpc-manager
 */
public class AssetBaselineDO extends BaseOperateDO {

  private String ipAddress;
  private String type;
  private String baseline;
  private String description;

  @Override
  public String toString() {
    return "AssetBaselineDO [ipAddress=" + ipAddress + ", type=" + type + ", baseline=" + baseline
        + ", description=" + description + "]";
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

}
