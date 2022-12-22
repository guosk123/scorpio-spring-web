package com.machloop.fpc.cms.center.central.bo;

public class CentralNetifUsage {

  private String deviceSerialNumber;
  private String deviceName;
  private String netifName;
  private String category;
  private long usagedBandwidth;
  private long totalBandwidth;
  private long usage;

  @Override
  public String toString() {
    return "CentralNetifUsage [deviceSerialNumber=" + deviceSerialNumber + ", deviceName="
        + deviceName + ", netifName=" + netifName + ", category=" + category + ", usagedBandwidth="
        + usagedBandwidth + ", totalBandwidth=" + totalBandwidth + ", usage=" + usage + "]";
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

  public String getNetifName() {
    return netifName;
  }

  public void setNetifName(String netifName) {
    this.netifName = netifName;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public long getUsagedBandwidth() {
    return usagedBandwidth;
  }

  public void setUsagedBandwidth(long usagedBandwidth) {
    this.usagedBandwidth = usagedBandwidth;
  }

  public long getTotalBandwidth() {
    return totalBandwidth;
  }

  public void setTotalBandwidth(long totalBandwidth) {
    this.totalBandwidth = totalBandwidth;
  }

  public long getUsage() {
    return usage;
  }

  public void setUsage(long usage) {
    this.usage = usage;
  }

}
