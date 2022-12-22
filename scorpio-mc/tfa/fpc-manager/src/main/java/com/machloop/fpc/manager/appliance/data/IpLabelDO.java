package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author chenshimiao
 *
 * create at 2022/9/6 11:06 AM,cms
 * @version 1.0
 */
public class IpLabelDO extends BaseOperateDO {

  private String name;
  private String ipAddress;
  private String category;
  private String description;

  @Override
  public String toString() {
    return "IpLabelDO[" + "name='" + name + ", ipAddress='" + ipAddress + ", category='" + category
        + ", description='" + description + ']';
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

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
