package com.machloop.fpc.npm.appliance.data;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月8日, fpc-manager
 */
public class NetflowStatisticDO {
  
  private String deviceName;
  private String netifNo;
  private long ingestBytes;
  private long transmitBytes;
  private long totalBytes;

  @Override
  public String toString() {
    return "NetflowStatisticDO [totalBytes=" + totalBytes + ", ingestBytes=" + ingestBytes
        + ", transmitBytes=" + transmitBytes + ", deviceName=" + deviceName + ", netifNo=" + netifNo
        + "]";
  }

  public long getTotalBytes() {
    return totalBytes;
  }

  public void setTotalBytes(long totalBytes) {
    this.totalBytes = totalBytes;
  }

  public long getIngestBytes() {
    return ingestBytes;
  }

  public void setIngestBytes(long ingestBytes) {
    this.ingestBytes = ingestBytes;
  }

  public long getTransmitBytes() {
    return transmitBytes;
  }

  public void setTransmitBytes(long transmitBytes) {
    this.transmitBytes = transmitBytes;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getNetifNo() {
    return netifNo;
  }

  public void setNetifNo(String netifNo) {
    this.netifNo = netifNo;
  }
}
