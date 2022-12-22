package com.machloop.fpc.cms.center.metric.data;

import java.util.Date;

/**
 * @author guosk
 *
 * create at 2020年12月4日, fpc-manager
 */
public class MetricNetifDataRecordDO {
  private Date timestamp;
  private String networkId;
  private String netifName;

  private long totalBytes;
  private long totalPackets;
  private long downstreamBytes;
  private long downstreamPackets;
  private long upstreamBytes;
  private long upstreamPackets;
  private long transmitBytes;
  private long transmitPackets;

  @Override
  public String toString() {
    return "MetricNetifDataRecordDO [timestamp=" + timestamp + ", networkId=" + networkId
        + ", netifName=" + netifName + ", totalBytes=" + totalBytes + ", totalPackets="
        + totalPackets + ", downstreamBytes=" + downstreamBytes + ", downstreamPackets="
        + downstreamPackets + ", upstreamBytes=" + upstreamBytes + ", upstreamPackets="
        + upstreamPackets + ", transmitBytes=" + transmitBytes + ", transmitPackets="
        + transmitPackets + "]";
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getNetifName() {
    return netifName;
  }

  public void setNetifName(String netifName) {
    this.netifName = netifName;
  }

  public long getTotalBytes() {
    return totalBytes;
  }

  public void setTotalBytes(long totalBytes) {
    this.totalBytes = totalBytes;
  }

  public long getTotalPackets() {
    return totalPackets;
  }

  public void setTotalPackets(long totalPackets) {
    this.totalPackets = totalPackets;
  }

  public long getDownstreamBytes() {
    return downstreamBytes;
  }

  public void setDownstreamBytes(long downstreamBytes) {
    this.downstreamBytes = downstreamBytes;
  }

  public long getDownstreamPackets() {
    return downstreamPackets;
  }

  public void setDownstreamPackets(long downstreamPackets) {
    this.downstreamPackets = downstreamPackets;
  }

  public long getUpstreamBytes() {
    return upstreamBytes;
  }

  public void setUpstreamBytes(long upstreamBytes) {
    this.upstreamBytes = upstreamBytes;
  }

  public long getUpstreamPackets() {
    return upstreamPackets;
  }

  public void setUpstreamPackets(long upstreamPackets) {
    this.upstreamPackets = upstreamPackets;
  }

  public long getTransmitBytes() {
    return transmitBytes;
  }

  public void setTransmitBytes(long transmitBytes) {
    this.transmitBytes = transmitBytes;
  }

  public long getTransmitPackets() {
    return transmitPackets;
  }

  public void setTransmitPackets(long transmitPackets) {
    this.transmitPackets = transmitPackets;
  }

}
