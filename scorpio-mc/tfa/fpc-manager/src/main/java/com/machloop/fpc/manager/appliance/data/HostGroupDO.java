package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月5日, fpc-manager
 */
public class HostGroupDO extends BaseOperateDO {
  private String name;
  private String ipAddress;
  private String hostGroupInCmsId;
  private String description;

  @Override
  public String toString() {
    return "HostGroupDO [name=" + name + ", ipAddress=" + ipAddress + ", hostGroupInCmsId="
        + hostGroupInCmsId + ", description=" + description + "]";
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

  public String getHostGroupInCmsId() {
    return hostGroupInCmsId;
  }

  public void setHostGroupInCmsId(String hostGroupInCmsId) {
    this.hostGroupInCmsId = hostGroupInCmsId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
