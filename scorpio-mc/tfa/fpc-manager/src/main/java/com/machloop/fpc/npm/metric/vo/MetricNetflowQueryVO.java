package com.machloop.fpc.npm.metric.vo;

import java.util.Date;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月23日, fpc-manager
 */
public class MetricNetflowQueryVO {

  private String sessionId;
  private String deviceName;
  private String netifNo;
  private String inNetif;
  private String outNetif;
  private String ipv4Address;
  private String ipv6Address;
  private String srcIpv4;
  private String srcIpv6;
  private String destIpv4;
  private String destIpv6;
  private String srcPort;
  private String destPort;
  private String port;
  private String protocol;
  private String tcpFlag;
  private String dscpFlag;
  private double netifSpeed;
  private String totalBytes;
  private String totalPackets;
  private String transmitBytes;
  private String transmitPackets;
  private String ingestBytes;
  private String ingestPackets;
  private String duration;
  private String reportTime;
  private int interval;
  private String drilldown;
  private String dsl;
  private int timePrecision = 9;
  private String startTime;
  private String endTime;
  private Date startTimeDate;
  private Date endTimeDate;

  // 统计查询范围是否包含开始时间
  private boolean statisticsIncludeStartTime = false;
  // 统计查询范围是否包含结束时间
  private boolean statisticsIncludeEndTime = true;

  // 会话详单查询范围是否包含开始时间
  private boolean sessionIncludeStartTime = true;
  // 会话详单查询范围是否包含结束时间
  private boolean sessionIncludeEndTime = false;

  @Override
  public String toString() {
    return "MetricNetflowQueryVO [sessionId=" + sessionId + ", deviceName=" + deviceName
        + ", netifNo=" + netifNo + ", inNetif=" + inNetif + ", outNetif=" + outNetif
        + ", ipv4Address=" + ipv4Address + ", ipv6Address=" + ipv6Address + ", srcIpv4=" + srcIpv4
        + ", srcIpv6=" + srcIpv6 + ", destIpv4=" + destIpv4 + ", destIpv6=" + destIpv6
        + ", srcPort=" + srcPort + ", destPort=" + destPort + ", port=" + port + ", protocol="
        + protocol + ", tcpFlag=" + tcpFlag + ", dscpFlag=" + dscpFlag + ", netifSpeed="
        + netifSpeed + ", totalBytes=" + totalBytes + ", totalPackets=" + totalPackets
        + ", transmitBytes=" + transmitBytes + ", transmitPackets=" + transmitPackets
        + ", ingestBytes=" + ingestBytes + ", ingestPackets=" + ingestPackets + ", duration="
        + duration + ", reportTime=" + reportTime + ", interval=" + interval + ", drilldown="
        + drilldown + ", dsl=" + dsl + ", timePrecision=" + timePrecision + ", startTime="
        + startTime + ", endTime=" + endTime + ", startTimeDate=" + startTimeDate + ", endTimeDate="
        + endTimeDate + ", statisticsIncludeStartTime=" + statisticsIncludeStartTime
        + ", statisticsIncludeEndTime=" + statisticsIncludeEndTime + ", sessionIncludeStartTime="
        + sessionIncludeStartTime + ", sessionIncludeEndTime=" + sessionIncludeEndTime + "]";
  }

  public int getTimePrecision() {
    return timePrecision;
  }

  public void setTimePrecision(int timePrecision) {
    this.timePrecision = timePrecision;
  }

  public String getDsl() {
    return dsl;
  }

  public void setDsl(String dsl) {
    this.dsl = dsl;
  }


  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getDrilldown() {
    return drilldown;
  }

  public void setDrilldown(String drilldown) {
    this.drilldown = drilldown;
  }

  public String getIpv4Address() {
    return ipv4Address;
  }

  public void setIpv4Address(String ipv4Address) {
    this.ipv4Address = ipv4Address;
  }

  public String getIpv6Address() {
    return ipv6Address;
  }

  public void setIpv6Address(String ipv6Address) {
    this.ipv6Address = ipv6Address;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public double getNetifSpeed() {
    return netifSpeed;
  }

  public void setNetifSpeed(double netifSpeed) {
    this.netifSpeed = netifSpeed;
  }

  public String getNetifNo() {
    return netifNo;
  }

  public void setNetifNo(String netifNo) {
    this.netifNo = netifNo;
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

  public String getInNetif() {
    return inNetif;
  }

  public void setInNetif(String inNetif) {
    this.inNetif = inNetif;
  }

  public String getOutNetif() {
    return outNetif;
  }

  public void setOutNetif(String outNetif) {
    this.outNetif = outNetif;
  }

  public String getSrcIpv4() {
    return srcIpv4;
  }

  public void setSrcIpv4(String srcIpv4) {
    this.srcIpv4 = srcIpv4;
  }

  public String getSrcIpv6() {
    return srcIpv6;
  }

  public void setSrcIpv6(String srcIpv6) {
    this.srcIpv6 = srcIpv6;
  }

  public String getDestIpv4() {
    return destIpv4;
  }

  public void setDestIpv4(String destIpv4) {
    this.destIpv4 = destIpv4;
  }

  public String getDestIpv6() {
    return destIpv6;
  }

  public void setDestIpv6(String destIpv6) {
    this.destIpv6 = destIpv6;
  }

  public String getSrcPort() {
    return srcPort;
  }

  public void setSrcPort(String srcPort) {
    this.srcPort = srcPort;
  }

  public String getDestPort() {
    return destPort;
  }

  public void setDestPort(String destPort) {
    this.destPort = destPort;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getTcpFlag() {
    return tcpFlag;
  }

  public void setTcpFlag(String tcpFlag) {
    this.tcpFlag = tcpFlag;
  }

  public String getDscpFlag() {
    return dscpFlag;
  }

  public void setDscpFlag(String dscpFlag) {
    this.dscpFlag = dscpFlag;
  }

  public String getReportTime() {
    return reportTime;
  }

  public void setReportTime(String reportTime) {
    this.reportTime = reportTime;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
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

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getTotalBytes() {
    return totalBytes;
  }

  public void setTotalBytes(String totalBytes) {
    this.totalBytes = totalBytes;
  }

  public String getTotalPackets() {
    return totalPackets;
  }

  public void setTotalPackets(String totalPackets) {
    this.totalPackets = totalPackets;
  }

  public String getTransmitBytes() {
    return transmitBytes;
  }

  public void setTransmitBytes(String transmitBytes) {
    this.transmitBytes = transmitBytes;
  }

  public String getTransmitPackets() {
    return transmitPackets;
  }

  public void setTransmitPackets(String transmitPackets) {
    this.transmitPackets = transmitPackets;
  }

  public String getIngestBytes() {
    return ingestBytes;
  }

  public void setIngestBytes(String ingestBytes) {
    this.ingestBytes = ingestBytes;
  }

  public String getIngestPackets() {
    return ingestPackets;
  }

  public void setIngestPackets(String ingestPackets) {
    this.ingestPackets = ingestPackets;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public boolean getStatisticsIncludeStartTime() {
    return statisticsIncludeStartTime;
  }

  public void setStatisticsIncludeStartTime(boolean statisticsIncludeStartTime) {
    this.statisticsIncludeStartTime = statisticsIncludeStartTime;
  }

  public boolean getStatisticsIncludeEndTime() {
    return statisticsIncludeEndTime;
  }

  public void setStatisticsIncludeEndTime(boolean statisticsIncludeEndTime) {
    this.statisticsIncludeEndTime = statisticsIncludeEndTime;
  }

  public boolean getSessionIncludeStartTime() {
    return sessionIncludeStartTime;
  }

  public void setSessionIncludeStartTime(boolean sessionIncludeStartTime) {
    this.sessionIncludeStartTime = sessionIncludeStartTime;
  }

  public boolean getSessionIncludeEndTime() {
    return sessionIncludeEndTime;
  }

  public void setSessionIncludeEndTime(boolean sessionIncludeEndTime) {
    this.sessionIncludeEndTime = sessionIncludeEndTime;
  }

}
