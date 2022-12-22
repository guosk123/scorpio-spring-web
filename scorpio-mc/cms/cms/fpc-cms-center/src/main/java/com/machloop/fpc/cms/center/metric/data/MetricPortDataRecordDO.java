package com.machloop.fpc.cms.center.metric.data;

import com.machloop.fpc.cms.center.global.data.AbstractDataRecordDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public class MetricPortDataRecordDO extends AbstractDataRecordDO {
  private int port;
  private String ipProtocol;

  private long downstreamBytes;
  private long downstreamPackets;
  private long upstreamBytes;
  private long upstreamPackets;
  private long totalPayloadBytes;
  private long totalPayloadPackets;
  private long downstreamPayloadBytes;
  private long downstreamPayloadPackets;
  private long upstreamPayloadBytes;
  private long upstreamPayloadPackets;
  private long tcpSynPackets;
  private long tcpSynAckPackets;
  private long tcpSynRstPackets;

  @Override
  public String toString() {
    return "MetricPortDataRecordDO [port=" + port + ", ipProtocol=" + ipProtocol
        + ", downstreamBytes=" + downstreamBytes + ", downstreamPackets=" + downstreamPackets
        + ", upstreamBytes=" + upstreamBytes + ", upstreamPackets=" + upstreamPackets
        + ", totalPayloadBytes=" + totalPayloadBytes + ", totalPayloadPackets="
        + totalPayloadPackets + ", downstreamPayloadBytes=" + downstreamPayloadBytes
        + ", downstreamPayloadPackets=" + downstreamPayloadPackets + ", upstreamPayloadBytes="
        + upstreamPayloadBytes + ", upstreamPayloadPackets=" + upstreamPayloadPackets
        + ", tcpSynPackets=" + tcpSynPackets + ", tcpSynAckPackets=" + tcpSynAckPackets
        + ", tcpSynRstPackets=" + tcpSynRstPackets + "]";
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getIpProtocol() {
    return ipProtocol;
  }

  public void setIpProtocol(String ipProtocol) {
    this.ipProtocol = ipProtocol;
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

  public long getTotalPayloadBytes() {
    return totalPayloadBytes;
  }

  public void setTotalPayloadBytes(long totalPayloadBytes) {
    this.totalPayloadBytes = totalPayloadBytes;
  }

  public long getTotalPayloadPackets() {
    return totalPayloadPackets;
  }

  public void setTotalPayloadPackets(long totalPayloadPackets) {
    this.totalPayloadPackets = totalPayloadPackets;
  }

  public long getDownstreamPayloadBytes() {
    return downstreamPayloadBytes;
  }

  public void setDownstreamPayloadBytes(long downstreamPayloadBytes) {
    this.downstreamPayloadBytes = downstreamPayloadBytes;
  }

  public long getDownstreamPayloadPackets() {
    return downstreamPayloadPackets;
  }

  public void setDownstreamPayloadPackets(long downstreamPayloadPackets) {
    this.downstreamPayloadPackets = downstreamPayloadPackets;
  }

  public long getUpstreamPayloadBytes() {
    return upstreamPayloadBytes;
  }

  public void setUpstreamPayloadBytes(long upstreamPayloadBytes) {
    this.upstreamPayloadBytes = upstreamPayloadBytes;
  }

  public long getUpstreamPayloadPackets() {
    return upstreamPayloadPackets;
  }

  public void setUpstreamPayloadPackets(long upstreamPayloadPackets) {
    this.upstreamPayloadPackets = upstreamPayloadPackets;
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

}