package com.machloop.fpc.cms.center.knowledge.bo;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
public class GeoCityBO {
  
  private String cityId;
  private String provinceId;
  private String countryId;
  private String name;
  private String nameText;
  private String description;
  private String descriptionText;
  private String longitude;
  private String latitude;
  private String ipAddress;

  @Override
  public String toString() {
    return "GeoCityBO [cityId=" + cityId + ", provinceId=" + provinceId + ", countryId=" + countryId
        + ", name=" + name + ", nameText=" + nameText + ", description=" + description
        + ", descriptionText=" + descriptionText + ", longitude=" + longitude + ", latitude="
        + latitude + ", ipAddress=" + ipAddress + "]";
  }

  public String getCityId() {
    return cityId;
  }

  public void setCityId(String cityId) {
    this.cityId = cityId;
  }

  public String getProvinceId() {
    return provinceId;
  }

  public void setProvinceId(String provinceId) {
    this.provinceId = provinceId;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNameText() {
    return nameText;
  }

  public void setNameText(String nameText) {
    this.nameText = nameText;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescriptionText() {
    return descriptionText;
  }

  public void setDescriptionText(String descriptionText) {
    this.descriptionText = descriptionText;
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

}