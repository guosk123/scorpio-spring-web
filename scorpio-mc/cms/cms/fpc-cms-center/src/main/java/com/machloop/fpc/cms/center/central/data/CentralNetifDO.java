package com.machloop.fpc.cms.center.central.data;

import java.util.Date;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseDO;
import com.machloop.alpha.common.metric.system.data.MonitorNetwork;

public class CentralNetifDO extends BaseDO {

  private String deviceType;
  private String monitoredSerialNumber;
  private String netifName;
  private String state;
  private String category;
  private int specification;

  private long rxBps;
  private long txBps;
  private long rxPps;
  private long txPps;
  private Date metricTime;

  public void compute(MonitorNetwork previous, MonitorNetwork current, int deltaSecond) {
    if (previous.getBytesRx() < current.getBytesRx()) {
      this.rxBps = (current.getBytesRx() - previous.getBytesRx()) * Constants.BYTE_BITS
          / deltaSecond;
    }
    if (previous.getPacketsRx() < current.getPacketsRx()) {
      this.rxPps = (current.getPacketsRx() - previous.getPacketsRx()) / deltaSecond;
    }
    if (previous.getBytesTx() < current.getBytesTx()) {
      this.txBps = (current.getBytesTx() - previous.getBytesTx()) * Constants.BYTE_BITS
          / deltaSecond;
    }
    if (previous.getPacketsTx() < current.getPacketsTx()) {
      this.txPps = (current.getPacketsTx() - previous.getPacketsTx()) / deltaSecond;
    }
  }

  @Override
  public String toString() {
    return "CentralNetifDO [deviceType=" + deviceType + ", monitoredSerialNumber="
        + monitoredSerialNumber + ", netifName=" + netifName + ", state=" + state + ", category="
        + category + ", specification=" + specification + ", rxBps=" + rxBps + ", txBps=" + txBps
        + ", rxPps=" + rxPps + ", txPps=" + txPps + ", metricTime=" + metricTime + "]";
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

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public int getSpecification() {
    return specification;
  }

  public void setSpecification(int specification) {
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

  public Date getMetricTime() {
    return metricTime;
  }

  public void setMetricTime(Date metricTime) {
    this.metricTime = metricTime;
  }

}
