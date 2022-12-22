package com.machloop.fpc.manager.system.data;

import java.util.Date;
import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public class DeviceNetifDO extends BaseOperateDO {

  private String name;
  private String state;
  private String category;
  private String type;
  private String ipv4Address;
  private String ipv4Gateway;
  private String ipv6Address;
  private String ipv6Gateway;
  private int specification;
  private String description;
  private String operatorId;
  private Date updateTime;

  @Override
  public String toString() {
    return "DeviceNetifDO [name=" + name + ", state=" + state + ", category=" + category
        + ", specification=" + specification + ", description=" + description + ", type=" + type
        + ", ipv4Address=" + ipv4Address + ", ipv4Gateway=" + ipv4Gateway + ", ipv6Address="
        + ipv6Address + ", ipv6Gateway=" + ipv6Gateway + ", operatorId=" + operatorId
        + ", updateTime=" + updateTime + super.toString() + "]";
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public String getIpv4Address() {
    return ipv4Address;
  }

  public void setIpv4Address(String ipv4Address) {
    this.ipv4Address = ipv4Address;
  }

  public String getIpv4Gateway() {
    return ipv4Gateway;
  }

  public void setIpv4Gateway(String ipv4Gateway) {
    this.ipv4Gateway = ipv4Gateway;
  }

  public String getIpv6Address() {
    return ipv6Address;
  }

  public void setIpv6Address(String ipv6Address) {
    this.ipv6Address = ipv6Address;
  }

  public String getIpv6Gateway() {
    return ipv6Gateway;
  }

  public void setIpv6Gateway(String ipv6Gateway) {
    this.ipv6Gateway = ipv6Gateway;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public int getSpecification() {
    return specification;
  }

  public void setSpecification(int specification) {
    this.specification = specification;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
