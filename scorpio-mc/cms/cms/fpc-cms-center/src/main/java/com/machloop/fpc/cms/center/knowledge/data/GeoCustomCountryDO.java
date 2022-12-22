package com.machloop.fpc.cms.center.knowledge.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
public class GeoCustomCountryDO extends BaseOperateDO {

  private String assignId;
  private String name;
  private String countryId;
  private String longitude;
  private String latitude;
  private String description;

  @Override
  public String toString() {
    return "GeoCustomCountryDO [assignId=" + assignId + ", name=" + name + ", countryId="
        + countryId + ", longitude=" + longitude + ", latitude=" + latitude + ", description="
        + description + "]";
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

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
