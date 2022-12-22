package com.machloop.fpc.cms.center.broker.data;

import java.util.Date;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author liyongjun
 *
 * create at 2019年12月3日, fpc-cms-center
 */
public class DeviceStatusDO {

  private String deviceType;
  private String deviceIp;
  private String deviceName;
  private String serialNumber;
  private String version;
  private String licenseState;
  private long upTime;
  private long timestamp;

  private Date lastLoginTime;
  private Date lastInteractiveTime;
  private long lastInteractiveLatency;

  @Override
  public String toString() {
    return "DeviceStatusDO [deviceType=" + deviceType + ", deviceIp=" + deviceIp + ", deviceName="
        + deviceName + ", serialNumber=" + serialNumber + ", version=" + version + ", licenseState="
        + licenseState + ", upTime=" + upTime + ", timestamp=" + timestamp + ", lastLoginTime="
        + lastLoginTime + ", lastInteractiveTime=" + lastInteractiveTime
        + ", lastInteractiveLatency=" + lastInteractiveLatency + "]";
  }

  /**
   * 获取当前连接状态
   * @return
   */
  public String getCurrentConnectStatus() {
    if ((DateUtils.now().getTime()
        - this.timestamp) < FpcCmsConstants.HEARTBEAT_INACTIVATION_MILLISECOND) {
      return FpcCmsConstants.CONNECT_STATUS_NORMAL;
    }

    return FpcCmsConstants.CONNECT_STATUS_ABNORMAL;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getDeviceIp() {
    return deviceIp;
  }

  public void setDeviceIp(String deviceIp) {
    this.deviceIp = deviceIp;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getLicenseState() {
    return licenseState;
  }

  public void setLicenseState(String licenseState) {
    this.licenseState = licenseState;
  }

  public long getUpTime() {
    return upTime;
  }

  public void setUpTime(long upTime) {
    this.upTime = upTime;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public Date getLastLoginTime() {
    return lastLoginTime;
  }

  public void setLastLoginTime(Date lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }

  public Date getLastInteractiveTime() {
    return lastInteractiveTime;
  }

  public void setLastInteractiveTime(Date lastInteractiveTime) {
    this.lastInteractiveTime = lastInteractiveTime;
  }

  public long getLastInteractiveLatency() {
    return lastInteractiveLatency;
  }

  public void setLastInteractiveLatency(long lastInteractiveLatency) {
    this.lastInteractiveLatency = lastInteractiveLatency;
  }

}
