package com.machloop.fpc.manager.metric.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.machloop.fpc.manager.global.data.AbstractDataRecordDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public class MetricServiceDataRecordDO extends AbstractDataRecordDO {

  // 流量数据包统计
  private long bytepsPeak;
  private long packetpsPeak;
  private long downstreamBytes;
  private long downstreamPackets;
  private long upstreamBytes;
  private long upstreamPackets;
  private long filterDiscardBytes;
  private long filterDiscardPackets;
  private long overloadDiscardBytes;
  private long overloadDiscardPackets;
  private long deduplicationBytes;
  private long deduplicationPackets;
  // 分片数据包统计
  private long fragmentTotalBytes;
  private long fragmentTotalPackets;
  // TCP相关统计
  private long tcpSynPackets;
  private long tcpClientSynPackets;
  private long tcpServerSynPackets;
  private long tcpSynAckPackets;
  private long tcpSynRstPackets;
  private long tcpEstablishedTimeAvg;
  private long tcpZeroWindowPackets;
  // 会话统计
  private long activeSessions;
  private long concurrentSessions;
  private long concurrentTcpSessions;
  private long concurrentUdpSessions;
  private long concurrentArpSessions;
  private long concurrentIcmpSessions;
  private long destroyedSessions;
  private long establishedTcpSessions;
  private long establishedUdpSessions;
  private long establishedIcmpSessions;
  private long establishedOtherSessions;
  private long establishedUpstreamSessions;
  private long establishedDownstreamSessions;
  // 性能统计
  private long serverResponseFastCounts;
  private long serverResponseNormalCounts;
  private long serverResponseTimeoutCounts;
  private long serverResponseLatencyPeak;
  // 独立用户数
  private long uniqueIpCounts;


  // Performance和TCP内网与外网相关字段
  private long serverResponseLatencyInsideService;
  private long serverResponseLatencyCountsInsideService;
  private long serverResponseLatencyPeakInsideService;
  private long serverResponseFastCountsInsideService;
  private long serverResponseNormalCountsInsideService;
  private long serverResponseTimeoutCountsInsideService;
  private long tcpClientNetworkLatencyInsideService;
  private long tcpClientNetworkLatencyCountsInsideService;
  private long tcpServerNetworkLatencyInsideService;
  private long tcpServerNetworkLatencyCountsInsideService;
  private long tcpEstablishedSuccessCountsInsideService;
  private long tcpEstablishedFailCountsInsideService;
  private long tcpClientSynPacketsInsideService;
  private long tcpServerSynPacketsInsideService;
  private long tcpClientRetransmissionPacketsInsideService;
  private long tcpClientPacketsInsideService;
  private long tcpServerRetransmissionPacketsInsideService;
  private long tcpServerPacketsInsideService;
  private long tcpClientZeroWindowPacketsInsideService;
  private long tcpServerZeroWindowPacketsInsideService;


  private long serverResponseLatencyOutsideService;
  private long serverResponseLatencyCountsOutsideService;
  private long serverResponseLatencyPeakOutsideService;
  private long serverResponseFastCountsOutsideService;
  private long serverResponseNormalCountsOutsideService;
  private long serverResponseTimeoutCountsOutsideService;
  private long tcpClientNetworkLatencyOutsideService;
  private long tcpClientNetworkLatencyCountsOutsideService;
  private long tcpServerNetworkLatencyOutsideService;
  private long tcpServerNetworkLatencyCountsOutsideService;
  private long tcpEstablishedSuccessCountsOutsideService;
  private long tcpEstablishedFailCountsOutsideService;
  private long tcpClientSynPacketsOutsideService;
  private long tcpServerSynPacketsOutsideService;
  private long tcpClientRetransmissionPacketsOutsideService;
  private long tcpClientPacketsOutsideService;
  private long tcpServerRetransmissionPacketsOutsideService;
  private long tcpServerPacketsOutsideService;
  private long tcpClientZeroWindowPacketsOutsideService;
  private long tcpServerZeroWindowPacketsOutsideService;

  // 计算数据
  // Performance和TCP指标
  @JsonIgnore private double tcpClientRetransmissionRateInsideService;
  @JsonIgnore private double tcpServerRetransmissionRateInsideService;
  @JsonIgnore private double tcpClientNetworkLatencyAvgInsideService;
  @JsonIgnore private double tcpServerNetworkLatencyAvgInsideService;
  @JsonIgnore private double serverResponseLatencyAvgInsideService;

  @JsonIgnore private double tcpClientRetransmissionRateOutsideService;
  @JsonIgnore private double tcpServerRetransmissionRateOutsideService;
  @JsonIgnore private double tcpClientNetworkLatencyAvgOutsideService;
  @JsonIgnore private double tcpServerNetworkLatencyAvgOutsideService;
  @JsonIgnore private double serverResponseLatencyAvgOutsideService;

  @Override public String toString() {
    return "MetricServiceDataRecordDO{" + "bytepsPeak=" + bytepsPeak + ", packetpsPeak="
        + packetpsPeak + ", downstreamBytes=" + downstreamBytes + ", downstreamPackets="
        + downstreamPackets + ", upstreamBytes=" + upstreamBytes + ", upstreamPackets="
        + upstreamPackets + ", filterDiscardBytes=" + filterDiscardBytes + ", filterDiscardPackets="
        + filterDiscardPackets + ", overloadDiscardBytes=" + overloadDiscardBytes
        + ", overloadDiscardPackets=" + overloadDiscardPackets + ", deduplicationBytes="
        + deduplicationBytes + ", deduplicationPackets=" + deduplicationPackets
        + ", fragmentTotalBytes=" + fragmentTotalBytes + ", fragmentTotalPackets="
        + fragmentTotalPackets + ", tcpSynPackets=" + tcpSynPackets + ", tcpClientSynPackets="
        + tcpClientSynPackets + ", tcpServerSynPackets=" + tcpServerSynPackets
        + ", tcpSynAckPackets=" + tcpSynAckPackets + ", tcpSynRstPackets=" + tcpSynRstPackets
        + ", tcpEstablishedTimeAvg=" + tcpEstablishedTimeAvg + ", tcpZeroWindowPackets="
        + tcpZeroWindowPackets + ", activeSessions=" + activeSessions + ", concurrentSessions="
        + concurrentSessions + ", concurrentTcpSessions=" + concurrentTcpSessions
        + ", concurrentUdpSessions=" + concurrentUdpSessions + ", concurrentArpSessions="
        + concurrentArpSessions + ", concurrentIcmpSessions=" + concurrentIcmpSessions
        + ", destroyedSessions=" + destroyedSessions + ", establishedTcpSessions="
        + establishedTcpSessions + ", establishedUdpSessions=" + establishedUdpSessions
        + ", establishedIcmpSessions=" + establishedIcmpSessions + ", establishedOtherSessions="
        + establishedOtherSessions + ", establishedUpstreamSessions=" + establishedUpstreamSessions
        + ", establishedDownstreamSessions=" + establishedDownstreamSessions
        + ", serverResponseFastCounts=" + serverResponseFastCounts + ", serverResponseNormalCounts="
        + serverResponseNormalCounts + ", serverResponseTimeoutCounts="
        + serverResponseTimeoutCounts + ", serverResponseLatencyPeak=" + serverResponseLatencyPeak
        + ", uniqueIpCounts=" + uniqueIpCounts + ", serverResponseLatencyInsideService="
        + serverResponseLatencyInsideService + ", serverResponseLatencyCountsInsideService="
        + serverResponseLatencyCountsInsideService + ", serverResponseLatencyPeakInsideService="
        + serverResponseLatencyPeakInsideService + ", serverResponseFastCountsInsideService="
        + serverResponseFastCountsInsideService + ", serverResponseNormalCountsInsideService="
        + serverResponseNormalCountsInsideService + ", serverResponseTimeoutCountsInsideService="
        + serverResponseTimeoutCountsInsideService + ", tcpClientNetworkLatencyInsideService="
        + tcpClientNetworkLatencyInsideService + ", tcpClientNetworkLatencyCountsInsideService="
        + tcpClientNetworkLatencyCountsInsideService + ", tcpServerNetworkLatencyInsideService="
        + tcpServerNetworkLatencyInsideService + ", tcpServerNetworkLatencyCountsInsideService="
        + tcpServerNetworkLatencyCountsInsideService + ", tcpEstablishedSuccessCountsInsideService="
        + tcpEstablishedSuccessCountsInsideService + ", tcpEstablishedFailCountsInsideService="
        + tcpEstablishedFailCountsInsideService + ", tcpClientSynPacketsInsideService="
        + tcpClientSynPacketsInsideService + ", tcpServerSynPacketsInsideService="
        + tcpServerSynPacketsInsideService + ", tcpClientRetransmissionPacketsInsideService="
        + tcpClientRetransmissionPacketsInsideService + ", tcpClientPacketsInsideService="
        + tcpClientPacketsInsideService + ", tcpServerRetransmissionPacketsInsideService="
        + tcpServerRetransmissionPacketsInsideService + ", tcpServerPacketsInsideService="
        + tcpServerPacketsInsideService + ", tcpClientZeroWindowPacketsInsideService="
        + tcpClientZeroWindowPacketsInsideService + ", tcpServerZeroWindowPacketsInsideService="
        + tcpServerZeroWindowPacketsInsideService + ", serverResponseLatencyOutsideService="
        + serverResponseLatencyOutsideService + ", serverResponseLatencyCountsOutsideService="
        + serverResponseLatencyCountsOutsideService + ", serverResponseLatencyPeakOutsideService="
        + serverResponseLatencyPeakOutsideService + ", serverResponseFastCountsOutsideService="
        + serverResponseFastCountsOutsideService + ", serverResponseNormalCountsOutsideService="
        + serverResponseNormalCountsOutsideService + ", serverResponseTimeoutCountsOutsideService="
        + serverResponseTimeoutCountsOutsideService + ", tcpClientNetworkLatencyOutsideService="
        + tcpClientNetworkLatencyOutsideService + ", tcpClientNetworkLatencyCountsOutsideService="
        + tcpClientNetworkLatencyCountsOutsideService + ", tcpServerNetworkLatencyOutsideService="
        + tcpServerNetworkLatencyOutsideService + ", tcpServerNetworkLatencyCountsOutsideService="
        + tcpServerNetworkLatencyCountsOutsideService
        + ", tcpEstablishedSuccessCountsOutsideService=" + tcpEstablishedSuccessCountsOutsideService
        + ", tcpEstablishedFailCountsOutsideService=" + tcpEstablishedFailCountsOutsideService
        + ", tcpClientSynPacketsOutsideService=" + tcpClientSynPacketsOutsideService
        + ", tcpServerSynPacketsOutsideService=" + tcpServerSynPacketsOutsideService
        + ", tcpClientRetransmissionPacketsOutsideService="
        + tcpClientRetransmissionPacketsOutsideService + ", tcpClientPacketsOutsideService="
        + tcpClientPacketsOutsideService + ", tcpServerRetransmissionPacketsOutsideService="
        + tcpServerRetransmissionPacketsOutsideService + ", tcpServerPacketsOutsideService="
        + tcpServerPacketsOutsideService + ", tcpClientZeroWindowPacketsOutsideService="
        + tcpClientZeroWindowPacketsOutsideService + ", tcpServerZeroWindowPacketsOutsideService="
        + tcpServerZeroWindowPacketsOutsideService + ", tcpClientRetransmissionRateInsideService="
        + tcpClientRetransmissionRateInsideService + ", tcpServerRetransmissionRateInsideService="
        + tcpServerRetransmissionRateInsideService + ", tcpClientNetworkLatencyAvgInsideService="
        + tcpClientNetworkLatencyAvgInsideService + ", tcpServerNetworkLatencyAvgInsideService="
        + tcpServerNetworkLatencyAvgInsideService + ", serverResponseLatencyAvgInsideService="
        + serverResponseLatencyAvgInsideService + ", tcpClientRetransmissionRateOutsideService="
        + tcpClientRetransmissionRateOutsideService + ", tcpServerRetransmissionRateOutsideService="
        + tcpServerRetransmissionRateOutsideService + ", tcpClientNetworkLatencyAvgOutsideService="
        + tcpClientNetworkLatencyAvgOutsideService + ", tcpServerNetworkLatencyAvgOutsideService="
        + tcpServerNetworkLatencyAvgOutsideService + ", serverResponseLatencyAvgOutsideService="
        + serverResponseLatencyAvgOutsideService + '}';
  }

  public long getBytepsPeak() {
    return bytepsPeak;
  }

  public void setBytepsPeak(long bytepsPeak) {
    this.bytepsPeak = bytepsPeak;
  }

  public long getPacketpsPeak() {
    return packetpsPeak;
  }

  public void setPacketpsPeak(long packetpsPeak) {
    this.packetpsPeak = packetpsPeak;
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

  public long getFilterDiscardBytes() {
    return filterDiscardBytes;
  }

  public void setFilterDiscardBytes(long filterDiscardBytes) {
    this.filterDiscardBytes = filterDiscardBytes;
  }

  public long getFilterDiscardPackets() {
    return filterDiscardPackets;
  }

  public void setFilterDiscardPackets(long filterDiscardPackets) {
    this.filterDiscardPackets = filterDiscardPackets;
  }

  public long getOverloadDiscardBytes() {
    return overloadDiscardBytes;
  }

  public void setOverloadDiscardBytes(long overloadDiscardBytes) {
    this.overloadDiscardBytes = overloadDiscardBytes;
  }

  public long getOverloadDiscardPackets() {
    return overloadDiscardPackets;
  }

  public void setOverloadDiscardPackets(long overloadDiscardPackets) {
    this.overloadDiscardPackets = overloadDiscardPackets;
  }

  public long getDeduplicationBytes() {
    return deduplicationBytes;
  }

  public void setDeduplicationBytes(long deduplicationBytes) {
    this.deduplicationBytes = deduplicationBytes;
  }

  public long getDeduplicationPackets() {
    return deduplicationPackets;
  }

  public void setDeduplicationPackets(long deduplicationPackets) {
    this.deduplicationPackets = deduplicationPackets;
  }

  public long getFragmentTotalBytes() {
    return fragmentTotalBytes;
  }

  public void setFragmentTotalBytes(long fragmentTotalBytes) {
    this.fragmentTotalBytes = fragmentTotalBytes;
  }

  public long getFragmentTotalPackets() {
    return fragmentTotalPackets;
  }

  public void setFragmentTotalPackets(long fragmentTotalPackets) {
    this.fragmentTotalPackets = fragmentTotalPackets;
  }

  public long getTcpSynPackets() {
    return tcpSynPackets;
  }

  public void setTcpSynPackets(long tcpSynPackets) {
    this.tcpSynPackets = tcpSynPackets;
  }

  public long getTcpClientSynPackets() {
    return tcpClientSynPackets;
  }

  public void setTcpClientSynPackets(long tcpClientSynPackets) {
    this.tcpClientSynPackets = tcpClientSynPackets;
  }

  public long getTcpServerSynPackets() {
    return tcpServerSynPackets;
  }

  public void setTcpServerSynPackets(long tcpServerSynPackets) {
    this.tcpServerSynPackets = tcpServerSynPackets;
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

  public long getTcpEstablishedTimeAvg() {
    return tcpEstablishedTimeAvg;
  }

  public void setTcpEstablishedTimeAvg(long tcpEstablishedTimeAvg) {
    this.tcpEstablishedTimeAvg = tcpEstablishedTimeAvg;
  }

  public long getTcpZeroWindowPackets() {
    return tcpZeroWindowPackets;
  }

  public void setTcpZeroWindowPackets(long tcpZeroWindowPackets) {
    this.tcpZeroWindowPackets = tcpZeroWindowPackets;
  }

  public long getActiveSessions() {
    return activeSessions;
  }

  public void setActiveSessions(long activeSessions) {
    this.activeSessions = activeSessions;
  }

  public long getConcurrentSessions() {
    return concurrentSessions;
  }

  public void setConcurrentSessions(long concurrentSessions) {
    this.concurrentSessions = concurrentSessions;
  }

  public long getConcurrentTcpSessions() {
    return concurrentTcpSessions;
  }

  public void setConcurrentTcpSessions(long concurrentTcpSessions) {
    this.concurrentTcpSessions = concurrentTcpSessions;
  }

  public long getConcurrentUdpSessions() {
    return concurrentUdpSessions;
  }

  public void setConcurrentUdpSessions(long concurrentUdpSessions) {
    this.concurrentUdpSessions = concurrentUdpSessions;
  }

  public long getConcurrentArpSessions() {
    return concurrentArpSessions;
  }

  public void setConcurrentArpSessions(long concurrentArpSessions) {
    this.concurrentArpSessions = concurrentArpSessions;
  }

  public long getConcurrentIcmpSessions() {
    return concurrentIcmpSessions;
  }

  public void setConcurrentIcmpSessions(long concurrentIcmpSessions) {
    this.concurrentIcmpSessions = concurrentIcmpSessions;
  }

  public long getDestroyedSessions() {
    return destroyedSessions;
  }

  public void setDestroyedSessions(long destroyedSessions) {
    this.destroyedSessions = destroyedSessions;
  }

  public long getEstablishedTcpSessions() {
    return establishedTcpSessions;
  }

  public void setEstablishedTcpSessions(long establishedTcpSessions) {
    this.establishedTcpSessions = establishedTcpSessions;
  }

  public long getEstablishedUdpSessions() {
    return establishedUdpSessions;
  }

  public void setEstablishedUdpSessions(long establishedUdpSessions) {
    this.establishedUdpSessions = establishedUdpSessions;
  }

  public long getEstablishedIcmpSessions() {
    return establishedIcmpSessions;
  }

  public void setEstablishedIcmpSessions(long establishedIcmpSessions) {
    this.establishedIcmpSessions = establishedIcmpSessions;
  }

  public long getEstablishedOtherSessions() {
    return establishedOtherSessions;
  }

  public void setEstablishedOtherSessions(long establishedOtherSessions) {
    this.establishedOtherSessions = establishedOtherSessions;
  }

  public long getEstablishedUpstreamSessions() {
    return establishedUpstreamSessions;
  }

  public void setEstablishedUpstreamSessions(long establishedUpstreamSessions) {
    this.establishedUpstreamSessions = establishedUpstreamSessions;
  }

  public long getEstablishedDownstreamSessions() {
    return establishedDownstreamSessions;
  }

  public void setEstablishedDownstreamSessions(long establishedDownstreamSessions) {
    this.establishedDownstreamSessions = establishedDownstreamSessions;
  }

  public long getServerResponseFastCounts() {
    return serverResponseFastCounts;
  }

  public void setServerResponseFastCounts(long serverResponseFastCounts) {
    this.serverResponseFastCounts = serverResponseFastCounts;
  }

  public long getServerResponseNormalCounts() {
    return serverResponseNormalCounts;
  }

  public void setServerResponseNormalCounts(long serverResponseNormalCounts) {
    this.serverResponseNormalCounts = serverResponseNormalCounts;
  }

  public long getServerResponseTimeoutCounts() {
    return serverResponseTimeoutCounts;
  }

  public void setServerResponseTimeoutCounts(long serverResponseTimeoutCounts) {
    this.serverResponseTimeoutCounts = serverResponseTimeoutCounts;
  }

  public long getServerResponseLatencyPeak() {
    return serverResponseLatencyPeak;
  }

  public void setServerResponseLatencyPeak(long serverResponseLatencyPeak) {
    this.serverResponseLatencyPeak = serverResponseLatencyPeak;
  }

  public long getUniqueIpCounts() {
    return uniqueIpCounts;
  }

  public void setUniqueIpCounts(long uniqueIpCounts) {
    this.uniqueIpCounts = uniqueIpCounts;
  }

  public long getServerResponseLatencyInsideService() {
    return serverResponseLatencyInsideService;
  }

  public void setServerResponseLatencyInsideService(long serverResponseLatencyInsideService) {
    this.serverResponseLatencyInsideService = serverResponseLatencyInsideService;
  }

  public long getServerResponseLatencyCountsInsideService() {
    return serverResponseLatencyCountsInsideService;
  }

  public void setServerResponseLatencyCountsInsideService(
      long serverResponseLatencyCountsInsideService) {
    this.serverResponseLatencyCountsInsideService = serverResponseLatencyCountsInsideService;
  }

  public long getServerResponseLatencyPeakInsideService() {
    return serverResponseLatencyPeakInsideService;
  }

  public void setServerResponseLatencyPeakInsideService(
      long serverResponseLatencyPeakInsideService) {
    this.serverResponseLatencyPeakInsideService = serverResponseLatencyPeakInsideService;
  }

  public long getServerResponseFastCountsInsideService() {
    return serverResponseFastCountsInsideService;
  }

  public void setServerResponseFastCountsInsideService(long serverResponseFastCountsInsideService) {
    this.serverResponseFastCountsInsideService = serverResponseFastCountsInsideService;
  }

  public long getServerResponseNormalCountsInsideService() {
    return serverResponseNormalCountsInsideService;
  }

  public void setServerResponseNormalCountsInsideService(
      long serverResponseNormalCountsInsideService) {
    this.serverResponseNormalCountsInsideService = serverResponseNormalCountsInsideService;
  }

  public long getServerResponseTimeoutCountsInsideService() {
    return serverResponseTimeoutCountsInsideService;
  }

  public void setServerResponseTimeoutCountsInsideService(
      long serverResponseTimeoutCountsInsideService) {
    this.serverResponseTimeoutCountsInsideService = serverResponseTimeoutCountsInsideService;
  }

  public long getTcpClientNetworkLatencyInsideService() {
    return tcpClientNetworkLatencyInsideService;
  }

  public void setTcpClientNetworkLatencyInsideService(long tcpClientNetworkLatencyInsideService) {
    this.tcpClientNetworkLatencyInsideService = tcpClientNetworkLatencyInsideService;
  }

  public long getTcpClientNetworkLatencyCountsInsideService() {
    return tcpClientNetworkLatencyCountsInsideService;
  }

  public void setTcpClientNetworkLatencyCountsInsideService(
      long tcpClientNetworkLatencyCountsInsideService) {
    this.tcpClientNetworkLatencyCountsInsideService = tcpClientNetworkLatencyCountsInsideService;
  }

  public long getTcpServerNetworkLatencyInsideService() {
    return tcpServerNetworkLatencyInsideService;
  }

  public void setTcpServerNetworkLatencyInsideService(long tcpServerNetworkLatencyInsideService) {
    this.tcpServerNetworkLatencyInsideService = tcpServerNetworkLatencyInsideService;
  }

  public long getTcpServerNetworkLatencyCountsInsideService() {
    return tcpServerNetworkLatencyCountsInsideService;
  }

  public void setTcpServerNetworkLatencyCountsInsideService(
      long tcpServerNetworkLatencyCountsInsideService) {
    this.tcpServerNetworkLatencyCountsInsideService = tcpServerNetworkLatencyCountsInsideService;
  }

  public long getTcpEstablishedSuccessCountsInsideService() {
    return tcpEstablishedSuccessCountsInsideService;
  }

  public void setTcpEstablishedSuccessCountsInsideService(
      long tcpEstablishedSuccessCountsInsideService) {
    this.tcpEstablishedSuccessCountsInsideService = tcpEstablishedSuccessCountsInsideService;
  }

  public long getTcpEstablishedFailCountsInsideService() {
    return tcpEstablishedFailCountsInsideService;
  }

  public void setTcpEstablishedFailCountsInsideService(long tcpEstablishedFailCountsInsideService) {
    this.tcpEstablishedFailCountsInsideService = tcpEstablishedFailCountsInsideService;
  }

  public long getTcpClientSynPacketsInsideService() {
    return tcpClientSynPacketsInsideService;
  }

  public void setTcpClientSynPacketsInsideService(long tcpClientSynPacketsInsideService) {
    this.tcpClientSynPacketsInsideService = tcpClientSynPacketsInsideService;
  }

  public long getTcpServerSynPacketsInsideService() {
    return tcpServerSynPacketsInsideService;
  }

  public void setTcpServerSynPacketsInsideService(long tcpServerSynPacketsInsideService) {
    this.tcpServerSynPacketsInsideService = tcpServerSynPacketsInsideService;
  }

  public long getTcpClientRetransmissionPacketsInsideService() {
    return tcpClientRetransmissionPacketsInsideService;
  }

  public void setTcpClientRetransmissionPacketsInsideService(
      long tcpClientRetransmissionPacketsInsideService) {
    this.tcpClientRetransmissionPacketsInsideService = tcpClientRetransmissionPacketsInsideService;
  }

  public long getTcpClientPacketsInsideService() {
    return tcpClientPacketsInsideService;
  }

  public void setTcpClientPacketsInsideService(long tcpClientPacketsInsideService) {
    this.tcpClientPacketsInsideService = tcpClientPacketsInsideService;
  }

  public long getTcpServerRetransmissionPacketsInsideService() {
    return tcpServerRetransmissionPacketsInsideService;
  }

  public void setTcpServerRetransmissionPacketsInsideService(
      long tcpServerRetransmissionPacketsInsideService) {
    this.tcpServerRetransmissionPacketsInsideService = tcpServerRetransmissionPacketsInsideService;
  }

  public long getTcpServerPacketsInsideService() {
    return tcpServerPacketsInsideService;
  }

  public void setTcpServerPacketsInsideService(long tcpServerPacketsInsideService) {
    this.tcpServerPacketsInsideService = tcpServerPacketsInsideService;
  }

  public long getTcpClientZeroWindowPacketsInsideService() {
    return tcpClientZeroWindowPacketsInsideService;
  }

  public void setTcpClientZeroWindowPacketsInsideService(
      long tcpClientZeroWindowPacketsInsideService) {
    this.tcpClientZeroWindowPacketsInsideService = tcpClientZeroWindowPacketsInsideService;
  }

  public long getTcpServerZeroWindowPacketsInsideService() {
    return tcpServerZeroWindowPacketsInsideService;
  }

  public void setTcpServerZeroWindowPacketsInsideService(
      long tcpServerZeroWindowPacketsInsideService) {
    this.tcpServerZeroWindowPacketsInsideService = tcpServerZeroWindowPacketsInsideService;
  }

  public long getServerResponseLatencyOutsideService() {
    return serverResponseLatencyOutsideService;
  }

  public void setServerResponseLatencyOutsideService(long serverResponseLatencyOutsideService) {
    this.serverResponseLatencyOutsideService = serverResponseLatencyOutsideService;
  }

  public long getServerResponseLatencyCountsOutsideService() {
    return serverResponseLatencyCountsOutsideService;
  }

  public void setServerResponseLatencyCountsOutsideService(
      long serverResponseLatencyCountsOutsideService) {
    this.serverResponseLatencyCountsOutsideService = serverResponseLatencyCountsOutsideService;
  }

  public long getServerResponseLatencyPeakOutsideService() {
    return serverResponseLatencyPeakOutsideService;
  }

  public void setServerResponseLatencyPeakOutsideService(
      long serverResponseLatencyPeakOutsideService) {
    this.serverResponseLatencyPeakOutsideService = serverResponseLatencyPeakOutsideService;
  }

  public long getServerResponseFastCountsOutsideService() {
    return serverResponseFastCountsOutsideService;
  }

  public void setServerResponseFastCountsOutsideService(
      long serverResponseFastCountsOutsideService) {
    this.serverResponseFastCountsOutsideService = serverResponseFastCountsOutsideService;
  }

  public long getServerResponseNormalCountsOutsideService() {
    return serverResponseNormalCountsOutsideService;
  }

  public void setServerResponseNormalCountsOutsideService(
      long serverResponseNormalCountsOutsideService) {
    this.serverResponseNormalCountsOutsideService = serverResponseNormalCountsOutsideService;
  }

  public long getServerResponseTimeoutCountsOutsideService() {
    return serverResponseTimeoutCountsOutsideService;
  }

  public void setServerResponseTimeoutCountsOutsideService(
      long serverResponseTimeoutCountsOutsideService) {
    this.serverResponseTimeoutCountsOutsideService = serverResponseTimeoutCountsOutsideService;
  }

  public long getTcpClientNetworkLatencyOutsideService() {
    return tcpClientNetworkLatencyOutsideService;
  }

  public void setTcpClientNetworkLatencyOutsideService(long tcpClientNetworkLatencyOutsideService) {
    this.tcpClientNetworkLatencyOutsideService = tcpClientNetworkLatencyOutsideService;
  }

  public long getTcpClientNetworkLatencyCountsOutsideService() {
    return tcpClientNetworkLatencyCountsOutsideService;
  }

  public void setTcpClientNetworkLatencyCountsOutsideService(
      long tcpClientNetworkLatencyCountsOutsideService) {
    this.tcpClientNetworkLatencyCountsOutsideService = tcpClientNetworkLatencyCountsOutsideService;
  }

  public long getTcpServerNetworkLatencyOutsideService() {
    return tcpServerNetworkLatencyOutsideService;
  }

  public void setTcpServerNetworkLatencyOutsideService(long tcpServerNetworkLatencyOutsideService) {
    this.tcpServerNetworkLatencyOutsideService = tcpServerNetworkLatencyOutsideService;
  }

  public long getTcpServerNetworkLatencyCountsOutsideService() {
    return tcpServerNetworkLatencyCountsOutsideService;
  }

  public void setTcpServerNetworkLatencyCountsOutsideService(
      long tcpServerNetworkLatencyCountsOutsideService) {
    this.tcpServerNetworkLatencyCountsOutsideService = tcpServerNetworkLatencyCountsOutsideService;
  }

  public long getTcpEstablishedSuccessCountsOutsideService() {
    return tcpEstablishedSuccessCountsOutsideService;
  }

  public void setTcpEstablishedSuccessCountsOutsideService(
      long tcpEstablishedSuccessCountsOutsideService) {
    this.tcpEstablishedSuccessCountsOutsideService = tcpEstablishedSuccessCountsOutsideService;
  }

  public long getTcpEstablishedFailCountsOutsideService() {
    return tcpEstablishedFailCountsOutsideService;
  }

  public void setTcpEstablishedFailCountsOutsideService(
      long tcpEstablishedFailCountsOutsideService) {
    this.tcpEstablishedFailCountsOutsideService = tcpEstablishedFailCountsOutsideService;
  }

  public long getTcpClientSynPacketsOutsideService() {
    return tcpClientSynPacketsOutsideService;
  }

  public void setTcpClientSynPacketsOutsideService(long tcpClientSynPacketsOutsideService) {
    this.tcpClientSynPacketsOutsideService = tcpClientSynPacketsOutsideService;
  }

  public long getTcpServerSynPacketsOutsideService() {
    return tcpServerSynPacketsOutsideService;
  }

  public void setTcpServerSynPacketsOutsideService(long tcpServerSynPacketsOutsideService) {
    this.tcpServerSynPacketsOutsideService = tcpServerSynPacketsOutsideService;
  }

  public long getTcpClientRetransmissionPacketsOutsideService() {
    return tcpClientRetransmissionPacketsOutsideService;
  }

  public void setTcpClientRetransmissionPacketsOutsideService(
      long tcpClientRetransmissionPacketsOutsideService) {
    this.tcpClientRetransmissionPacketsOutsideService = tcpClientRetransmissionPacketsOutsideService;
  }

  public long getTcpClientPacketsOutsideService() {
    return tcpClientPacketsOutsideService;
  }

  public void setTcpClientPacketsOutsideService(long tcpClientPacketsOutsideService) {
    this.tcpClientPacketsOutsideService = tcpClientPacketsOutsideService;
  }

  public long getTcpServerRetransmissionPacketsOutsideService() {
    return tcpServerRetransmissionPacketsOutsideService;
  }

  public void setTcpServerRetransmissionPacketsOutsideService(
      long tcpServerRetransmissionPacketsOutsideService) {
    this.tcpServerRetransmissionPacketsOutsideService = tcpServerRetransmissionPacketsOutsideService;
  }

  public long getTcpServerPacketsOutsideService() {
    return tcpServerPacketsOutsideService;
  }

  public void setTcpServerPacketsOutsideService(long tcpServerPacketsOutsideService) {
    this.tcpServerPacketsOutsideService = tcpServerPacketsOutsideService;
  }

  public long getTcpClientZeroWindowPacketsOutsideService() {
    return tcpClientZeroWindowPacketsOutsideService;
  }

  public void setTcpClientZeroWindowPacketsOutsideService(
      long tcpClientZeroWindowPacketsOutsideService) {
    this.tcpClientZeroWindowPacketsOutsideService = tcpClientZeroWindowPacketsOutsideService;
  }

  public long getTcpServerZeroWindowPacketsOutsideService() {
    return tcpServerZeroWindowPacketsOutsideService;
  }

  public void setTcpServerZeroWindowPacketsOutsideService(
      long tcpServerZeroWindowPacketsOutsideService) {
    this.tcpServerZeroWindowPacketsOutsideService = tcpServerZeroWindowPacketsOutsideService;
  }

  public double getTcpClientRetransmissionRateInsideService() {
    return tcpClientRetransmissionRateInsideService;
  }

  public void setTcpClientRetransmissionRateInsideService(
      double tcpClientRetransmissionRateInsideService) {
    this.tcpClientRetransmissionRateInsideService = tcpClientRetransmissionRateInsideService;
  }

  public double getTcpServerRetransmissionRateInsideService() {
    return tcpServerRetransmissionRateInsideService;
  }

  public void setTcpServerRetransmissionRateInsideService(
      double tcpServerRetransmissionRateInsideService) {
    this.tcpServerRetransmissionRateInsideService = tcpServerRetransmissionRateInsideService;
  }

  public double getTcpClientNetworkLatencyAvgInsideService() {
    return tcpClientNetworkLatencyAvgInsideService;
  }

  public void setTcpClientNetworkLatencyAvgInsideService(
      double tcpClientNetworkLatencyAvgInsideService) {
    this.tcpClientNetworkLatencyAvgInsideService = tcpClientNetworkLatencyAvgInsideService;
  }

  public double getTcpServerNetworkLatencyAvgInsideService() {
    return tcpServerNetworkLatencyAvgInsideService;
  }

  public void setTcpServerNetworkLatencyAvgInsideService(
      double tcpServerNetworkLatencyAvgInsideService) {
    this.tcpServerNetworkLatencyAvgInsideService = tcpServerNetworkLatencyAvgInsideService;
  }

  public double getServerResponseLatencyAvgInsideService() {
    return serverResponseLatencyAvgInsideService;
  }

  public void setServerResponseLatencyAvgInsideService(
      double serverResponseLatencyAvgInsideService) {
    this.serverResponseLatencyAvgInsideService = serverResponseLatencyAvgInsideService;
  }

  public double getTcpClientRetransmissionRateOutsideService() {
    return tcpClientRetransmissionRateOutsideService;
  }

  public void setTcpClientRetransmissionRateOutsideService(
      double tcpClientRetransmissionRateOutsideService) {
    this.tcpClientRetransmissionRateOutsideService = tcpClientRetransmissionRateOutsideService;
  }

  public double getTcpServerRetransmissionRateOutsideService() {
    return tcpServerRetransmissionRateOutsideService;
  }

  public void setTcpServerRetransmissionRateOutsideService(
      double tcpServerRetransmissionRateOutsideService) {
    this.tcpServerRetransmissionRateOutsideService = tcpServerRetransmissionRateOutsideService;
  }

  public double getTcpClientNetworkLatencyAvgOutsideService() {
    return tcpClientNetworkLatencyAvgOutsideService;
  }

  public void setTcpClientNetworkLatencyAvgOutsideService(
      double tcpClientNetworkLatencyAvgOutsideService) {
    this.tcpClientNetworkLatencyAvgOutsideService = tcpClientNetworkLatencyAvgOutsideService;
  }

  public double getTcpServerNetworkLatencyAvgOutsideService() {
    return tcpServerNetworkLatencyAvgOutsideService;
  }

  public void setTcpServerNetworkLatencyAvgOutsideService(
      double tcpServerNetworkLatencyAvgOutsideService) {
    this.tcpServerNetworkLatencyAvgOutsideService = tcpServerNetworkLatencyAvgOutsideService;
  }

  public double getServerResponseLatencyAvgOutsideService() {
    return serverResponseLatencyAvgOutsideService;
  }

  public void setServerResponseLatencyAvgOutsideService(
      double serverResponseLatencyAvgOutsideService) {
    this.serverResponseLatencyAvgOutsideService = serverResponseLatencyAvgOutsideService;
  }
}
