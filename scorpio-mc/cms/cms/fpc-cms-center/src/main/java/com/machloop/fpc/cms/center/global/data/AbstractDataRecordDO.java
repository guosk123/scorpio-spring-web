package com.machloop.fpc.cms.center.global.data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author liumeng
 *
 * create at 2019年10月10日, fpc-manager
 */
public abstract class AbstractDataRecordDO {

  private Date timestamp;
  private String networkId;
  private String networkGroupId;
  private String serviceId;

  // KPI
  private long totalBytes;
  private long totalPackets;
  private long establishedSessions;
  private long tcpClientNetworkLatency;
  private long tcpClientNetworkLatencyCounts;
  private long tcpServerNetworkLatency;
  private long tcpServerNetworkLatencyCounts;
  private long serverResponseLatency;
  private long serverResponseLatencyCounts;
  private long tcpClientRetransmissionPackets;
  private long tcpClientPackets;
  private long tcpServerRetransmissionPackets;
  private long tcpServerPackets;
  private long tcpClientZeroWindowPackets;
  private long tcpServerZeroWindowPackets;
  private long tcpEstablishedFailCounts;
  private long tcpEstablishedSuccessCounts;

  // 二次计算数据
  @JsonIgnore
  private double tcpClientNetworkLatencyAvg;
  @JsonIgnore
  private double tcpServerNetworkLatencyAvg;
  @JsonIgnore
  private double serverResponseLatencyAvg;
  @JsonIgnore
  private double tcpClientRetransmissionRate;
  @JsonIgnore
  private double tcpServerRetransmissionRate;

  @Override
  public String toString() {
    return "AbstractDataRecordDO [timestamp=" + timestamp + ", networkId=" + networkId
        + ", networkGroupId=" + networkGroupId + ", serviceId=" + serviceId + ", totalBytes="
        + totalBytes + ", totalPackets=" + totalPackets + ", establishedSessions="
        + establishedSessions + ", tcpClientNetworkLatency=" + tcpClientNetworkLatency
        + ", tcpClientNetworkLatencyCounts=" + tcpClientNetworkLatencyCounts
        + ", tcpServerNetworkLatency=" + tcpServerNetworkLatency
        + ", tcpServerNetworkLatencyCounts=" + tcpServerNetworkLatencyCounts
        + ", serverResponseLatency=" + serverResponseLatency + ", serverResponseLatencyCounts="
        + serverResponseLatencyCounts + ", tcpClientRetransmissionPackets="
        + tcpClientRetransmissionPackets + ", tcpClientPackets=" + tcpClientPackets
        + ", tcpServerRetransmissionPackets=" + tcpServerRetransmissionPackets
        + ", tcpServerPackets=" + tcpServerPackets + ", tcpClientZeroWindowPackets="
        + tcpClientZeroWindowPackets + ", tcpServerZeroWindowPackets=" + tcpServerZeroWindowPackets
        + ", tcpEstablishedFailCounts=" + tcpEstablishedFailCounts
        + ", tcpEstablishedSuccessCounts=" + tcpEstablishedSuccessCounts
        + ", tcpClientNetworkLatencyAvg=" + tcpClientNetworkLatencyAvg
        + ", tcpServerNetworkLatencyAvg=" + tcpServerNetworkLatencyAvg
        + ", serverResponseLatencyAvg=" + serverResponseLatencyAvg
        + ", tcpClientRetransmissionRate=" + tcpClientRetransmissionRate
        + ", tcpServerRetransmissionRate=" + tcpServerRetransmissionRate + "]";
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

  public String getNetworkGroupId() {
    return networkGroupId;
  }

  public void setNetworkGroupId(String networkGroupId) {
    this.networkGroupId = networkGroupId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
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

  public long getEstablishedSessions() {
    return establishedSessions;
  }

  public void setEstablishedSessions(long establishedSessions) {
    this.establishedSessions = establishedSessions;
  }

  public long getTcpClientNetworkLatency() {
    return tcpClientNetworkLatency;
  }

  public void setTcpClientNetworkLatency(long tcpClientNetworkLatency) {
    this.tcpClientNetworkLatency = tcpClientNetworkLatency;
  }

  public long getTcpClientNetworkLatencyCounts() {
    return tcpClientNetworkLatencyCounts;
  }

  public void setTcpClientNetworkLatencyCounts(long tcpClientNetworkLatencyCounts) {
    this.tcpClientNetworkLatencyCounts = tcpClientNetworkLatencyCounts;
  }

  public long getTcpServerNetworkLatency() {
    return tcpServerNetworkLatency;
  }

  public void setTcpServerNetworkLatency(long tcpServerNetworkLatency) {
    this.tcpServerNetworkLatency = tcpServerNetworkLatency;
  }

  public long getTcpServerNetworkLatencyCounts() {
    return tcpServerNetworkLatencyCounts;
  }

  public void setTcpServerNetworkLatencyCounts(long tcpServerNetworkLatencyCounts) {
    this.tcpServerNetworkLatencyCounts = tcpServerNetworkLatencyCounts;
  }

  public long getServerResponseLatency() {
    return serverResponseLatency;
  }

  public void setServerResponseLatency(long serverResponseLatency) {
    this.serverResponseLatency = serverResponseLatency;
  }

  public long getServerResponseLatencyCounts() {
    return serverResponseLatencyCounts;
  }

  public void setServerResponseLatencyCounts(long serverResponseLatencyCounts) {
    this.serverResponseLatencyCounts = serverResponseLatencyCounts;
  }

  public long getTcpClientRetransmissionPackets() {
    return tcpClientRetransmissionPackets;
  }

  public void setTcpClientRetransmissionPackets(long tcpClientRetransmissionPackets) {
    this.tcpClientRetransmissionPackets = tcpClientRetransmissionPackets;
  }

  public long getTcpClientPackets() {
    return tcpClientPackets;
  }

  public void setTcpClientPackets(long tcpClientPackets) {
    this.tcpClientPackets = tcpClientPackets;
  }

  public long getTcpServerRetransmissionPackets() {
    return tcpServerRetransmissionPackets;
  }

  public void setTcpServerRetransmissionPackets(long tcpServerRetransmissionPackets) {
    this.tcpServerRetransmissionPackets = tcpServerRetransmissionPackets;
  }

  public long getTcpServerPackets() {
    return tcpServerPackets;
  }

  public void setTcpServerPackets(long tcpServerPackets) {
    this.tcpServerPackets = tcpServerPackets;
  }

  public long getTcpClientZeroWindowPackets() {
    return tcpClientZeroWindowPackets;
  }

  public void setTcpClientZeroWindowPackets(long tcpClientZeroWindowPackets) {
    this.tcpClientZeroWindowPackets = tcpClientZeroWindowPackets;
  }

  public long getTcpServerZeroWindowPackets() {
    return tcpServerZeroWindowPackets;
  }

  public void setTcpServerZeroWindowPackets(long tcpServerZeroWindowPackets) {
    this.tcpServerZeroWindowPackets = tcpServerZeroWindowPackets;
  }

  public long getTcpEstablishedFailCounts() {
    return tcpEstablishedFailCounts;
  }

  public void setTcpEstablishedFailCounts(long tcpEstablishedFailCounts) {
    this.tcpEstablishedFailCounts = tcpEstablishedFailCounts;
  }

  public long getTcpEstablishedSuccessCounts() {
    return tcpEstablishedSuccessCounts;
  }

  public void setTcpEstablishedSuccessCounts(long tcpEstablishedSuccessCounts) {
    this.tcpEstablishedSuccessCounts = tcpEstablishedSuccessCounts;
  }

  public double getTcpClientNetworkLatencyAvg() {
    return tcpClientNetworkLatencyAvg;
  }

  public void setTcpClientNetworkLatencyAvg(double tcpClientNetworkLatencyAvg) {
    this.tcpClientNetworkLatencyAvg = tcpClientNetworkLatencyAvg;
  }

  public double getTcpServerNetworkLatencyAvg() {
    return tcpServerNetworkLatencyAvg;
  }

  public void setTcpServerNetworkLatencyAvg(double tcpServerNetworkLatencyAvg) {
    this.tcpServerNetworkLatencyAvg = tcpServerNetworkLatencyAvg;
  }

  public double getServerResponseLatencyAvg() {
    return serverResponseLatencyAvg;
  }

  public void setServerResponseLatencyAvg(double serverResponseLatencyAvg) {
    this.serverResponseLatencyAvg = serverResponseLatencyAvg;
  }

  public double getTcpClientRetransmissionRate() {
    return tcpClientRetransmissionRate;
  }

  public void setTcpClientRetransmissionRate(double tcpClientRetransmissionRate) {
    this.tcpClientRetransmissionRate = tcpClientRetransmissionRate;
  }

  public double getTcpServerRetransmissionRate() {
    return tcpServerRetransmissionRate;
  }

  public void setTcpServerRetransmissionRate(double tcpServerRetransmissionRate) {
    this.tcpServerRetransmissionRate = tcpServerRetransmissionRate;
  }

}
