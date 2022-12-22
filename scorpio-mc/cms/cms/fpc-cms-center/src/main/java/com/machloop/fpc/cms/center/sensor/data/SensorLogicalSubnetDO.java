package com.machloop.fpc.cms.center.sensor.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public class SensorLogicalSubnetDO extends BaseOperateDO {

  private String id;
  private String assignId;
  private String name;
  private String type;
  private String configuration;
  private String networkInSensorIds;
  private int bandwidth;

  @Override
  public String toString() {
    return "SensorLogicalSubnetDO [id=" + id + ", assignId=" + assignId + ", name=" + name
        + ", type=" + type + ", configuration=" + configuration + ", networkInSensorIds="
        + networkInSensorIds + ", bandwidth=" + bandwidth + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public String getNetworkInSensorIds() {
    return networkInSensorIds;
  }

  public void setNetworkInSensorIds(String networkInSensorIds) {
    this.networkInSensorIds = networkInSensorIds;
  }

  public int getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
  }
}
