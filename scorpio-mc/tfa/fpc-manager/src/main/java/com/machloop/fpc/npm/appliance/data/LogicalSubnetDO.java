package com.machloop.fpc.npm.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public class LogicalSubnetDO extends BaseOperateDO {

  private String name;
  private String networkId;
  private int bandwidth;
  private String type;
  private String configuration;
  private String subnetInCmsId;
  private String description;

  @Override
  public String toString() {
    return "LogicalSubnetDO [name=" + name + ", networkId=" + networkId + ", bandwidth=" + bandwidth
        + ", type=" + type + ", configuration=" + configuration + ", subnetInCmsId=" + subnetInCmsId
        + ", description=" + description + "]";
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

  public String getSubnetInCmsId() {
    return subnetInCmsId;
  }

  public void setSubnetInCmsId(String subnetInCmsId) {
    this.subnetInCmsId = subnetInCmsId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
