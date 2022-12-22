package com.machloop.fpc.cms.center.metric.data;

import com.machloop.fpc.cms.center.global.data.AbstractDataRecordDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public class MetricL2DeviceDataRecordDO extends AbstractDataRecordDO {
  private String macAddress;
  private String ethernetType;

  private long downstreamBytes;
  private long upstreamBytes;
  private long downstreamPackets;
  private long upstreamPackets;

  @Override
  public String toString() {
    return "MetricL2DeviceDataRecordDO [macAddress=" + macAddress + ", ethernetType=" + ethernetType
        + ", downstreamBytes=" + downstreamBytes + ", upstreamBytes=" + upstreamBytes
        + ", downstreamPackets=" + downstreamPackets + ", upstreamPackets=" + upstreamPackets + "]";
  }

  public String getMacAddress() {
    return macAddress;
  }

  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  public String getEthernetType() {
    return ethernetType;
  }

  public void setEthernetType(String ethernetType) {
    this.ethernetType = ethernetType;
  }

  public long getDownstreamBytes() {
    return downstreamBytes;
  }

  public void setDownstreamBytes(long downstreamBytes) {
    this.downstreamBytes = downstreamBytes;
  }

  public long getUpstreamBytes() {
    return upstreamBytes;
  }

  public void setUpstreamBytes(long upstreamBytes) {
    this.upstreamBytes = upstreamBytes;
  }

  public long getDownstreamPackets() {
    return downstreamPackets;
  }

  public void setDownstreamPackets(long downstreamPackets) {
    this.downstreamPackets = downstreamPackets;
  }

  public long getUpstreamPackets() {
    return upstreamPackets;
  }

  public void setUpstreamPackets(long upstreamPackets) {
    this.upstreamPackets = upstreamPackets;
  }

}
