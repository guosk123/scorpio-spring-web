package com.machloop.fpc.cms.center.broker.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

public class CollectMetricDO extends BaseDO {

  private String deviceType;
  private String deviceSerialNumber;

  private Date startTime;
  private Date endTime;

  private String type;

  private int collectAmount;
  private int entityAmount;

  private Date createTime;
  private Date updateTime;

  @Override
  public String toString() {
    return "CollectMetricDO [deviceType=" + deviceType + ", deviceSerialNumber="
        + deviceSerialNumber + ", startTime=" + startTime + ", endTime=" + endTime + ", type="
        + type + ", collectAmount=" + collectAmount + ", entityAmount=" + entityAmount
        + ", createTime=" + createTime + ", updateTime=" + updateTime + "]";
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

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
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

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }
}
