package com.machloop.fpc.npm.analysis.vo;

import java.util.Date;

/**
 * @author guosk
 *
 * create at 2021年7月19日, fpc-manager
 */
public class AbnormalEventQueryVO {

  private String startTime;
  private String endTime;
  private String networkId;
  private Integer type;
  private String content;
  private String ipAddress;
  private Integer l7ProtocolId;

  private Date startTimeDate;
  private Date endTimeDate;

  @Override
  public String toString() {
    return "AbnormalEventQueryVO [startTime=" + startTime + ", endTime=" + endTime + ", networkId="
        + networkId + ", type=" + type + ", content=" + content + ", ipAddress=" + ipAddress
        + ", l7ProtocolId=" + l7ProtocolId + ", startTimeDate=" + startTimeDate + ", endTimeDate="
        + endTimeDate + "]";
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

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public Integer getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(Integer l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
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
