package com.machloop.fpc.npm.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2020年11月11日, fpc-manager
 */
public class NetworkInsideIpDO extends BaseDO {

  private String networkId;
  private String ipAddress;
  private long ipStart;
  private long ipEnd;
  private Date timestamp;
  private String operatorId;

  @Override
  public String toString() {
    return "NetworkInsideIpDO [networkId=" + networkId + ", ipAddress=" + ipAddress + ", ipStart="
        + ipStart + ", ipEnd=" + ipEnd + ", timestamp=" + timestamp + ", operatorId=" + operatorId
        + "]";
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public long getIpStart() {
    return ipStart;
  }

  public void setIpStart(long ipStart) {
    this.ipStart = ipStart;
  }

  public long getIpEnd() {
    return ipEnd;
  }

  public void setIpEnd(long ipEnd) {
    this.ipEnd = ipEnd;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
