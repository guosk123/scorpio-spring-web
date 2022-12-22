package com.machloop.fpc.cms.center.sensor.vo;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public class SensorLogicalSubnetModificationVO {

  private String name;
  private String type;
  private String configuration;
  private String networkInSensorIds;
  private int bandwidth;

  @Override
  public String toString() {
    return "SensorLogicalSubnetModificationVO [name=" + name + ", type=" + type + ", configuration="
        + configuration + ", networkInSensorIds=" + networkInSensorIds + ", bandwidth=" + bandwidth
        + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNetworkInSensorIds() {
    return networkInSensorIds;
  }

  public void setNetworkInSensorIds(String networkInSensorIds) {
    this.networkInSensorIds = networkInSensorIds;
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

  public int getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
  }
}
