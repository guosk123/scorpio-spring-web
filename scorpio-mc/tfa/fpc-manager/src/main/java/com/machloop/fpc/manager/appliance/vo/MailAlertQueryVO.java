package com.machloop.fpc.manager.appliance.vo;

import java.util.Date;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月31日, fpc-manager
 */
public class MailAlertQueryVO {

  private String timestamp;
  private String srcIp;
  private String srcPort;
  private String destIp;
  private String destPort;
  private String protocol;
  private String mailAddress;
  private String countryId;
  private String provinceId;
  private String cityId;
  private String description;
  private String loginTimestamp;
  
  private String startTime;
  private String endTime;
  private Date startTimeDate;
  private Date endTimeDate;
  
  // 查询范围是否包含开始时间
  private boolean includeStartTime = true;
  // 查询范围是否包含结束时间
  private boolean includeEndTime = false;
  
  @Override
  public String toString() {
    return "MailAlertQueryVO [timestamp=" + timestamp + ", srcIp=" + srcIp + ", srcPort=" + srcPort
        + ", destIp=" + destIp + ", destPort=" + destPort + ", protocol=" + protocol
        + ", mailAddress=" + mailAddress + ", countryId=" + countryId + ", provinceId=" + provinceId
        + ", cityId=" + cityId + ", description=" + description + ", loginTimestamp="
        + loginTimestamp + ", startTime=" + startTime + ", endTime=" + endTime + ", startTimeDate="
        + startTimeDate + ", endTimeDate=" + endTimeDate + ", includeStartTime=" + includeStartTime
        + ", includeEndTime=" + includeEndTime + "]";
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getSrcIp() {
    return srcIp;
  }

  public void setSrcIp(String srcIp) {
    this.srcIp = srcIp;
  }

  public String getSrcPort() {
    return srcPort;
  }

  public void setSrcPort(String srcPort) {
    this.srcPort = srcPort;
  }

  public String getDestIp() {
    return destIp;
  }

  public void setDestIp(String destIp) {
    this.destIp = destIp;
  }

  public String getDestPort() {
    return destPort;
  }

  public void setDestPort(String destPort) {
    this.destPort = destPort;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getMailAddress() {
    return mailAddress;
  }

  public void setMailAddress(String mailAddress) {
    this.mailAddress = mailAddress;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  public String getProvinceId() {
    return provinceId;
  }

  public void setProvinceId(String provinceId) {
    this.provinceId = provinceId;
  }

  public String getCityId() {
    return cityId;
  }

  public void setCityId(String cityId) {
    this.cityId = cityId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLoginTimestamp() {
    return loginTimestamp;
  }

  public void setLoginTimestamp(String loginTimestamp) {
    this.loginTimestamp = loginTimestamp;
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

  public boolean isIncludeStartTime() {
    return includeStartTime;
  }

  public void setIncludeStartTime(boolean includeStartTime) {
    this.includeStartTime = includeStartTime;
  }

  public boolean isIncludeEndTime() {
    return includeEndTime;
  }

  public void setIncludeEndTime(boolean includeEndTime) {
    this.includeEndTime = includeEndTime;
  }
  
}
