package com.machloop.fpc.cms.center.metric.data;

import java.util.Date;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public class MetricDhcpDataRecordDO {
  private Date timestamp;
  private String networkId;
  private String clientIpAddress;
  private String serverIpAddress;
  private String clientMacAddress;
  private String serverMacAddress;
  private int messageType;
  private int dhcpVersion;

  private long totalBytes;
  private long totalPackets;
  private long sendBytes;
  private long sendPackets;
  private long receiveBytes;
  private long receivePackets;

  @Override
  public String toString() {
    return "MetricDhcpDataRecordDO [timestamp=" + timestamp + ", networkId=" + networkId
        + ", clientIpAddress=" + clientIpAddress + ", serverIpAddress=" + serverIpAddress
        + ", clientMacAddress=" + clientMacAddress + ", serverMacAddress=" + serverMacAddress
        + ", messageType=" + messageType + ", dhcpVersion=" + dhcpVersion + ", totalBytes="
        + totalBytes + ", totalPackets=" + totalPackets + ", sendBytes=" + sendBytes
        + ", sendPackets=" + sendPackets + ", receiveBytes=" + receiveBytes + ", receivePackets="
        + receivePackets + "]";
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

  public String getClientIpAddress() {
    return clientIpAddress;
  }

  public void setClientIpAddress(String clientIpAddress) {
    this.clientIpAddress = clientIpAddress;
  }

  public String getServerIpAddress() {
    return serverIpAddress;
  }

  public void setServerIpAddress(String serverIpAddress) {
    this.serverIpAddress = serverIpAddress;
  }

  public String getClientMacAddress() {
    return clientMacAddress;
  }

  public void setClientMacAddress(String clientMacAddress) {
    this.clientMacAddress = clientMacAddress;
  }

  public String getServerMacAddress() {
    return serverMacAddress;
  }

  public void setServerMacAddress(String serverMacAddress) {
    this.serverMacAddress = serverMacAddress;
  }

  public int getMessageType() {
    return messageType;
  }

  public void setMessageType(int messageType) {
    this.messageType = messageType;
  }

  public int getDhcpVersion() {
    return dhcpVersion;
  }

  public void setDhcpVersion(int dhcpVersion) {
    this.dhcpVersion = dhcpVersion;
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

  public long getSendBytes() {
    return sendBytes;
  }

  public void setSendBytes(long sendBytes) {
    this.sendBytes = sendBytes;
  }

  public long getSendPackets() {
    return sendPackets;
  }

  public void setSendPackets(long sendPackets) {
    this.sendPackets = sendPackets;
  }

  public long getReceiveBytes() {
    return receiveBytes;
  }

  public void setReceiveBytes(long receiveBytes) {
    this.receiveBytes = receiveBytes;
  }

  public long getReceivePackets() {
    return receivePackets;
  }

  public void setReceivePackets(long receivePackets) {
    this.receivePackets = receivePackets;
  }

}
