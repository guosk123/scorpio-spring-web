package com.machloop.fpc.cms.center.metric.data;

import com.machloop.fpc.cms.center.global.data.AbstractDataRecordDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public class MetricHostgroupDataRecordDO extends AbstractDataRecordDO {
  private String hostgroupId;

  private long downstreamBytes;
  private long upstreamBytes;
  private long downstreamPackets;
  private long upstreamPackets;
  private long tcpSynPackets;
  private long tcpSynAckPackets;
  private long tcpSynRstPackets;
  private long tcpZeroWindowPackets;

  @Override
  public String toString() {
    return "MetricHostgroupDataRecordDO [hostgroupId=" + hostgroupId + ", downstreamBytes="
        + downstreamBytes + ", upstreamBytes=" + upstreamBytes + ", downstreamPackets="
        + downstreamPackets + ", upstreamPackets=" + upstreamPackets + ", tcpSynPackets="
        + tcpSynPackets + ", tcpSynAckPackets=" + tcpSynAckPackets + ", tcpSynRstPackets="
        + tcpSynRstPackets + ", tcpZeroWindowPackets" + tcpZeroWindowPackets + "]";
  }

  public String getHostgroupId() {
    return hostgroupId;
  }

  public void setHostgroupId(String hostGroupId) {
    this.hostgroupId = hostGroupId;
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

  public long getTcpSynPackets() {
    return tcpSynPackets;
  }

  public void setTcpSynPackets(long tcpSynPackets) {
    this.tcpSynPackets = tcpSynPackets;
  }

  public long getTcpSynAckPackets() {
    return tcpSynAckPackets;
  }

  public void setTcpSynAckPackets(long tcpSynAckPackets) {
    this.tcpSynAckPackets = tcpSynAckPackets;
  }

  public long getTcpSynRstPackets() {
    return tcpSynRstPackets;
  }

  public void setTcpSynRstPackets(long tcpSynRstPackets) {
    this.tcpSynRstPackets = tcpSynRstPackets;
  }

  public long getTcpZeroWindowPackets() {
    return tcpZeroWindowPackets;
  }

  public void setTcpZeroWindowPackets(long tcpZeroWindowPackets) {
    this.tcpZeroWindowPackets = tcpZeroWindowPackets;
  }

}
