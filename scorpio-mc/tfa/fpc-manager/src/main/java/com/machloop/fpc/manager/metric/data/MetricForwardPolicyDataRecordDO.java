package com.machloop.fpc.manager.metric.data;

import java.util.Date;

/**
 * @author ChenXiao
 *
 * create at 2022/5/17 9:34,IntelliJ IDEA
 *
 */
public class MetricForwardPolicyDataRecordDO {

  private Date timestamp;

  private String policyId;

  private String networkId;

  private String netifName;

  private long forwardTotalBytes;

  private long forwardSuccessBytes;

  private long forwardFailBytes;

  private long forwardTotalPackets;

  private long forwardSuccessPackets;

  private long forwardFailPackets;

  @Override public String toString() {
    return "MetricForwardPolicyDataRecordDO{" + "timestamp=" + timestamp + ", policyId='" + policyId
        + '\'' + ", networkId='" + networkId + '\'' + ", netifName='" + netifName + '\''
        + ", forwardTotalBytes=" + forwardTotalBytes + ", forwardSuccessBytes="
        + forwardSuccessBytes + ", forwardFailBytes=" + forwardFailBytes + ", forwardTotalPackets="
        + forwardTotalPackets + ", forwardSuccessPackets=" + forwardSuccessPackets
        + ", forwardFailPackets=" + forwardFailPackets + '}';
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getPolicyId() {
    return policyId;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getNetifName() {
    return netifName;
  }

  public void setNetifName(String netifName) {
    this.netifName = netifName;
  }

  public long getForwardTotalBytes() {
    return forwardTotalBytes;
  }

  public void setForwardTotalBytes(long forwardTotalBytes) {
    this.forwardTotalBytes = forwardTotalBytes;
  }

  public long getForwardSuccessBytes() {
    return forwardSuccessBytes;
  }

  public void setForwardSuccessBytes(long forwardSuccessBytes) {
    this.forwardSuccessBytes = forwardSuccessBytes;
  }

  public long getForwardFailBytes() {
    return forwardFailBytes;
  }

  public void setForwardFailBytes(long forwardFailBytes) {
    this.forwardFailBytes = forwardFailBytes;
  }

  public long getForwardTotalPackets() {
    return forwardTotalPackets;
  }

  public void setForwardTotalPackets(long forwardTotalPackets) {
    this.forwardTotalPackets = forwardTotalPackets;
  }

  public long getForwardSuccessPackets() {
    return forwardSuccessPackets;
  }

  public void setForwardSuccessPackets(long forwardSuccessPackets) {
    this.forwardSuccessPackets = forwardSuccessPackets;
  }

  public long getForwardFailPackets() {
    return forwardFailPackets;
  }

  public void setForwardFailPackets(long forwardFailPackets) {
    this.forwardFailPackets = forwardFailPackets;
  }
}
