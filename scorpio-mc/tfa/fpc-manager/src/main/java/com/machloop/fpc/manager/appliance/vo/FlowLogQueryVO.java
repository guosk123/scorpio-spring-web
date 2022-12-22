package com.machloop.fpc.manager.appliance.vo;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

public class FlowLogQueryVO implements LogAudit {
  private String id;
  private String interfaceName;
  private Long flowId;

  private String timestamp;
  private String networkId;
  private String serviceId;
  private String startTime;
  private String endTime;
  private Date startTimeDate;
  private Date endTimeDate;
  private Boolean flowContinued;
  private String packetSigseq;
  private Integer ethernetType;
  private String ethernetInitiator;
  private String ethernetResponder;
  private String ethernetProtocol;
  private Integer vlanId;
  private Integer ipLocalityInitiator;
  private Integer ipLocalityResponder;
  private String ipInitiator;
  private String ipResponder;
  private String ipProtocol;
  private String portInitiator;
  private String portResponder;
  private String l7ProtocolId;
  private String applicationIds;
  private String maliciousApplicationIds;
  private String countryIdInitiator;
  private String provinceIdInitiator;
  private String cityIdInitiator;
  private String countryIdResponder;
  private String provinceIdResponder;
  private String cityIdResponder;
  private String locationInitiator;
  private String locationResponder;

  private String reportTime;
  private String duration;
  private String upstreamBytes;
  private String downstreamBytes;
  private String totalBytes;
  private String upstreamPackets;
  private String downstreamPackets;
  private String totalPackets;
  private String upstreamPayloadBytes;
  private String downstreamPayloadBytes;
  private String totalPayloadBytes;
  private String upstreamPayloadPackets;
  private String downstreamPayloadPackets;
  private String totalPayloadPackets;
  private String tcpClientNetworkLatency;
  private String tcpServerNetworkLatency;
  private String serverResponseLatency;
  private String tcpClientLossBytes;
  private String tcpServerLossBytes;
  private String tcpClientZeroWindowPackets;
  private String tcpServerZeroWindowPackets;
  private Integer tcpSessionState;
  private String tcpEstablishedSuccessFlag;
  private String tcpEstablishedFailFlag;
  private String establishedSessions;
  private String tcpSynPackets;
  private String tcpSynAckPackets;
  private String tcpSynRstPackets;
  private String tcpClientPackets;
  private String tcpServerPackets;
  private String tcpClientRetransmissionPackets;
  private String tcpServerRetransmissionPackets;
  private String tcpRetransmissionRate;

  private String dsl;
  private String sourceType;// 数据源
  private String packetFileId;// 离线数据包文件ID
  private int timePrecision = 9;

  // 查询范围是否包含开始时间
  private boolean includeStartTime = true;
  // 查询范围是否包含结束时间
  private boolean includeEndTime = false;

  private String entry;// 入口页面标记（中文）

  // 安全告警跳转时的sid
  private Integer sid;

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(entry)) {
      entry = "会话详单";
    }
    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_QUERY:
        builder.append("查询").append(entry).append("数据，");
        break;
      case LogHelper.AUDIT_LOG_ACTION_EXPORT:
        builder.append("导出查询后的").append(entry).append("数据，");
        break;
      default:
        return "";
    }
    builder.append("数据源：").append(sourceType).append("；");
    builder.append("查询条件：").append(dsl).append("；");
    builder.append("开始时间=").append(startTime).append("；");
    builder.append("结束时间=").append(endTime).append("。");

    return builder.toString();
  }

  @Override
  public String toString() {
    return "FlowLogQueryVO{" + "id='" + id + '\'' + ", interfaceName='" + interfaceName + '\''
        + ", flowId=" + flowId + ", timestamp='" + timestamp + '\'' + ", networkId='" + networkId
        + '\'' + ", serviceId='" + serviceId + '\'' + ", startTime='" + startTime + '\''
        + ", endTime='" + endTime + '\'' + ", startTimeDate=" + startTimeDate + ", endTimeDate="
        + endTimeDate + ", flowContinued=" + flowContinued + ", packetSigseq='" + packetSigseq
        + '\'' + ", ethernetType=" + ethernetType + ", ethernetInitiator='" + ethernetInitiator
        + '\'' + ", ethernetResponder='" + ethernetResponder + '\'' + ", ethernetProtocol='"
        + ethernetProtocol + '\'' + ", vlanId=" + vlanId + ", ipLocalityInitiator="
        + ipLocalityInitiator + ", ipLocalityResponder=" + ipLocalityResponder + ", ipInitiator='"
        + ipInitiator + '\'' + ", ipResponder='" + ipResponder + '\'' + ", ipProtocol='"
        + ipProtocol + '\'' + ", portInitiator='" + portInitiator + '\'' + ", portResponder='"
        + portResponder + '\'' + ", l7ProtocolId='" + l7ProtocolId + '\'' + ", applicationIds='"
        + applicationIds + '\'' + ", maliciousApplicationIds='" + maliciousApplicationIds + '\''
        + ", countryIdInitiator='" + countryIdInitiator + '\'' + ", provinceIdInitiator='"
        + provinceIdInitiator + '\'' + ", cityIdInitiator='" + cityIdInitiator + '\''
        + ", countryIdResponder='" + countryIdResponder + '\'' + ", provinceIdResponder='"
        + provinceIdResponder + '\'' + ", cityIdResponder='" + cityIdResponder + '\''
        + ", locationInitiator='" + locationInitiator + '\'' + ", locationResponder='"
        + locationResponder + '\'' + ", reportTime='" + reportTime + '\'' + ", duration='"
        + duration + '\'' + ", upstreamBytes='" + upstreamBytes + '\'' + ", downstreamBytes='"
        + downstreamBytes + '\'' + ", totalBytes='" + totalBytes + '\'' + ", upstreamPackets='"
        + upstreamPackets + '\'' + ", downstreamPackets='" + downstreamPackets + '\''
        + ", totalPackets='" + totalPackets + '\'' + ", upstreamPayloadBytes='"
        + upstreamPayloadBytes + '\'' + ", downstreamPayloadBytes='" + downstreamPayloadBytes + '\''
        + ", totalPayloadBytes='" + totalPayloadBytes + '\'' + ", upstreamPayloadPackets='"
        + upstreamPayloadPackets + '\'' + ", downstreamPayloadPackets='" + downstreamPayloadPackets
        + '\'' + ", totalPayloadPackets='" + totalPayloadPackets + '\''
        + ", tcpClientNetworkLatency='" + tcpClientNetworkLatency + '\''
        + ", tcpServerNetworkLatency='" + tcpServerNetworkLatency + '\''
        + ", serverResponseLatency='" + serverResponseLatency + '\'' + ", tcpClientLossBytes='"
        + tcpClientLossBytes + '\'' + ", tcpServerLossBytes='" + tcpServerLossBytes + '\''
        + ", tcpClientZeroWindowPackets='" + tcpClientZeroWindowPackets + '\''
        + ", tcpServerZeroWindowPackets='" + tcpServerZeroWindowPackets + '\''
        + ", tcpSessionState=" + tcpSessionState + ", tcpEstablishedSuccessFlag='"
        + tcpEstablishedSuccessFlag + '\'' + ", tcpEstablishedFailFlag='" + tcpEstablishedFailFlag
        + '\'' + ", establishedSessions='" + establishedSessions + '\'' + ", tcpSynPackets='"
        + tcpSynPackets + '\'' + ", tcpSynAckPackets='" + tcpSynAckPackets + '\''
        + ", tcpSynRstPackets='" + tcpSynRstPackets + '\'' + ", tcpClientPackets='"
        + tcpClientPackets + '\'' + ", tcpServerPackets='" + tcpServerPackets + '\''
        + ", tcpClientRetransmissionPackets='" + tcpClientRetransmissionPackets + '\''
        + ", tcpServerRetransmissionPackets='" + tcpServerRetransmissionPackets + '\''
        + ", tcpRetransmissionRate='" + tcpRetransmissionRate + '\'' + ", dsl='" + dsl + '\''
        + ", sourceType='" + sourceType + '\'' + ", packetFileId='" + packetFileId + '\''
        + ", timePrecision=" + timePrecision + ", includeStartTime=" + includeStartTime
        + ", includeEndTime=" + includeEndTime + ", entry='" + entry + '\'' + ", sid=" + sid + + '\'' + '}';
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
  }

  public Long getFlowId() {
    return flowId;
  }

  public void setFlowId(Long flowId) {
    this.flowId = flowId;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public Date getStartTimeDate() {
    return startTimeDate;
  }

  public void setStartTimeDate(Date startTimeDate) {
    this.startTimeDate = startTimeDate;
  }

  public Date getEndTimeDate() {
    return endTimeDate;
  }

  public void setEndTimeDate(Date endTimeDate) {
    this.endTimeDate = endTimeDate;
  }

  public Boolean getFlowContinued() {
    return flowContinued;
  }

  public void setFlowContinued(Boolean flowContinued) {
    this.flowContinued = flowContinued;
  }

  public Integer getEthernetType() {
    return ethernetType;
  }

  public void setEthernetType(Integer ethernetType) {
    this.ethernetType = ethernetType;
  }

  public String getEthernetInitiator() {
    return ethernetInitiator;
  }

  public void setEthernetInitiator(String ethernetInitiator) {
    this.ethernetInitiator = ethernetInitiator;
  }

  public String getEthernetResponder() {
    return ethernetResponder;
  }

  public void setEthernetResponder(String ethernetResponder) {
    this.ethernetResponder = ethernetResponder;
  }

  public String getEthernetProtocol() {
    return ethernetProtocol;
  }

  public void setEthernetProtocol(String ethernetProtocol) {
    this.ethernetProtocol = ethernetProtocol;
  }

  public Integer getVlanId() {
    return vlanId;
  }

  public void setVlanId(Integer vlanId) {
    this.vlanId = vlanId;
  }

  public Integer getIpLocalityInitiator() {
    return ipLocalityInitiator;
  }

  public void setIpLocalityInitiator(Integer ipLocalityInitiator) {
    this.ipLocalityInitiator = ipLocalityInitiator;
  }

  public Integer getIpLocalityResponder() {
    return ipLocalityResponder;
  }

  public void setIpLocalityResponder(Integer ipLocalityResponder) {
    this.ipLocalityResponder = ipLocalityResponder;
  }

  public String getIpInitiator() {
    return ipInitiator;
  }

  public void setIpInitiator(String ipInitiator) {
    this.ipInitiator = ipInitiator;
  }

  public String getIpResponder() {
    return ipResponder;
  }

  public void setIpResponder(String ipResponder) {
    this.ipResponder = ipResponder;
  }

  public String getIpProtocol() {
    return ipProtocol;
  }

  public void setIpProtocol(String ipProtocol) {
    this.ipProtocol = ipProtocol;
  }

  public String getPortInitiator() {
    return portInitiator;
  }

  public void setPortInitiator(String portInitiator) {
    this.portInitiator = portInitiator;
  }

  public String getPortResponder() {
    return portResponder;
  }

  public void setPortResponder(String portResponder) {
    this.portResponder = portResponder;
  }

  public String getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(String l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public String getApplicationIds() {
    return applicationIds;
  }

  public void setApplicationIds(String applicationIds) {
    this.applicationIds = applicationIds;
  }

  public String getMaliciousApplicationIds() {
    return maliciousApplicationIds;
  }

  public void setMaliciousApplicationIds(String maliciousApplicationIds) {
    this.maliciousApplicationIds = maliciousApplicationIds;
  }

  public String getCountryIdInitiator() {
    return countryIdInitiator;
  }

  public void setCountryIdInitiator(String countryIdInitiator) {
    this.countryIdInitiator = countryIdInitiator;
  }

  public String getProvinceIdInitiator() {
    return provinceIdInitiator;
  }

  public void setProvinceIdInitiator(String provinceIdInitiator) {
    this.provinceIdInitiator = provinceIdInitiator;
  }

  public String getCityIdInitiator() {
    return cityIdInitiator;
  }

  public void setCityIdInitiator(String cityIdInitiator) {
    this.cityIdInitiator = cityIdInitiator;
  }

  public String getCountryIdResponder() {
    return countryIdResponder;
  }

  public void setCountryIdResponder(String countryIdResponder) {
    this.countryIdResponder = countryIdResponder;
  }

  public String getProvinceIdResponder() {
    return provinceIdResponder;
  }

  public void setProvinceIdResponder(String provinceIdResponder) {
    this.provinceIdResponder = provinceIdResponder;
  }

  public String getCityIdResponder() {
    return cityIdResponder;
  }

  public void setCityIdResponder(String cityIdResponder) {
    this.cityIdResponder = cityIdResponder;
  }

  public String getLocationInitiator() {
    return locationInitiator;
  }

  public void setLocationInitiator(String locationInitiator) {
    this.locationInitiator = locationInitiator;
  }

  public String getLocationResponder() {
    return locationResponder;
  }

  public void setLocationResponder(String locationResponder) {
    this.locationResponder = locationResponder;
  }

  public String getReportTime() {
    return reportTime;
  }

  public void setReportTime(String reportTime) {
    this.reportTime = reportTime;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String getUpstreamBytes() {
    return upstreamBytes;
  }

  public void setUpstreamBytes(String upstreamBytes) {
    this.upstreamBytes = upstreamBytes;
  }

  public String getDownstreamBytes() {
    return downstreamBytes;
  }

  public void setDownstreamBytes(String downstreamBytes) {
    this.downstreamBytes = downstreamBytes;
  }

  public String getTotalBytes() {
    return totalBytes;
  }

  public void setTotalBytes(String totalBytes) {
    this.totalBytes = totalBytes;
  }

  public String getUpstreamPackets() {
    return upstreamPackets;
  }

  public void setUpstreamPackets(String upstreamPackets) {
    this.upstreamPackets = upstreamPackets;
  }

  public String getDownstreamPackets() {
    return downstreamPackets;
  }

  public void setDownstreamPackets(String downstreamPackets) {
    this.downstreamPackets = downstreamPackets;
  }

  public String getTotalPackets() {
    return totalPackets;
  }

  public void setTotalPackets(String totalPackets) {
    this.totalPackets = totalPackets;
  }

  public String getUpstreamPayloadBytes() {
    return upstreamPayloadBytes;
  }

  public void setUpstreamPayloadBytes(String upstreamPayloadBytes) {
    this.upstreamPayloadBytes = upstreamPayloadBytes;
  }

  public String getDownstreamPayloadBytes() {
    return downstreamPayloadBytes;
  }

  public void setDownstreamPayloadBytes(String downstreamPayloadBytes) {
    this.downstreamPayloadBytes = downstreamPayloadBytes;
  }

  public String getTotalPayloadBytes() {
    return totalPayloadBytes;
  }

  public void setTotalPayloadBytes(String totalPayloadBytes) {
    this.totalPayloadBytes = totalPayloadBytes;
  }

  public String getUpstreamPayloadPackets() {
    return upstreamPayloadPackets;
  }

  public void setUpstreamPayloadPackets(String upstreamPayloadPackets) {
    this.upstreamPayloadPackets = upstreamPayloadPackets;
  }

  public String getDownstreamPayloadPackets() {
    return downstreamPayloadPackets;
  }

  public void setDownstreamPayloadPackets(String downstreamPayloadPackets) {
    this.downstreamPayloadPackets = downstreamPayloadPackets;
  }

  public String getTotalPayloadPackets() {
    return totalPayloadPackets;
  }

  public void setTotalPayloadPackets(String totalPayloadPackets) {
    this.totalPayloadPackets = totalPayloadPackets;
  }

  public String getTcpClientNetworkLatency() {
    return tcpClientNetworkLatency;
  }

  public void setTcpClientNetworkLatency(String tcpClientNetworkLatency) {
    this.tcpClientNetworkLatency = tcpClientNetworkLatency;
  }

  public String getTcpServerNetworkLatency() {
    return tcpServerNetworkLatency;
  }

  public void setTcpServerNetworkLatency(String tcpServerNetworkLatency) {
    this.tcpServerNetworkLatency = tcpServerNetworkLatency;
  }

  public String getServerResponseLatency() {
    return serverResponseLatency;
  }

  public void setServerResponseLatency(String serverResponseLatency) {
    this.serverResponseLatency = serverResponseLatency;
  }

  public String getTcpClientLossBytes() {
    return tcpClientLossBytes;
  }

  public void setTcpClientLossBytes(String tcpClientLossBytes) {
    this.tcpClientLossBytes = tcpClientLossBytes;
  }

  public String getTcpServerLossBytes() {
    return tcpServerLossBytes;
  }

  public void setTcpServerLossBytes(String tcpServerLossBytes) {
    this.tcpServerLossBytes = tcpServerLossBytes;
  }

  public String getTcpClientZeroWindowPackets() {
    return tcpClientZeroWindowPackets;
  }

  public void setTcpClientZeroWindowPackets(String tcpClientZeroWindowPackets) {
    this.tcpClientZeroWindowPackets = tcpClientZeroWindowPackets;
  }

  public String getTcpServerZeroWindowPackets() {
    return tcpServerZeroWindowPackets;
  }

  public void setTcpServerZeroWindowPackets(String tcpServerZeroWindowPackets) {
    this.tcpServerZeroWindowPackets = tcpServerZeroWindowPackets;
  }

  public Integer getTcpSessionState() {
    return tcpSessionState;
  }

  public void setTcpSessionState(Integer tcpSessionState) {
    this.tcpSessionState = tcpSessionState;
  }

  public String getTcpEstablishedSuccessFlag() {
    return tcpEstablishedSuccessFlag;
  }

  public void setTcpEstablishedSuccessFlag(String tcpEstablishedSuccessFlag) {
    this.tcpEstablishedSuccessFlag = tcpEstablishedSuccessFlag;
  }

  public String getTcpEstablishedFailFlag() {
    return tcpEstablishedFailFlag;
  }

  public void setTcpEstablishedFailFlag(String tcpEstablishedFailFlag) {
    this.tcpEstablishedFailFlag = tcpEstablishedFailFlag;
  }

  public String getEstablishedSessions() {
    return establishedSessions;
  }

  public void setEstablishedSessions(String establishedSessions) {
    this.establishedSessions = establishedSessions;
  }

  public String getTcpSynPackets() {
    return tcpSynPackets;
  }

  public void setTcpSynPackets(String tcpSynPackets) {
    this.tcpSynPackets = tcpSynPackets;
  }

  public String getTcpSynAckPackets() {
    return tcpSynAckPackets;
  }

  public void setTcpSynAckPackets(String tcpSynAckPackets) {
    this.tcpSynAckPackets = tcpSynAckPackets;
  }

  public String getTcpSynRstPackets() {
    return tcpSynRstPackets;
  }

  public void setTcpSynRstPackets(String tcpSynRstPackets) {
    this.tcpSynRstPackets = tcpSynRstPackets;
  }

  public String getTcpClientPackets() {
    return tcpClientPackets;
  }

  public void setTcpClientPackets(String tcpClientPackets) {
    this.tcpClientPackets = tcpClientPackets;
  }

  public String getTcpServerPackets() {
    return tcpServerPackets;
  }

  public void setTcpServerPackets(String tcpServerPackets) {
    this.tcpServerPackets = tcpServerPackets;
  }

  public String getTcpClientRetransmissionPackets() {
    return tcpClientRetransmissionPackets;
  }

  public void setTcpClientRetransmissionPackets(String tcpClientRetransmissionPackets) {
    this.tcpClientRetransmissionPackets = tcpClientRetransmissionPackets;
  }

  public String getTcpServerRetransmissionPackets() {
    return tcpServerRetransmissionPackets;
  }

  public void setTcpServerRetransmissionPackets(String tcpServerRetransmissionPackets) {
    this.tcpServerRetransmissionPackets = tcpServerRetransmissionPackets;
  }

  public String getTcpRetransmissionRate() {
    return tcpRetransmissionRate;
  }

  public void setTcpRetransmissionRate(String tcpRetransmissionRate) {
    this.tcpRetransmissionRate = tcpRetransmissionRate;
  }

  public String getDsl() {
    return dsl;
  }

  public void setDsl(String dsl) {
    this.dsl = dsl;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getPacketFileId() {
    return packetFileId;
  }

  public void setPacketFileId(String packetFileId) {
    this.packetFileId = packetFileId;
  }

  public int getTimePrecision() {
    return timePrecision;
  }

  public void setTimePrecision(int timePrecision) {
    this.timePrecision = timePrecision;
  }

  public boolean getIncludeStartTime() {
    return includeStartTime;
  }

  public void setIncludeStartTime(boolean includeStartTime) {
    this.includeStartTime = includeStartTime;
  }

  public boolean getIncludeEndTime() {
    return includeEndTime;
  }

  public void setIncludeEndTime(boolean includeEndTime) {
    this.includeEndTime = includeEndTime;
  }

  public String getEntry() {
    return entry;
  }

  public void setEntry(String entry) {
    this.entry = entry;
  }

  public String getPacketSigseq() {
    return packetSigseq;
  }

  public void setPacketSigseq(String packetSigseq) {
    this.packetSigseq = packetSigseq;
  }

  public Integer getSid() {
    return sid;
  }

  public void setSid(Integer sid) {
    this.sid = sid;
  }
}
