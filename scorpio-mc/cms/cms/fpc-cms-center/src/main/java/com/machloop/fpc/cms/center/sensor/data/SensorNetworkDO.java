package com.machloop.fpc.cms.center.sensor.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月22日, fpc-cms-center
 */
public class SensorNetworkDO extends BaseOperateDO {
  
  private String id;
  private String name;
  private String status;
  private int bandwidth;
  private String sensorId;
  private String sensorName;
  private String sensorType;
  private String networkInSensorId;
  private String networkInSensorName;
  private String description;
  private String owner;

  @Override
  public String toString() {
    return "SensorNetworkDO [id=" + id + ", name=" + name + ", status=" + status + ", bandwidth="
        + bandwidth + ", sensorId=" + sensorId + ", sensorName=" + sensorName + ", sensorType="
        + sensorType + ", networkInSensorId=" + networkInSensorId + ", networkInSensorName="
        + networkInSensorName + ", description=" + description + ", owner=" + owner + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
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

  public String getSensorType() {
    return sensorType;
  }

  public void setSensorType(String sensorType) {
    this.sensorType = sensorType;
  }

  public String getNetworkInSensorId() {
    return networkInSensorId;
  }

  public void setNetworkInSensorId(String networkInSensorId) {
    this.networkInSensorId = networkInSensorId;
  }

  public String getNetworkInSensorName() {
    return networkInSensorName;
  }

  public void setNetworkInSensorName(String networkInSensorName) {
    this.networkInSensorName = networkInSensorName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

}
