package com.machloop.fpc.cms.center.metric.data;

import com.machloop.fpc.cms.center.global.data.AbstractDataRecordDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public class MetricApplicationDataRecordDO extends AbstractDataRecordDO {
  private int applicationId;
  private int categoryId;
  private int subcategoryId;
  private int type;

  private long bytepsPeak;
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
  private long tcpZeroWindowPackets;

  @Override
  public String toString() {
    return "MetricApplicationDataRecordDO [applicationId=" + applicationId + ", categoryId="
        + categoryId + ", subcategoryId=" + subcategoryId + ", type=" + type + ", bytepsPeak="
        + bytepsPeak + ", downstreamBytes=" + downstreamBytes + ", downstreamPackets="
        + downstreamPackets + ", upstreamBytes=" + upstreamBytes + ", upstreamPackets="
        + upstreamPackets + ", totalPayloadBytes=" + totalPayloadBytes + ", totalPayloadPackets="
        + totalPayloadPackets + ", downstreamPayloadBytes=" + downstreamPayloadBytes
        + ", downstreamPayloadPackets=" + downstreamPayloadPackets + ", upstreamPayloadBytes="
        + upstreamPayloadBytes + ", upstreamPayloadPackets=" + upstreamPayloadPackets
        + ", tcpSynPackets=" + tcpSynPackets + ", tcpSynAckPackets=" + tcpSynAckPackets
        + ", tcpSynRstPackets=" + tcpSynRstPackets +", tcpZeroWindowPackets" + tcpZeroWindowPackets + "]";
  }

  public int getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(int applicationId) {
    this.applicationId = applicationId;
  }

  public int getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(int categoryId) {
    this.categoryId = categoryId;
  }

  public int getSubcategoryId() {
    return subcategoryId;
  }

  public void setSubcategoryId(int subcategoryId) {
    this.subcategoryId = subcategoryId;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public long getBytepsPeak() {
    return bytepsPeak;
  }

  public void setBytepsPeak(long bytepsPeak) {
    this.bytepsPeak = bytepsPeak;
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

  public long getTcpZeroWindowPackets() {
    return tcpZeroWindowPackets;
  }

  public void setTcpZeroWindowPackets(long tcpZeroWindowPackets) {
    this.tcpZeroWindowPackets = tcpZeroWindowPackets;
  }


}
