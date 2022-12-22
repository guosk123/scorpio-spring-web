package com.machloop.fpc.manager.metadata.data;

import java.util.List;

public class ProtocolDhcpLogDO extends AbstractLogRecordDO {

  private String srcIpv4;
  private String srcIpv6;
  private String destIpv4;
  private String destIpv6;
  private int srcPort;
  private int destPort;
  private int version;
  private String srcMac;
  private String destMac;
  private int messageType;
  private String transactionId;
  private List<Integer> parameters;
  private String offeredIpv4Address;
  private String offeredIpv6Address;
  private long upstreamBytes;
  private long downstreamBytes;

  @Override
  public String toString() {
    return "ProtocolDhcpLogDO [srcIpv4=" + srcIpv4 + ", srcIpv6=" + srcIpv6 + ", destIpv4="
        + destIpv4 + ", destIpv6=" + destIpv6 + ", srcPort=" + srcPort + ", destPort=" + destPort
        + ", version=" + version + ", srcMac=" + srcMac + ", destMac=" + destMac + ", messageType="
        + messageType + ", transactionId=" + transactionId + ", parameters=" + parameters
        + ", offeredIpv4Address=" + offeredIpv4Address + ", offeredIpv6Address="
        + offeredIpv6Address + ", upstreamBytes=" + upstreamBytes + ", downstreamBytes="
        + downstreamBytes + "]";
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

  public int getSrcPort() {
    return srcPort;
  }

  public void setSrcPort(int srcPort) {
    this.srcPort = srcPort;
  }

  public int getDestPort() {
    return destPort;
  }

  public void setDestPort(int destPort) {
    this.destPort = destPort;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getSrcMac() {
    return srcMac;
  }

  public void setSrcMac(String srcMac) {
    this.srcMac = srcMac;
  }

  public String getDestMac() {
    return destMac;
  }

  public void setDestMac(String destMac) {
    this.destMac = destMac;
  }

  public int getMessageType() {
    return messageType;
  }

  public void setMessageType(int messageType) {
    this.messageType = messageType;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public List<Integer> getParameters() {
    return parameters;
  }

  public void setParameters(List<Integer> parameters) {
    this.parameters = parameters;
  }

  public String getOfferedIpv4Address() {
    return offeredIpv4Address;
  }

  public void setOfferedIpv4Address(String offeredIpv4Address) {
    this.offeredIpv4Address = offeredIpv4Address;
  }

  public String getOfferedIpv6Address() {
    return offeredIpv6Address;
  }

  public void setOfferedIpv6Address(String offeredIpv6Address) {
    this.offeredIpv6Address = offeredIpv6Address;
  }

  public long getUpstreamBytes() {
    return upstreamBytes;
  }

  public void setUpstreamBytes(long upstreamBytes) {
    this.upstreamBytes = upstreamBytes;
  }

  public long getDownstreamBytes() {
    return downstreamBytes;
  }

  public void setDownstreamBytes(long downstreamBytes) {
    this.downstreamBytes = downstreamBytes;
  }

}
