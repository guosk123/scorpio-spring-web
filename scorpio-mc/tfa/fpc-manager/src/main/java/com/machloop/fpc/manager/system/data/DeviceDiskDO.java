package com.machloop.fpc.manager.system.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author liumeng
 *
 * create at 2018年12月14日, fpc-manager
 */
public class DeviceDiskDO extends BaseDO {

  private String nodeId;

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

  private String deviceId;
  private String physicalLocation;
  private String arrayNo;
  private int mediaErrorCount;
  private int otherErrorCount;
  private int predictiveFailureCount;

  private Date metricTime;

  @Override
  public String toString() {
    return "DeviceDiskDO [nodeId=" + nodeId + ", slotNo=" + slotNo + ", raidNo=" + raidNo
        + ", raidLevel=" + raidLevel + ", state=" + state + ", medium=" + medium + ", capacity="
        + capacity + ", rebuildProgress=" + rebuildProgress + ", copybackProgress="
        + copybackProgress + ", foreignState=" + foreignState + ", description=" + description
        + ", deviceId=" + deviceId + ", physicalLocation=" + physicalLocation + ", arrayNo="
        + arrayNo + ", mediaErrorCount=" + mediaErrorCount + ", otherErrorCount=" + otherErrorCount
        + ", predictiveFailureCount=" + predictiveFailureCount + ", metricTime=" + metricTime
        + ", toString()=" + super.toString() + "]";
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
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

  public String getArrayNo() {
    return arrayNo;
  }

  public void setArrayNo(String arrayNo) {
    this.arrayNo = arrayNo;
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

  public Date getMetricTime() {
    return metricTime;
  }

  public void setMetricTime(Date metricTime) {
    this.metricTime = metricTime;
  }

}
