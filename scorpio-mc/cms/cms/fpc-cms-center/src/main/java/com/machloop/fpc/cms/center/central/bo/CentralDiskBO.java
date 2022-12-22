package com.machloop.fpc.cms.center.central.bo;

public class CentralDiskBO {

  private String id;
  private String deviceType;
  private String deviceSerialNumber;
  private String physicalLocation;
  private String slotNo;
  private String raidNo;
  private String raidLevel;
  private String state;
  private String medium;
  private String capacity;
  private String rebuildProgress;
  private String copybackProgress;
  private String foreignState;
  private String description;

  private String stateText;
  private String mediumText;
  private String rebuildProgressText;
  private String copybackProgressText;

  @Override
  public String toString() {
    return "CentralDiskBO [id=" + id + ", deviceType=" + deviceType + ", deviceSerialNumber="
        + deviceSerialNumber + ", physicalLocation=" + physicalLocation + ", slotNo=" + slotNo
        + ", raidNo=" + raidNo + ", raidLevel=" + raidLevel + ", state=" + state + ", medium="
        + medium + ", capacity=" + capacity + ", rebuildProgress=" + rebuildProgress
        + ", copybackProgress=" + copybackProgress + ", foreignState=" + foreignState
        + ", description=" + description + ", stateText=" + stateText + ", mediumText=" + mediumText
        + ", rebuildProgressText=" + rebuildProgressText + ", copybackProgressText="
        + copybackProgressText + "]";
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

  public String getPhysicalLocation() {
    return physicalLocation;
  }

  public void setPhysicalLocation(String physicalLocation) {
    this.physicalLocation = physicalLocation;
  }

  public String getSlotNo() {
    return slotNo;
  }

  public void setSlotNo(String slotNo) {
    this.slotNo = slotNo;
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

  public String getMedium() {
    return medium;
  }

  public void setMedium(String medium) {
    this.medium = medium;
  }

  public String getCapacity() {
    return capacity;
  }

  public void setCapacity(String capacity) {
    this.capacity = capacity;
  }

  public String getRebuildProgress() {
    return rebuildProgress;
  }

  public void setRebuildProgress(String rebuildProgress) {
    this.rebuildProgress = rebuildProgress;
  }

  public String getCopybackProgress() {
    return copybackProgress;
  }

  public void setCopybackProgress(String copybackProgress) {
    this.copybackProgress = copybackProgress;
  }

  public String getForeignState() {
    return foreignState;
  }

  public void setForeignState(String foreignState) {
    this.foreignState = foreignState;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStateText() {
    return stateText;
  }

  public void setStateText(String stateText) {
    this.stateText = stateText;
  }

  public String getMediumText() {
    return mediumText;
  }

  public void setMediumText(String mediumText) {
    this.mediumText = mediumText;
  }

  public String getRebuildProgressText() {
    return rebuildProgressText;
  }

  public void setRebuildProgressText(String rebuildProgressText) {
    this.rebuildProgressText = rebuildProgressText;
  }

  public String getCopybackProgressText() {
    return copybackProgressText;
  }

  public void setCopybackProgressText(String copybackProgressText) {
    this.copybackProgressText = copybackProgressText;
  }

}
