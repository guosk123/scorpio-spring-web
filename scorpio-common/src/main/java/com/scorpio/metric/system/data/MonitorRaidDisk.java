package com.scorpio.metric.system.data;

public class MonitorRaidDisk {

  private String raidNo;
  private String arrayNo;
  private String deviceId;
  private String physicalLocation;
  private String slotNo;
  private String wwn;
  private String mediaType;
  private String size;
  private String state;

  private int mediaErrorCount;
  private int otherErrorCount;
  private int predictiveFailureCount;

  private String rebuildProgress;
  
  private String copybackProgress;

  private String foreignState;

  private String description;

  @Override
  public String toString() {
    return "MonitorRaidDisk [raidNo=" + raidNo + ", arrayNo=" + arrayNo + ", deviceId=" + deviceId
        + ", physicalLocation=" + physicalLocation + ", slotNo=" + slotNo + ", wwn=" + wwn
        + ", mediaType=" + mediaType + ", size=" + size + ", state=" + state + ", mediaErrorCount="
        + mediaErrorCount + ", otherErrorCount=" + otherErrorCount + ", predictiveFailureCount="
        + predictiveFailureCount + ", rebuildProgress=" + rebuildProgress + ", copybackProgress="
        + copybackProgress + ", foreignState=" + foreignState + ", description=" + description
        + "]";
  }

  public String getRaidNo() {
    return raidNo;
  }

  public void setRaidNo(String raidNo) {
    this.raidNo = raidNo;
  }

  public String getArrayNo() {
    return arrayNo;
  }

  public void setArrayNo(String arrayNo) {
    this.arrayNo = arrayNo;
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

  public String getSlotNo() {
    return slotNo;
  }

  public void setSlotNo(String slotNo) {
    this.slotNo = slotNo;
  }

  public String getWwn() {
    return wwn;
  }

  public void setWwn(String wwn) {
    this.wwn = wwn;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public int getMediaErrorCount() {
    return mediaErrorCount;
  }

  public void setMediaErrorCount(int mediaErrorCount) {
    this.mediaErrorCount = mediaErrorCount;
  }

  public int getOtherErrorCount() {
    return otherErrorCount;
  }

  public void setOtherErrorCount(int otherErrorCount) {
    this.otherErrorCount = otherErrorCount;
  }

  public int getPredictiveFailureCount() {
    return predictiveFailureCount;
  }

  public void setPredictiveFailureCount(int predictiveFailureCount) {
    this.predictiveFailureCount = predictiveFailureCount;
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

}
