package com.machloop.fpc.cms.center.central.bo;

public class CentralNetifBO {

  private String id;
  private String deviceType;
  private String monitoredSerialNumber;
  private String netifName;
  private String category;
  private String state;
  private String specification;

  private long rxBps;
  private long txBps;
  private long rxPps;
  private long txPps;
  private String metricTime;

  private String monitoredName;

  @Override
  public String toString() {
    return "CentralNetifBO [id=" + id + ", deviceType=" + deviceType + ", monitoredSerialNumber="
        + monitoredSerialNumber + ", netifName=" + netifName + ", category=" + category + ", state="
        + state + ", specification=" + specification + ", rxBps=" + rxBps + ", txBps=" + txBps
        + ", rxPps=" + rxPps + ", txPps=" + txPps + ", metricTime=" + metricTime
        + ", monitoredName=" + monitoredName + "]";
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

  public String getMonitoredSerialNumber() {
    return monitoredSerialNumber;
  }

  public void setMonitoredSerialNumber(String monitoredSerialNumber) {
    this.monitoredSerialNumber = monitoredSerialNumber;
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

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getSpecification() {
    return specification;
  }

  public void setSpecification(String specification) {
    this.specification = specification;
  }

  public long getRxBps() {
    return rxBps;
  }

  public void setRxBps(long rxBps) {
    this.rxBps = rxBps;
  }

  public long getTxBps() {
    return txBps;
  }

  public void setTxBps(long txBps) {
    this.txBps = txBps;
  }

  public long getRxPps() {
    return rxPps;
  }

  public void setRxPps(long rxPps) {
    this.rxPps = rxPps;
  }

  public long getTxPps() {
    return txPps;
  }

  public void setTxPps(long txPps) {
    this.txPps = txPps;
  }

  public String getMetricTime() {
    return metricTime;
  }

  public void setMetricTime(String metricTime) {
    this.metricTime = metricTime;
  }

  public String getMonitoredName() {
    return monitoredName;
  }

  public void setMonitoredName(String monitoredName) {
    this.monitoredName = monitoredName;
  }

}
