package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月5日, fpc-manager
 */
public class HostGroupDO extends BaseOperateDO {
  private String assignId;
  private String name;
  private String ipAddress;
  private String description;

  @Override
  public String toString() {
    return "HostGroupDO [assignId=" + assignId + ", name=" + name + ", ipAddress=" + ipAddress
        + ", description=" + description + "]";
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
