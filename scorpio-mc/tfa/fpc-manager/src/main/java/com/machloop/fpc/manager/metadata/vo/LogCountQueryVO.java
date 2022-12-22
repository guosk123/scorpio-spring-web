package com.machloop.fpc.manager.metadata.vo;

import java.util.Date;

/**
 * @author guosk
 *
 * create at 2021年9月16日, fpc-manager
 */
public class LogCountQueryVO {

  private String sourceType;// 数据源
  private String packetFileId;// 离线数据包文件ID
  private String startTime;
  private String endTime;
  private String networkId;
  private String serviceId;
  private String srcIp;

  private Date startTimeDate;
  private Date endTimeDate;

  // 查询范围是否包含开始时间
  private boolean includeStartTime = true;
  // 查询范围是否包含结束时间
  private boolean includeEndTime = false;

  @Override
  public String toString() {
    return "LogCountQueryVO [sourceType=" + sourceType + ", packetFileId=" + packetFileId
        + ", startTime=" + startTime + ", endTime=" + endTime + ", networkId=" + networkId
        + ", serviceId=" + serviceId + ", srcIp=" + srcIp + ", startTimeDate=" + startTimeDate
        + ", endTimeDate=" + endTimeDate + ", includeStartTime=" + includeStartTime
        + ", includeEndTime=" + includeEndTime + "]";
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getPacketFileId() {
    return packetFileId;
  }

  public void setPacketFileId(String packetFileId) {
    this.packetFileId = packetFileId;
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

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getSrcIp() {
    return srcIp;
  }

  public void setSrcIp(String srcIp) {
    this.srcIp = srcIp;
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
