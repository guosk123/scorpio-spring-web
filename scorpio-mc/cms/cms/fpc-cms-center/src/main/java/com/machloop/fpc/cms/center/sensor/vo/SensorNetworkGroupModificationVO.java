package com.machloop.fpc.cms.center.sensor.vo;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public class SensorNetworkGroupModificationVO {

  private String name;
  private String networkInSensorIds;
  private String description;
  private String sendPolicyIds;

  @Override
  public String toString() {
    return "SensorNetworkGroupModificationVO{" + "name='" + name + '\'' + ", networkInSensorIds='"
        + networkInSensorIds + '\'' + ", description='" + description + '\'' + ", sendPolicyIds='"
        + sendPolicyIds + '\'' + '}';
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSendPolicyIds() {
    return sendPolicyIds;
  }

  public void setSendPolicyIds(String sendPolicyIds) {
    this.sendPolicyIds = sendPolicyIds;
  }
}
