package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author liumeng
 *
 * create at 2018年12月12日, fpc-manager
 */
public class HostInsideDO extends BaseOperateDO {

  private String ipAddress;
  private long ipStart;
  private long ipEnd;
  private String description;

  @Override
  public String toString() {
    return "HostInsideDO [ipAddress=" + ipAddress + ", ipStart=" + ipStart + ", ipEnd=" + ipEnd
        + ", description=" + description + "]" + super.toString();
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


}
