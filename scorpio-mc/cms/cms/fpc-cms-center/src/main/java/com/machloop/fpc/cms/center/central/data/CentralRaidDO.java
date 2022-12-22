package com.machloop.fpc.cms.center.central.data;

import com.machloop.alpha.common.base.BaseDO;

public class CentralRaidDO extends BaseDO {

  private String deviceType;
  private String deviceSerialNumber;
  private String raidNo;
  private String raidLevel;
  private String state;

  @Override
  public String toString() {
    return "CentralRaidDO [deviceType=" + deviceType + ", deviceSerialNumber=" + deviceSerialNumber
        + ", raidNo=" + raidNo + ", raidLevel=" + raidLevel + ", state=" + state + "]";
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

  public String getRaidNo() {
    return raidNo;
  }

  public void setRaidNo(String raidNo) {
    this.raidNo = raidNo;
  }

  public String getRaidLevel() {
    return raidLevel;
  }

  public void setRaidLevel(String raidLevel) {
    this.raidLevel = raidLevel;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

}
