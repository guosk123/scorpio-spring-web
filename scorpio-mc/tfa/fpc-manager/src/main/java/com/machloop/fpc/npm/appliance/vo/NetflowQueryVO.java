package com.machloop.fpc.npm.appliance.vo;

import java.util.Date;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月13日, fpc-manager
 */
public class NetflowQueryVO {

  private String startTime;
  private String endTime;
  private String id;
  private String deviceName;
  private String totalBytes;
  private String totalPackets;
  private String transmitBytes;
  private String transmitPackets;
  private String ingestBytes;
  private String ingestPackets;
  private String duration;
  private String updateTime;
  private Date startTimeDate;
  private Date endTimeDate;

  // 查询范围是否包含开始时间
  private boolean includeStartTime = false;
  // 查询范围是否包含结束时间
  private boolean includeEndTime = true;

  @Override
  public String toString() {
    return "NetflowQueryVO [startTime=" + startTime + ", endTime=" + endTime + ", id=" + id
        + ", deviceName=" + deviceName + ", totalBytes=" + totalBytes + ", totalPackets="
        + totalPackets + ", transmitBytes=" + transmitBytes + ", transmitPackets=" + transmitPackets
        + ", ingestBytes=" + ingestBytes + ", ingestPackets=" + ingestPackets + ", duration="
        + duration + ", updateTime=" + updateTime + ", startTimeDate=" + startTimeDate
        + ", endTimeDate=" + endTimeDate + ", includeStartTime=" + includeStartTime
        + ", includeEndTime=" + includeEndTime + "]";
  }

  public Date getStartTimeDate() {
    return startTimeDate;
  }

  public void setStartTimeDate(Date startTimeDate) {
    this.startTimeDate = startTimeDate;
  }

  public Date getEndTimeDate() {
    return endTimeDate;
  }

  public void setEndTimeDate(Date endTimeDate) {
    this.endTimeDate = endTimeDate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }


  public String getTotalBytes() {
    return totalBytes;
  }

  public void setTotalBytes(String totalBytes) {
    this.totalBytes = totalBytes;
  }

  public String getTotalPackets() {
    return totalPackets;
  }

  public void setTotalPackets(String totalPackets) {
    this.totalPackets = totalPackets;
  }

  public String getTransmitBytes() {
    return transmitBytes;
  }

  public void setTransmitBytes(String transmitBytes) {
    this.transmitBytes = transmitBytes;
  }

  public String getTransmitPackets() {
    return transmitPackets;
  }

  public void setTransmitPackets(String transmitPackets) {
    this.transmitPackets = transmitPackets;
  }

  public String getIngestBytes() {
    return ingestBytes;
  }

  public void setIngestBytes(String ingestBytes) {
    this.ingestBytes = ingestBytes;
  }

  public String getIngestPackets() {
    return ingestPackets;
  }

  public void setIngestPackets(String ingestPackets) {
    this.ingestPackets = ingestPackets;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
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

  public boolean getIncludeStartTime() {
    return includeStartTime;
  }

  public void setIncludeStartTime(boolean includeStartTime) {
    this.includeStartTime = includeStartTime;
  }

  public boolean getIncludeEndTime() {
    return includeEndTime;
  }

  public void setIncludeEndTime(boolean includeEndTime) {
    this.includeEndTime = includeEndTime;
  }

}
