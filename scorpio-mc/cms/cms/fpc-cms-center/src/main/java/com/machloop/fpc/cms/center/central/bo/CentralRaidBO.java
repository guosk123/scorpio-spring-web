package com.machloop.fpc.cms.center.central.bo;

public class CentralRaidBO {

  private String id;
  private String deviceType;
  private String deviceSerialNumber;
  private String raidNo;
  private String raidLevel;
  private String state;

  private String stateText;

  @Override
  public String toString() {
    return "CentralRaidBO [id=" + id + ", deviceType=" + deviceType + ", deviceSerialNumber="
        + deviceSerialNumber + ", raidNo=" + raidNo + ", raidLevel=" + raidLevel + ", state="
        + state + ", stateText=" + stateText + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getStateText() {
    return stateText;
  }

  public void setStateText(String stateText) {
    this.stateText = stateText;
  }

}
