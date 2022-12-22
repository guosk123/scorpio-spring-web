package com.machloop.fpc.npm.appliance.bo;

import java.util.List;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月15日, fpc-manager
 */
public class NetflowSourceBO {
  //包含Netflow设备与接口的统计与配置数据
  private String id;
  private String deviceName;
  private String deviceType;
  private String alias;
  private String protocolVersion;
  private double netifSpeed;
  private double totalBandwidth;
  private List<NetflowNetifBO> netif;
  private String description;

  @Override
  public String toString() {
    return "NetflowSourceBO [id=" + id + ", deviceName=" + deviceName + ", deviceType=" + deviceType
        + ", alias=" + alias + ", netifSpeed=" + netifSpeed + ", totalBandwidth=" + totalBandwidth
        + ", netif=" + netif + ", description=" + description + ", protocolVersion="
        + protocolVersion + "]";
  }

  public String getProtocolVersion() {
    return protocolVersion;
  }

  public void setProtocolVersion(String protocolVersion) {
    this.protocolVersion = protocolVersion;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public double getNetifSpeed() {
    return netifSpeed;
  }

  public void setNetifSpeed(double netifSpeed) {
    this.netifSpeed = netifSpeed;
  }

  public double getTotalBandwidth() {
    return totalBandwidth;
  }

  public void setTotalBandwidth(double totalBandwidth) {
    this.totalBandwidth = totalBandwidth;
  }

  public List<NetflowNetifBO> getNetif() {
    return netif;
  }

  public void setNetif(List<NetflowNetifBO> netif) {
    this.netif = netif;
  }

}
