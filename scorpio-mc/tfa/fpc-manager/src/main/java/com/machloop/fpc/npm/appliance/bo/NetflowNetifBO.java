package com.machloop.fpc.npm.appliance.bo;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月16日, fpc-manager
 */
public class NetflowNetifBO {
  private String id;
  private String deviceName;
  private String deviceType;
  private String netifNo;
  private String alias;
  private double netifSpeed;
  private String totalBytes;
  private String transmitBytes;
  private String ingestBytes;
  private double transmitBandwidth;
  private double ingestBandwidth;
  private double totalBandwidth;
  private String description;
  private String reportTime;

  @Override
  public String toString() {
    return "NetflowNetifBO [id=" + id + ", netifNo=" + netifNo + ", alias=" + alias
        + ", deviceName=" + deviceName + ", deviceType=" + deviceType + ", description="
        + description + ", netifSpeed=" + netifSpeed + ", totalBytes=" + totalBytes
        + ", transmitBytes=" + transmitBytes + ", ingestBytes=" + ingestBytes
        + ", transmitBandwidth=" + transmitBandwidth + ", ingestBandwidth=" + ingestBandwidth
        + ", totalBandwidth=" + totalBandwidth + ", reportTime=" + reportTime + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public String getTotalBytes() {
    return totalBytes;
  }

  public void setTotalBytes(String totalBytes) {
    this.totalBytes = totalBytes;
  }

  public String getTransmitBytes() {
    return transmitBytes;
  }

  public void setTransmitBytes(String transmitBytes) {
    this.transmitBytes = transmitBytes;
  }

  public String getIngestBytes() {
    return ingestBytes;
  }

  public void setIngestBytes(String ingestBytes) {
    this.ingestBytes = ingestBytes;
  }

  public void setNetifSpeed(double netifSpeed) {
    this.netifSpeed = netifSpeed;
  }

  public double getNetifSpeed() {
    return netifSpeed;
  }
  
  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }



  public double getTransmitBandwidth() {
    return transmitBandwidth;
  }

  public double getIngestBandwidth() {
    return ingestBandwidth;
  }

  public double getTotalBandwidth() {
    return totalBandwidth;
  }

  public void setTransmitBandwidth(double transmitBandwidth) {
    this.transmitBandwidth = transmitBandwidth;
  }

  public void setIngestBandwidth(double ingestBandwidth) {
    this.ingestBandwidth = ingestBandwidth;
  }

  public void setTotalBandwidth(double totalBandwidth) {
    this.totalBandwidth = totalBandwidth;
  }

  public String getNetifNo() {
    return netifNo;
  }

  public void setNetifNo(String netifNo) {
    this.netifNo = netifNo;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getReportTime() {
    return reportTime;
  }

  public void setReportTime(String reportTime) {
    this.reportTime = reportTime;
  }
}
