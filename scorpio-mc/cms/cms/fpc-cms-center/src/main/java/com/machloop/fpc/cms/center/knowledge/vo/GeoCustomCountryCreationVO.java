package com.machloop.fpc.cms.center.knowledge.vo;

import javax.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
public class GeoCustomCountryCreationVO {

  @NotEmpty(message = "地区名称不能为空")
  private String name;
  @NotEmpty(message = "经度不能为空")
  private String longitude;
  @NotEmpty(message = "维度不能为空")
  private String latitude;
  private String ipAddress;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "GeoCustomCountryCreationVO [name=" + name + ", longitude=" + longitude + ", latitude="
        + latitude + ", ipAddress=" + ipAddress + ", description=" + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
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
