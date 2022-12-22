package com.machloop.fpc.manager.metric.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.machloop.fpc.manager.global.data.AbstractDataRecordDO;

/**
 * @author guosk
 * <p>
 * create at 2020年12月10日, fpc-manager
 */
public class MetricL3DeviceDataRecordDO extends AbstractDataRecordDO {
  private String macAddress;
  private String ipAddress;
  private String ipLocality;

  private long downstreamBytes;
  private long upstreamBytes;
  private long downstreamPackets;
  private long upstreamPackets;
  private long activeEstablishedSessions;
  private long passiveEstablishedSessions;
  private long tcpSynPackets;
  private long tcpSynAckPackets;
  private long tcpSynRstPackets;
  private long tcpZeroWindowPackets;
  // 新增字段，区分总体服务、内网服务和外网服务
  // 建连失败次数
  private long tcpClientEstablishedFailCountsInsideService;
  private long tcpClientEstablishedFailCountsOutsideService;
  private long tcpClientEstablishedFailCounts;
  private long tcpServerEstablishedFailCountsInsideService;
  private long tcpServerEstablishedFailCountsOutsideService;
  private long tcpServerEstablishedFailCounts;
  // 建连总次数
  private long tcpClientEstablishedCountsInsideService;
  private long tcpClientEstablishedCountsOutsideService;
  private long tcpClientEstablishedCounts;
  private long tcpServerEstablishedCountsInsideService;
  private long tcpServerEstablishedCountsOutsideService;
  private long tcpServerEstablishedCounts;
  // 重传包数
  private long tcpClientRecvRetransmissionPacketsInsideService;
  private long tcpClientSendRetransmissionPacketsInsideService;
  private long tcpClientRecvRetransmissionPacketsOutsideService;
  private long tcpClientSendRetransmissionPacketsOutsideService;
  private long tcpClientRecvRetransmissionPackets;
  private long tcpClientSendRetransmissionPackets;
  private long tcpServerRecvRetransmissionPacketsInsideService;
  private long tcpServerSendRetransmissionPacketsInsideService;
  private long tcpServerRecvRetransmissionPacketsOutsideService;
  private long tcpServerSendRetransmissionPacketsOutsideService;
  private long tcpServerRecvRetransmissionPackets;
  private long tcpServerSendRetransmissionPackets;
  // 总包数
  private long tcpClientRecvPacketsInsideService;
  private long tcpClientSendPacketsInsideService;
  private long tcpClientRecvPacketsOutsideService;
  private long tcpClientSendPacketsOutsideService;
  private long tcpClientRecvPackets;
  private long tcpClientSendPackets;
  private long tcpServerRecvPacketsInsideService;
  private long tcpServerSendPacketsInsideService;
  private long tcpServerRecvPacketsOutsideService;
  private long tcpServerSendPacketsOutsideService;
  private long tcpServerRecvPackets;
  private long tcpServerSendPackets;

  // 二次计算数据
  // 建连分析(总体服务)
  @JsonIgnore
  private double tcpEstablishedCounts;

  public double getTcpEstablishedCounts() {
    return tcpEstablishedCounts;
  }

  public void setTcpEstablishedCounts(double tcpEstablishedCounts) {
    this.tcpEstablishedCounts = tcpEstablishedCounts;
  }

  @Override
  public String toString() {
    return "MetricL3DeviceDataRecordDO{" + "macAddress='" + macAddress + '\'' + ", ipAddress='"
        + ipAddress + '\'' + ", ipLocality='" + ipLocality + '\'' + ", downstreamBytes="
        + downstreamBytes + ", upstreamBytes=" + upstreamBytes + ", downstreamPackets="
        + downstreamPackets + ", upstreamPackets=" + upstreamPackets
        + ", activeEstablishedSessions=" + activeEstablishedSessions
        + ", passiveEstablishedSessions=" + passiveEstablishedSessions + ", tcpSynPackets="
        + tcpSynPackets + ", tcpSynAckPackets=" + tcpSynAckPackets + ", tcpSynRstPackets="
        + tcpSynRstPackets + ", tcpZeroWindowPackets=" + tcpZeroWindowPackets
        + ", tcpClientEstablishedFailCountsInsideService="
        + tcpClientEstablishedFailCountsInsideService
        + ", tcpClientEstablishedFailCountsOutsideService="
        + tcpClientEstablishedFailCountsOutsideService + ", tcpClientEstablishedFailCounts="
        + tcpClientEstablishedFailCounts + ", tcpServerEstablishedFailCountsInsideService="
        + tcpServerEstablishedFailCountsInsideService
        + ", tcpServerEstablishedFailCountsOutsideService="
        + tcpServerEstablishedFailCountsOutsideService + ", tcpServerEstablishedFailCounts="
        + tcpServerEstablishedFailCounts + ", tcpClientEstablishedCountsInsideService="
        + tcpClientEstablishedCountsInsideService + ", tcpClientEstablishedCountsOutsideService="
        + tcpClientEstablishedCountsOutsideService + ", tcpClientEstablishedCounts="
        + tcpClientEstablishedCounts + ", tcpServerEstablishedCountsInsideService="
        + tcpServerEstablishedCountsInsideService + ", tcpServerEstablishedCountsOutsideService="
        + tcpServerEstablishedCountsOutsideService + ", tcpServerEstablishedCounts="
        + tcpServerEstablishedCounts + ", tcpClientRecvRetransmissionPacketsInsideService="
        + tcpClientRecvRetransmissionPacketsInsideService
        + ", tcpClientSendRetransmissionPacketsInsideService="
        + tcpClientSendRetransmissionPacketsInsideService
        + ", tcpClientRecvRetransmissionPacketsOutsideService="
        + tcpClientRecvRetransmissionPacketsOutsideService
        + ", tcpClientSendRetransmissionPacketsOutsideService="
        + tcpClientSendRetransmissionPacketsOutsideService + ", tcpClientRecvRetransmissionPackets="
        + tcpClientRecvRetransmissionPackets + ", tcpClientSendRetransmissionPackets="
        + tcpClientSendRetransmissionPackets + ", tcpServerRecvRetransmissionPacketsInsideService="
        + tcpServerRecvRetransmissionPacketsInsideService
        + ", tcpServerSendRetransmissionPacketsInsideService="
        + tcpServerSendRetransmissionPacketsInsideService
        + ", tcpServerRecvRetransmissionPacketsOutsideService="
        + tcpServerRecvRetransmissionPacketsOutsideService
        + ", tcpServerSendRetransmissionPacketsOutsideService="
        + tcpServerSendRetransmissionPacketsOutsideService + ", tcpServerRecvRetransmissionPackets="
        + tcpServerRecvRetransmissionPackets + ", tcpServerSendRetransmissionPackets="
        + tcpServerSendRetransmissionPackets + ", tcpClientRecvPacketsInsideService="
        + tcpClientRecvPacketsInsideService + ", tcpClientSendPacketsInsideService="
        + tcpClientSendPacketsInsideService + ", tcpClientRecvPacketsOutsideService="
        + tcpClientRecvPacketsOutsideService + ", tcpClientSendPacketsOutsideService="
        + tcpClientSendPacketsOutsideService + ", tcpClientRecvPackets=" + tcpClientRecvPackets
        + ", tcpClientSendPackets=" + tcpClientSendPackets + ", tcpServerRecvPacketsInsideService="
        + tcpServerRecvPacketsInsideService + ", tcpServerSendPacketsInsideService="
        + tcpServerSendPacketsInsideService + ", tcpServerRecvPacketsOutsideService="
        + tcpServerRecvPacketsOutsideService + ", tcpServerSendPacketsOutsideService="
        + tcpServerSendPacketsOutsideService + ", tcpServerRecvPackets=" + tcpServerRecvPackets
        + ", tcpServerSendPackets=" + tcpServerSendPackets + ", tcpEstablishedCounts="
        + tcpEstablishedCounts + ", tcpEstablishedFailCountsRate=" + tcpEstablishedFailCountsRate
        + ", tcpServerEstablishedFailCountsRate=" + tcpServerEstablishedFailCountsRate
        + ", tcpClientEstablishedFailCountsRate=" + tcpClientEstablishedFailCountsRate
        + ", tcpEstablishedFailCountsInsideService=" + tcpEstablishedFailCountsInsideService
        + ", tcpEstablishedCountsInsideService=" + tcpEstablishedCountsInsideService
        + ", tcpEstablishedFailCountsInsideServiceRate=" + tcpEstablishedFailCountsInsideServiceRate
        + ", tcpServerEstablishedFailCountsInsideServiceRate="
        + tcpServerEstablishedFailCountsInsideServiceRate
        + ", tcpClientEstablishedFailCountsInsideServiceRate="
        + tcpClientEstablishedFailCountsInsideServiceRate
        + ", tcpEstablishedFailCountsOutsideService=" + tcpEstablishedFailCountsOutsideService
        + ", tcpEstablishedCountsOutsideService=" + tcpEstablishedCountsOutsideService
        + ", tcpEstablishedFailCountsOutsideServiceRate="
        + tcpEstablishedFailCountsOutsideServiceRate
        + ", tcpServerEstablishedFailCountsOutsideServiceRate="
        + tcpServerEstablishedFailCountsOutsideServiceRate
        + ", tcpClientEstablishedFailCountsOutsideServiceRate="
        + tcpClientEstablishedFailCountsOutsideServiceRate
        + ", tcpServerRecvRetransmissionPacketsRate=" + tcpServerRecvRetransmissionPacketsRate
        + ", tcpServerSendRetransmissionPacketsRate=" + tcpServerSendRetransmissionPacketsRate
        + ", tcpClientRecvRetransmissionPacketsRate=" + tcpClientRecvRetransmissionPacketsRate
        + ", tcpClientSendRetransmissionPacketsRate=" + tcpClientSendRetransmissionPacketsRate
        + ", tcpServerRecvRetransmissionPacketsInsideServiceRate="
        + tcpServerRecvRetransmissionPacketsInsideServiceRate
        + ", tcpServerSendRetransmissionPacketsInsideServiceRate="
        + tcpServerSendRetransmissionPacketsInsideServiceRate
        + ", tcpClientRecvRetransmissionPacketsInsideServiceRate="
        + tcpClientRecvRetransmissionPacketsInsideServiceRate
        + ", tcpClientSendRetransmissionPacketsInsideServiceRate="
        + tcpClientSendRetransmissionPacketsInsideServiceRate
        + ", tcpServerRecvRetransmissionPacketsOutsideServiceRate="
        + tcpServerRecvRetransmissionPacketsOutsideServiceRate
        + ", tcpServerSendRetransmissionPacketsOutsideServiceRate="
        + tcpServerSendRetransmissionPacketsOutsideServiceRate
        + ", tcpClientRecvRetransmissionPacketsOutsideServiceRate="
        + tcpClientRecvRetransmissionPacketsOutsideServiceRate
        + ", tcpClientSendRetransmissionPacketsOutsideServiceRate="
        + tcpClientSendRetransmissionPacketsOutsideServiceRate + '}';
  }

  @JsonIgnore
  private double tcpEstablishedFailCountsRate;

  @JsonIgnore
  private double tcpServerEstablishedFailCountsRate;

  @JsonIgnore
  private double tcpClientEstablishedFailCountsRate;

  // 建连分析(内网服务)
  @JsonIgnore
  private double tcpEstablishedFailCountsInsideService;

  @JsonIgnore
  private double tcpEstablishedCountsInsideService;

  @JsonIgnore
  private double tcpEstablishedFailCountsInsideServiceRate;

  @JsonIgnore
  private double tcpServerEstablishedFailCountsInsideServiceRate;

  @JsonIgnore
  private double tcpClientEstablishedFailCountsInsideServiceRate;

  // 建连分析(外网服务)
  @JsonIgnore
  private double tcpEstablishedFailCountsOutsideService;

  @JsonIgnore
  private double tcpEstablishedCountsOutsideService;

  @JsonIgnore
  private double tcpEstablishedFailCountsOutsideServiceRate;

  @JsonIgnore
  private double tcpServerEstablishedFailCountsOutsideServiceRate;

  @JsonIgnore
  private double tcpClientEstablishedFailCountsOutsideServiceRate;

  // 重传分析(总体服务)
  @JsonIgnore
  private double tcpServerRecvRetransmissionPacketsRate;

  @JsonIgnore
  private double tcpServerSendRetransmissionPacketsRate;

  @JsonIgnore
  private double tcpClientRecvRetransmissionPacketsRate;

  @JsonIgnore
  private double tcpClientSendRetransmissionPacketsRate;

  // 重传分析(内网服务)
  @JsonIgnore
  private double tcpServerRecvRetransmissionPacketsInsideServiceRate;

  @JsonIgnore
  private double tcpServerSendRetransmissionPacketsInsideServiceRate;

  @JsonIgnore
  private double tcpClientRecvRetransmissionPacketsInsideServiceRate;

  @JsonIgnore
  private double tcpClientSendRetransmissionPacketsInsideServiceRate;

  // 重传分析(外网服务)
  @JsonIgnore
  private double tcpServerRecvRetransmissionPacketsOutsideServiceRate;

  @JsonIgnore
  private double tcpServerSendRetransmissionPacketsOutsideServiceRate;

  @JsonIgnore
  private double tcpClientRecvRetransmissionPacketsOutsideServiceRate;

  @JsonIgnore
  private double tcpClientSendRetransmissionPacketsOutsideServiceRate;


  public String getMacAddress() {
    return macAddress;
  }

  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getIpLocality() {
    return ipLocality;
  }

  public void setIpLocality(String ipLocality) {
    this.ipLocality = ipLocality;
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

  public long getActiveEstablishedSessions() {
    return activeEstablishedSessions;
  }

  public void setActiveEstablishedSessions(long activeEstablishedSessions) {
    this.activeEstablishedSessions = activeEstablishedSessions;
  }

  public long getPassiveEstablishedSessions() {
    return passiveEstablishedSessions;
  }

  public void setPassiveEstablishedSessions(long passiveEstablishedSessions) {
    this.passiveEstablishedSessions = passiveEstablishedSessions;
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

  public long getTcpClientEstablishedFailCountsInsideService() {
    return tcpClientEstablishedFailCountsInsideService;
  }

  public void setTcpClientEstablishedFailCountsInsideService(
      long tcpClientEstablishedFailCountsInsideService) {
    this.tcpClientEstablishedFailCountsInsideService = tcpClientEstablishedFailCountsInsideService;
  }

  public long getTcpClientEstablishedFailCountsOutsideService() {
    return tcpClientEstablishedFailCountsOutsideService;
  }

  public void setTcpClientEstablishedFailCountsOutsideService(
      long tcpClientEstablishedFailCountsOutsideService) {
    this.tcpClientEstablishedFailCountsOutsideService = tcpClientEstablishedFailCountsOutsideService;
  }

  public long getTcpClientEstablishedFailCounts() {
    return tcpClientEstablishedFailCounts;
  }

  public void setTcpClientEstablishedFailCounts(long tcpClientEstablishedFailCounts) {
    this.tcpClientEstablishedFailCounts = tcpClientEstablishedFailCounts;
  }

  public long getTcpServerEstablishedFailCountsInsideService() {
    return tcpServerEstablishedFailCountsInsideService;
  }

  public void setTcpServerEstablishedFailCountsInsideService(
      long tcpServerEstablishedFailCountsInsideService) {
    this.tcpServerEstablishedFailCountsInsideService = tcpServerEstablishedFailCountsInsideService;
  }

  public long getTcpServerEstablishedFailCountsOutsideService() {
    return tcpServerEstablishedFailCountsOutsideService;
  }

  public void setTcpServerEstablishedFailCountsOutsideService(
      long tcpServerEstablishedFailCountsOutsideService) {
    this.tcpServerEstablishedFailCountsOutsideService = tcpServerEstablishedFailCountsOutsideService;
  }

  public long getTcpServerEstablishedFailCounts() {
    return tcpServerEstablishedFailCounts;
  }

  public void setTcpServerEstablishedFailCounts(long tcpServerEstablishedFailCounts) {
    this.tcpServerEstablishedFailCounts = tcpServerEstablishedFailCounts;
  }

  public long getTcpClientEstablishedCountsInsideService() {
    return tcpClientEstablishedCountsInsideService;
  }

  public void setTcpClientEstablishedCountsInsideService(
      long tcpClientEstablishedCountsInsideService) {
    this.tcpClientEstablishedCountsInsideService = tcpClientEstablishedCountsInsideService;
  }

  public long getTcpClientEstablishedCountsOutsideService() {
    return tcpClientEstablishedCountsOutsideService;
  }

  public void setTcpClientEstablishedCountsOutsideService(
      long tcpClientEstablishedCountsOutsideService) {
    this.tcpClientEstablishedCountsOutsideService = tcpClientEstablishedCountsOutsideService;
  }

  public long getTcpClientEstablishedCounts() {
    return tcpClientEstablishedCounts;
  }

  public void setTcpClientEstablishedCounts(long tcpClientEstablishedCounts) {
    this.tcpClientEstablishedCounts = tcpClientEstablishedCounts;
  }

  public long getTcpServerEstablishedCountsInsideService() {
    return tcpServerEstablishedCountsInsideService;
  }

  public void setTcpServerEstablishedCountsInsideService(
      long tcpServerEstablishedCountsInsideService) {
    this.tcpServerEstablishedCountsInsideService = tcpServerEstablishedCountsInsideService;
  }

  public long getTcpServerEstablishedCountsOutsideService() {
    return tcpServerEstablishedCountsOutsideService;
  }

  public void setTcpServerEstablishedCountsOutsideService(
      long tcpServerEstablishedCountsOutsideService) {
    this.tcpServerEstablishedCountsOutsideService = tcpServerEstablishedCountsOutsideService;
  }

  public long getTcpServerEstablishedCounts() {
    return tcpServerEstablishedCounts;
  }

  public void setTcpServerEstablishedCounts(long tcpServerEstablishedCounts) {
    this.tcpServerEstablishedCounts = tcpServerEstablishedCounts;
  }

  public long getTcpClientRecvRetransmissionPacketsInsideService() {
    return tcpClientRecvRetransmissionPacketsInsideService;
  }

  public void setTcpClientRecvRetransmissionPacketsInsideService(
      long tcpClientRecvRetransmissionPacketsInsideService) {
    this.tcpClientRecvRetransmissionPacketsInsideService = tcpClientRecvRetransmissionPacketsInsideService;
  }

  public long getTcpClientSendRetransmissionPacketsInsideService() {
    return tcpClientSendRetransmissionPacketsInsideService;
  }

  public void setTcpClientSendRetransmissionPacketsInsideService(
      long tcpClientSendRetransmissionPacketsInsideService) {
    this.tcpClientSendRetransmissionPacketsInsideService = tcpClientSendRetransmissionPacketsInsideService;
  }

  public long getTcpClientRecvRetransmissionPacketsOutsideService() {
    return tcpClientRecvRetransmissionPacketsOutsideService;
  }

  public void setTcpClientRecvRetransmissionPacketsOutsideService(
      long tcpClientRecvRetransmissionPacketsOutsideService) {
    this.tcpClientRecvRetransmissionPacketsOutsideService = tcpClientRecvRetransmissionPacketsOutsideService;
  }

  public long getTcpClientSendRetransmissionPacketsOutsideService() {
    return tcpClientSendRetransmissionPacketsOutsideService;
  }

  public void setTcpClientSendRetransmissionPacketsOutsideService(
      long tcpClientSendRetransmissionPacketsOutsideService) {
    this.tcpClientSendRetransmissionPacketsOutsideService = tcpClientSendRetransmissionPacketsOutsideService;
  }

  public long getTcpClientRecvRetransmissionPackets() {
    return tcpClientRecvRetransmissionPackets;
  }

  public void setTcpClientRecvRetransmissionPackets(long tcpClientRecvRetransmissionPackets) {
    this.tcpClientRecvRetransmissionPackets = tcpClientRecvRetransmissionPackets;
  }

  public long getTcpClientSendRetransmissionPackets() {
    return tcpClientSendRetransmissionPackets;
  }

  public void setTcpClientSendRetransmissionPackets(long tcpClientSendRetransmissionPackets) {
    this.tcpClientSendRetransmissionPackets = tcpClientSendRetransmissionPackets;
  }

  public long getTcpServerRecvRetransmissionPacketsInsideService() {
    return tcpServerRecvRetransmissionPacketsInsideService;
  }

  public void setTcpServerRecvRetransmissionPacketsInsideService(
      long tcpServerRecvRetransmissionPacketsInsideService) {
    this.tcpServerRecvRetransmissionPacketsInsideService = tcpServerRecvRetransmissionPacketsInsideService;
  }

  public long getTcpServerSendRetransmissionPacketsInsideService() {
    return tcpServerSendRetransmissionPacketsInsideService;
  }

  public void setTcpServerSendRetransmissionPacketsInsideService(
      long tcpServerSendRetransmissionPacketsInsideService) {
    this.tcpServerSendRetransmissionPacketsInsideService = tcpServerSendRetransmissionPacketsInsideService;
  }

  public long getTcpServerRecvRetransmissionPacketsOutsideService() {
    return tcpServerRecvRetransmissionPacketsOutsideService;
  }

  public void setTcpServerRecvRetransmissionPacketsOutsideService(
      long tcpServerRecvRetransmissionPacketsOutsideService) {
    this.tcpServerRecvRetransmissionPacketsOutsideService = tcpServerRecvRetransmissionPacketsOutsideService;
  }

  public long getTcpServerSendRetransmissionPacketsOutsideService() {
    return tcpServerSendRetransmissionPacketsOutsideService;
  }

  public void setTcpServerSendRetransmissionPacketsOutsideService(
      long tcpServerSendRetransmissionPacketsOutsideService) {
    this.tcpServerSendRetransmissionPacketsOutsideService = tcpServerSendRetransmissionPacketsOutsideService;
  }

  public long getTcpServerRecvRetransmissionPackets() {
    return tcpServerRecvRetransmissionPackets;
  }

  public void setTcpServerRecvRetransmissionPackets(long tcpServerRecvRetransmissionPackets) {
    this.tcpServerRecvRetransmissionPackets = tcpServerRecvRetransmissionPackets;
  }

  public long getTcpServerSendRetransmissionPackets() {
    return tcpServerSendRetransmissionPackets;
  }

  public void setTcpServerSendRetransmissionPackets(long tcpServerSendRetransmissionPackets) {
    this.tcpServerSendRetransmissionPackets = tcpServerSendRetransmissionPackets;
  }

  public long getTcpClientRecvPacketsInsideService() {
    return tcpClientRecvPacketsInsideService;
  }

  public void setTcpClientRecvPacketsInsideService(long tcpClientRecvPacketsInsideService) {
    this.tcpClientRecvPacketsInsideService = tcpClientRecvPacketsInsideService;
  }

  public long getTcpClientSendPacketsInsideService() {
    return tcpClientSendPacketsInsideService;
  }

  public void setTcpClientSendPacketsInsideService(long tcpClientSendPacketsInsideService) {
    this.tcpClientSendPacketsInsideService = tcpClientSendPacketsInsideService;
  }

  public long getTcpClientRecvPacketsOutsideService() {
    return tcpClientRecvPacketsOutsideService;
  }

  public void setTcpClientRecvPacketsOutsideService(long tcpClientRecvPacketsOutsideService) {
    this.tcpClientRecvPacketsOutsideService = tcpClientRecvPacketsOutsideService;
  }

  public long getTcpClientSendPacketsOutsideService() {
    return tcpClientSendPacketsOutsideService;
  }

  public void setTcpClientSendPacketsOutsideService(long tcpClientSendPacketsOutsideService) {
    this.tcpClientSendPacketsOutsideService = tcpClientSendPacketsOutsideService;
  }

  public long getTcpClientRecvPackets() {
    return tcpClientRecvPackets;
  }

  public void setTcpClientRecvPackets(long tcpClientRecvPackets) {
    this.tcpClientRecvPackets = tcpClientRecvPackets;
  }

  public long getTcpClientSendPackets() {
    return tcpClientSendPackets;
  }

  public void setTcpClientSendPackets(long tcpClientSendPackets) {
    this.tcpClientSendPackets = tcpClientSendPackets;
  }

  public long getTcpServerRecvPacketsInsideService() {
    return tcpServerRecvPacketsInsideService;
  }

  public void setTcpServerRecvPacketsInsideService(long tcpServerRecvPacketsInsideService) {
    this.tcpServerRecvPacketsInsideService = tcpServerRecvPacketsInsideService;
  }

  public long getTcpServerSendPacketsInsideService() {
    return tcpServerSendPacketsInsideService;
  }

  public void setTcpServerSendPacketsInsideService(long tcpServerSendPacketsInsideService) {
    this.tcpServerSendPacketsInsideService = tcpServerSendPacketsInsideService;
  }

  public long getTcpServerRecvPacketsOutsideService() {
    return tcpServerRecvPacketsOutsideService;
  }

  public void setTcpServerRecvPacketsOutsideService(long tcpServerRecvPacketsOutsideService) {
    this.tcpServerRecvPacketsOutsideService = tcpServerRecvPacketsOutsideService;
  }

  public long getTcpServerSendPacketsOutsideService() {
    return tcpServerSendPacketsOutsideService;
  }

  public void setTcpServerSendPacketsOutsideService(long tcpServerSendPacketsOutsideService) {
    this.tcpServerSendPacketsOutsideService = tcpServerSendPacketsOutsideService;
  }

  public long getTcpServerRecvPackets() {
    return tcpServerRecvPackets;
  }

  public void setTcpServerRecvPackets(long tcpServerRecvPackets) {
    this.tcpServerRecvPackets = tcpServerRecvPackets;
  }

  public long getTcpServerSendPackets() {
    return tcpServerSendPackets;
  }

  public void setTcpServerSendPackets(long tcpServerSendPackets) {
    this.tcpServerSendPackets = tcpServerSendPackets;
  }

  public double getTcpEstablishedFailCountsRate() {
    return tcpEstablishedFailCountsRate;
  }

  public void setTcpEstablishedFailCountsRate(double tcpEstablishedFailCountsRate) {
    this.tcpEstablishedFailCountsRate = tcpEstablishedFailCountsRate;
  }

  public double getTcpServerEstablishedFailCountsRate() {
    return tcpServerEstablishedFailCountsRate;
  }

  public void setTcpServerEstablishedFailCountsRate(double tcpServerEstablishedFailCountsRate) {
    this.tcpServerEstablishedFailCountsRate = tcpServerEstablishedFailCountsRate;
  }

  public double getTcpClientEstablishedFailCountsRate() {
    return tcpClientEstablishedFailCountsRate;
  }

  public void setTcpClientEstablishedFailCountsRate(double tcpClientEstablishedFailCountsRate) {
    this.tcpClientEstablishedFailCountsRate = tcpClientEstablishedFailCountsRate;
  }

  public double getTcpEstablishedFailCountsInsideService() {
    return tcpEstablishedFailCountsInsideService;
  }

  public void setTcpEstablishedFailCountsInsideService(
      double tcpEstablishedFailCountsInsideService) {
    this.tcpEstablishedFailCountsInsideService = tcpEstablishedFailCountsInsideService;
  }

  public double getTcpEstablishedCountsInsideService() {
    return tcpEstablishedCountsInsideService;
  }

  public void setTcpEstablishedCountsInsideService(double tcpEstablishedCountsInsideService) {
    this.tcpEstablishedCountsInsideService = tcpEstablishedCountsInsideService;
  }

  public double getTcpEstablishedFailCountsInsideServiceRate() {
    return tcpEstablishedFailCountsInsideServiceRate;
  }

  public void setTcpEstablishedFailCountsInsideServiceRate(
      double tcpEstablishedFailCountsInsideServiceRate) {
    this.tcpEstablishedFailCountsInsideServiceRate = tcpEstablishedFailCountsInsideServiceRate;
  }

  public double getTcpServerEstablishedFailCountsInsideServiceRate() {
    return tcpServerEstablishedFailCountsInsideServiceRate;
  }

  public void setTcpServerEstablishedFailCountsInsideServiceRate(
      double tcpServerEstablishedFailCountsInsideServiceRate) {
    this.tcpServerEstablishedFailCountsInsideServiceRate = tcpServerEstablishedFailCountsInsideServiceRate;
  }

  public double getTcpClientEstablishedFailCountsInsideServiceRate() {
    return tcpClientEstablishedFailCountsInsideServiceRate;
  }

  public void setTcpClientEstablishedFailCountsInsideServiceRate(
      double tcpClientEstablishedFailCountsInsideServiceRate) {
    this.tcpClientEstablishedFailCountsInsideServiceRate = tcpClientEstablishedFailCountsInsideServiceRate;
  }

  public double getTcpEstablishedFailCountsOutsideService() {
    return tcpEstablishedFailCountsOutsideService;
  }

  public void setTcpEstablishedFailCountsOutsideService(
      double tcpEstablishedFailCountsOutsideService) {
    this.tcpEstablishedFailCountsOutsideService = tcpEstablishedFailCountsOutsideService;
  }

  public double getTcpEstablishedCountsOutsideService() {
    return tcpEstablishedCountsOutsideService;
  }

  public void setTcpEstablishedCountsOutsideService(double tcpEstablishedCountsOutsideService) {
    this.tcpEstablishedCountsOutsideService = tcpEstablishedCountsOutsideService;
  }

  public double getTcpEstablishedFailCountsOutsideServiceRate() {
    return tcpEstablishedFailCountsOutsideServiceRate;
  }

  public void setTcpEstablishedFailCountsOutsideServiceRate(
      double tcpEstablishedFailCountsOutsideServiceRate) {
    this.tcpEstablishedFailCountsOutsideServiceRate = tcpEstablishedFailCountsOutsideServiceRate;
  }

  public double getTcpServerEstablishedFailCountsOutsideServiceRate() {
    return tcpServerEstablishedFailCountsOutsideServiceRate;
  }

  public void setTcpServerEstablishedFailCountsOutsideServiceRate(
      double tcpServerEstablishedFailCountsOutsideServiceRate) {
    this.tcpServerEstablishedFailCountsOutsideServiceRate = tcpServerEstablishedFailCountsOutsideServiceRate;
  }

  public double getTcpClientEstablishedFailCountsOutsideServiceRate() {
    return tcpClientEstablishedFailCountsOutsideServiceRate;
  }

  public void setTcpClientEstablishedFailCountsOutsideServiceRate(
      double tcpClientEstablishedFailCountsOutsideServiceRate) {
    this.tcpClientEstablishedFailCountsOutsideServiceRate = tcpClientEstablishedFailCountsOutsideServiceRate;
  }

  public double getTcpServerRecvRetransmissionPacketsRate() {
    return tcpServerRecvRetransmissionPacketsRate;
  }

  public void setTcpServerRecvRetransmissionPacketsRate(
      double tcpServerRecvRetransmissionPacketsRate) {
    this.tcpServerRecvRetransmissionPacketsRate = tcpServerRecvRetransmissionPacketsRate;
  }

  public double getTcpServerSendRetransmissionPacketsRate() {
    return tcpServerSendRetransmissionPacketsRate;
  }

  public void setTcpServerSendRetransmissionPacketsRate(
      double tcpServerSendRetransmissionPacketsRate) {
    this.tcpServerSendRetransmissionPacketsRate = tcpServerSendRetransmissionPacketsRate;
  }

  public double getTcpClientRecvRetransmissionPacketsRate() {
    return tcpClientRecvRetransmissionPacketsRate;
  }

  public void setTcpClientRecvRetransmissionPacketsRate(
      double tcpClientRecvRetransmissionPacketsRate) {
    this.tcpClientRecvRetransmissionPacketsRate = tcpClientRecvRetransmissionPacketsRate;
  }

  public double getTcpClientSendRetransmissionPacketsRate() {
    return tcpClientSendRetransmissionPacketsRate;
  }

  public void setTcpClientSendRetransmissionPacketsRate(
      double tcpClientSendRetransmissionPacketsRate) {
    this.tcpClientSendRetransmissionPacketsRate = tcpClientSendRetransmissionPacketsRate;
  }

  public double getTcpServerRecvRetransmissionPacketsInsideServiceRate() {
    return tcpServerRecvRetransmissionPacketsInsideServiceRate;
  }

  public void setTcpServerRecvRetransmissionPacketsInsideServiceRate(
      double tcpServerRecvRetransmissionPacketsInsideServiceRate) {
    this.tcpServerRecvRetransmissionPacketsInsideServiceRate = tcpServerRecvRetransmissionPacketsInsideServiceRate;
  }

  public double getTcpServerSendRetransmissionPacketsInsideServiceRate() {
    return tcpServerSendRetransmissionPacketsInsideServiceRate;
  }

  public void setTcpServerSendRetransmissionPacketsInsideServiceRate(
      double tcpServerSendRetransmissionPacketsInsideServiceRate) {
    this.tcpServerSendRetransmissionPacketsInsideServiceRate = tcpServerSendRetransmissionPacketsInsideServiceRate;
  }

  public double getTcpClientRecvRetransmissionPacketsInsideServiceRate() {
    return tcpClientRecvRetransmissionPacketsInsideServiceRate;
  }

  public void setTcpClientRecvRetransmissionPacketsInsideServiceRate(
      double tcpClientRecvRetransmissionPacketsInsideServiceRate) {
    this.tcpClientRecvRetransmissionPacketsInsideServiceRate = tcpClientRecvRetransmissionPacketsInsideServiceRate;
  }

  public double getTcpClientSendRetransmissionPacketsInsideServiceRate() {
    return tcpClientSendRetransmissionPacketsInsideServiceRate;
  }

  public void setTcpClientSendRetransmissionPacketsInsideServiceRate(
      double tcpClientSendRetransmissionPacketsInsideServiceRate) {
    this.tcpClientSendRetransmissionPacketsInsideServiceRate = tcpClientSendRetransmissionPacketsInsideServiceRate;
  }

  public double getTcpServerRecvRetransmissionPacketsOutsideServiceRate() {
    return tcpServerRecvRetransmissionPacketsOutsideServiceRate;
  }

  public void setTcpServerRecvRetransmissionPacketsOutsideServiceRate(
      double tcpServerRecvRetransmissionPacketsOutsideServiceRate) {
    this.tcpServerRecvRetransmissionPacketsOutsideServiceRate = tcpServerRecvRetransmissionPacketsOutsideServiceRate;
  }

  public double getTcpServerSendRetransmissionPacketsOutsideServiceRate() {
    return tcpServerSendRetransmissionPacketsOutsideServiceRate;
  }

  public void setTcpServerSendRetransmissionPacketsOutsideServiceRate(
      double tcpServerSendRetransmissionPacketsOutsideServiceRate) {
    this.tcpServerSendRetransmissionPacketsOutsideServiceRate = tcpServerSendRetransmissionPacketsOutsideServiceRate;
  }

  public double getTcpClientRecvRetransmissionPacketsOutsideServiceRate() {
    return tcpClientRecvRetransmissionPacketsOutsideServiceRate;
  }

  public void setTcpClientRecvRetransmissionPacketsOutsideServiceRate(
      double tcpClientRecvRetransmissionPacketsOutsideServiceRate) {
    this.tcpClientRecvRetransmissionPacketsOutsideServiceRate = tcpClientRecvRetransmissionPacketsOutsideServiceRate;
  }

  public double getTcpClientSendRetransmissionPacketsOutsideServiceRate() {
    return tcpClientSendRetransmissionPacketsOutsideServiceRate;
  }

  public void setTcpClientSendRetransmissionPacketsOutsideServiceRate(
      double tcpClientSendRetransmissionPacketsOutsideServiceRate) {
    this.tcpClientSendRetransmissionPacketsOutsideServiceRate = tcpClientSendRetransmissionPacketsOutsideServiceRate;
  }
}
