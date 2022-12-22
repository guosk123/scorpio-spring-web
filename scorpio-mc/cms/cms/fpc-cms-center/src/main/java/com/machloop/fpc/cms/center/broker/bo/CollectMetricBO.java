package com.machloop.fpc.cms.center.broker.bo;

public class CollectMetricBO {

  private String deviceType;
  private String deviceSerialNumber;

  private String startTime;
  private String endTime;

  private String type;

  private int collectAmount;
  private int entityAmount;

  @Override
  public String toString() {
    return "CollectMetricBO [deviceType=" + deviceType + ", deviceSerialNumber="
        + deviceSerialNumber + ", startTime=" + startTime + ", endTime=" + endTime + ", type="
        + type + ", collectAmount=" + collectAmount + ", entityAmount=" + entityAmount + "]";
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getDeviceSerialNumber() {
    return deviceSerialNumber;
  }

  public void setDeviceSerialNumber(String deviceSerialNumber) {
    this.deviceSerialNumber = deviceSerialNumber;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getCollectAmount() {
    return collectAmount;
  }

  public void setCollectAmount(int collectAmount) {
    this.collectAmount = collectAmount;
  }

  public int getEntityAmount() {
    return entityAmount;
  }

  public void setEntityAmount(int entityAmount) {
    this.entityAmount = entityAmount;
  }

}
