package com.machloop.fpc.manager.metric.data;

import java.util.Date;

/**
 * @author guosk
 *
 * create at 2021年4月23日, fpc-manager
 */
public class MetricDscpDataRecordDO {

  private Date timestamp;
  private String networkId;
  private String serviceId;
  private String type;

  private long totalBytes;
  private long totalPackets;

  @Override
  public String toString() {
    return "MetricDscpDataRecordDO [timestamp=" + timestamp + ", networkId=" + networkId
        + ", serviceId=" + serviceId + ", type=" + type + ", totalBytes=" + totalBytes
        + ", totalPackets=" + totalPackets + "]";
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

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

}
