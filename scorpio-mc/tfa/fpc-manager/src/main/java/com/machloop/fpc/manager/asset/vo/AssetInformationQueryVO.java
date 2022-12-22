package com.machloop.fpc.manager.asset.vo;

import java.util.Date;

/**
 * @author minjiajun
 *
 * create at 2022年9月2日, fpc-manager
 */
public class AssetInformationQueryVO {

  private String queryId;

  private String ip;
  private String ipAddress;
  private String deviceType;
  private String os;
  private String port;
  private String alarm;
  private String timestamp;
  private String firstTime;

  private String startTime;
  private String endTime;
  private Date startTimeDate;
  private Date endTimeDate;
  
  private String sortProperty = "";

  @Override
  public String toString() {
    return "AssetInformationQueryVO [queryId=" + queryId + ", ip=" + ip + ", ipAddress=" + ipAddress
        + ", deviceType=" + deviceType + ", os=" + os + ", port=" + port + ", alarm=" + alarm
        + ", timestamp=" + timestamp + ", firstTime=" + firstTime + ", startTime=" + startTime
        + ", endTime=" + endTime + ", startTimeDate=" + startTimeDate + ", endTimeDate="
        + endTimeDate + ", sortProperty=" + sortProperty + "]";
  }

  public String getQueryId() {
    return queryId;
  }

  public void setQueryId(String queryId) {
    this.queryId = queryId;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getOs() {
    return os;
  }

  public void setOs(String os) {
    this.os = os;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getAlarm() {
    return alarm;
  }

  public void setAlarm(String alarm) {
    this.alarm = alarm;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getFirstTime() {
    return firstTime;
  }

  public void setFirstTime(String firstTime) {
    this.firstTime = firstTime;
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

  public String getSortProperty() {
    return sortProperty;
  }

  public void setSortProperty(String sortProperty) {
    this.sortProperty = sortProperty;
  }

}
