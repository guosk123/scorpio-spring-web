package com.machloop.fpc.cms.center.sensor.vo;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public class SensorNetworkModificationVO {

  private String name;
  private String sensorId;
  private String sensorName;
  private String owner;
  private String networkInSensorId;
  private String description;

  private String sendPolicyIds;

  @Override
  public String toString() {
    return "SensorNetworkModificationVO{" + "name='" + name + '\'' + ", sensorId='" + sensorId
        + '\'' + ", sensorName='" + sensorName + '\'' + ", owner='" + owner + '\''
        + ", networkInSensorId='" + networkInSensorId + '\'' + ", description='" + description
        + '\'' + ", sendPolicyIds='" + sendPolicyIds + '\'' + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSensorId() {
    return sensorId;
  }

  public void setSensorId(String sensorId) {
    this.sensorId = sensorId;
  }

  public String getSensorName() {
    return sensorName;
  }

  public void setSensorName(String sensorName) {
    this.sensorName = sensorName;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getNetworkInSensorId() {
    return networkInSensorId;
  }

  public void setNetworkInSensorId(String networkInSensorId) {
    this.networkInSensorId = networkInSensorId;
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
