package com.machloop.fpc.cms.center.broker.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2021年12月16日, fpc-cms-center
 */
public class SendupMessageDO extends BaseDO {

  private String messageId;
  private String deviceType;
  private String deviceSerialNumber;
  private Date startTime;
  private Date endTime;
  private String type;
  private String content;
  private String result;
  private Date createTime;
  private Date updateTime;

  @Override
  public String toString() {
    return "SendupMessageDO [messageId=" + messageId + ", deviceType=" + deviceType
        + ", deviceSerialNumber=" + deviceSerialNumber + ", startTime=" + startTime + ", endTime="
        + endTime + ", type=" + type + ", content=" + content + ", result=" + result
        + ", createTime=" + createTime + ", updateTime=" + updateTime + "]";
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
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

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
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
