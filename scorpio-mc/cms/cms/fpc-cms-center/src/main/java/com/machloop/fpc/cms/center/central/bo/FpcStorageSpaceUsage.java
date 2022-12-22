package com.machloop.fpc.cms.center.central.bo;

public class FpcStorageSpaceUsage {

  private String deviceSerialNumber;
  private String deviceName;
  private int fsDataUsedPct;
  private long fsDataTotalByte;

  @Override
  public String toString() {
    return "FpcStorageSpaceUsage [deviceSerialNumber=" + deviceSerialNumber + ", deviceName="
        + deviceName + ", fsDataUsedPct=" + fsDataUsedPct + ", fsDataTotalByte=" + fsDataTotalByte
        + "]";
  }

  public String getDeviceSerialNumber() {
    return deviceSerialNumber;
  }

  public void setDeviceSerialNumber(String deviceSerialNumber) {
    this.deviceSerialNumber = deviceSerialNumber;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public int getFsDataUsedPct() {
    return fsDataUsedPct;
  }

  public void setFsDataUsedPct(int fsDataUsedPct) {
    this.fsDataUsedPct = fsDataUsedPct;
  }

  public long getFsDataTotalByte() {
    return fsDataTotalByte;
  }

  public void setFsDataTotalByte(long fsDataTotalByte) {
    this.fsDataTotalByte = fsDataTotalByte;
  }

}
