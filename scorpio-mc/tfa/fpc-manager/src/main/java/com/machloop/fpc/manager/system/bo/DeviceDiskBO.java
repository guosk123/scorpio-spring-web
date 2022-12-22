package com.machloop.fpc.manager.system.bo;

/**
 * @author liyongjun
 *
 * create at 2020年3月4日, fpc-manager
 */
public class DeviceDiskBO {

  private String id;
  private String slotNo;
  private String raidNo;
  private String raidLevel;
  private String state;
  private String medium;
  private String capacity;
  private String rebuildProgress;
  private String copybackProgress;
  private String description;

  private String deviceId;
  private String physicalLocation;
  private String stateText;
  private String mediumText;
  private String capacityText;
  private String rebuildProgressText;
  private String copybackProgressText;

  @Override
  public String toString() {
    return "DeviceDiskVO [id=" + id + ", slotNo=" + slotNo + ", raidNo=" + raidNo + ", raidLevel="
        + raidLevel + ", state=" + state + ", medium=" + medium + ", capacity=" + capacity
        + ", rebuildProgress=" + rebuildProgress + ", copybackProgress=" + copybackProgress
        + ", description=" + description + ", deviceId=" + deviceId + ", physicalLocation="
        + physicalLocation + ", stateText=" + stateText + ", mediumText=" + mediumText
        + ", capacityText=" + capacityText + ", rebuildProgressText=" + rebuildProgressText
        + ", copybackProgressText=" + copybackProgressText + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getPhysicalLocation() {
    return physicalLocation;
  }

  public void setPhysicalLocation(String physicalLocation) {
    this.physicalLocation = physicalLocation;
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

  public String getCapacityText() {
    return capacityText;
  }

  public void setCapacityText(String capacityText) {
    this.capacityText = capacityText;
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
