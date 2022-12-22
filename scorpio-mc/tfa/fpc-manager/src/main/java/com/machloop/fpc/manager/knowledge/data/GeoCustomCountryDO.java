package com.machloop.fpc.manager.knowledge.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2021年8月25日, fpc-manager
 */
public class GeoCustomCountryDO extends BaseOperateDO {

  private String name;
  private String countryId;
  private String longitude;
  private String latitude;
  private String customCountryInCmsId;
  private String description;

  @Override
  public String toString() {
    return "GeoCustomCountryDO [name=" + name + ", countryId=" + countryId + ", longitude="
        + longitude + ", latitude=" + latitude + ", customCountryInCmsId=" + customCountryInCmsId
        + ", description=" + description + "]";
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

  public String getCustomCountryInCmsId() {
    return customCountryInCmsId;
  }

  public void setCustomCountryInCmsId(String customCountryInCmsId) {
    this.customCountryInCmsId = customCountryInCmsId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
