package com.machloop.fpc.manager.system.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public class DeviceNetifModificationVO {

  @NotEmpty(message = "修改接口时传入的id不能为空")
  private String id;
  @NotEmpty(message = "修改接口时传入的类型不能为空")
  private String category;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String type;
  private String ipv4Address;
  private String ipv4Gateway;
  private String ipv6Address;
  private String ipv6Gateway;
  private String description;

  @Override
  public String toString() {
    return "DeviceNetifModificationVO [id=" + id + ", category=" + category + ", description="
        + description + ", type=" + type + ", ipv4Address=" + ipv4Address + ", ipv4Gateway="
        + ipv4Gateway + ", ipv6Address=" + ipv6Address + ", ipv6Gateway=" + ipv6Gateway + "]";
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

  
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
