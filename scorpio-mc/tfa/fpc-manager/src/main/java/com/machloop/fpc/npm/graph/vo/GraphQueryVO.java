package com.machloop.fpc.npm.graph.vo;

import java.util.Date;

import javax.validation.constraints.Max;

/**
 * @author guosk
 *
 * create at 2021年7月27日, fpc-manager
 */
public class GraphQueryVO {

  private String startTime;
  private String endTime;
  private String networkId;
  private String ipAddress;
  @Max(value = Integer.MAX_VALUE, message = "最小新建会话数超过最大限制")
  private Integer minEstablishedSessions = 0;
  @Max(value = Integer.MAX_VALUE, message = "最小字节数超过最大限制")
  private Integer minTotalBytes = 0;
  @Max(value = 5, message = "层数最多支持5层")
  private Integer pathLength = 1;

  private Date startTimeDate;
  private Date endTimeDate;

  @Override
  public String toString() {
    return "GraphQueryVO [startTime=" + startTime + ", endTime=" + endTime + ", networkId="
        + networkId + ", ipAddress=" + ipAddress + ", minEstablishedSessions="
        + minEstablishedSessions + ", minTotalBytes=" + minTotalBytes + ", pathLength=" + pathLength
        + ", startTimeDate=" + startTimeDate + ", endTimeDate=" + endTimeDate + "]";
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

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public Integer getMinEstablishedSessions() {
    return minEstablishedSessions;
  }

  public void setMinEstablishedSessions(Integer minEstablishedSessions) {
    this.minEstablishedSessions = minEstablishedSessions;
  }

  public Integer getMinTotalBytes() {
    return minTotalBytes;
  }

  public void setMinTotalBytes(Integer minTotalBytes) {
    this.minTotalBytes = minTotalBytes;
  }

  public Integer getPathLength() {
    return pathLength;
  }

  public void setPathLength(Integer pathLength) {
    this.pathLength = pathLength;
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

}
