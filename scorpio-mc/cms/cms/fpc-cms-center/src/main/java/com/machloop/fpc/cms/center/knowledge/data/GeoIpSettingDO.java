package com.machloop.fpc.cms.center.knowledge.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月18日, fpc-cms-center
 */
public class GeoIpSettingDO extends BaseOperateDO {

  private String assignId;
  private String countryId;
  private String provinceId;
  private String cityId;
  private String ipAddress;

  @Override
  public String toString() {
    return "GeoIpSettingDO [assignId=" + assignId + ", countryId=" + countryId + ", provinceId="
        + provinceId + ", cityId=" + cityId + ", ipAddress=" + ipAddress + "]";
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getCountryId() {
    return countryId;
  }

  public void setCountryId(String countryId) {
    this.countryId = countryId;
  }

  public String getProvinceId() {
    return provinceId;
  }

  public void setProvinceId(String provinceId) {
    this.provinceId = provinceId;
  }

  public String getCityId() {
    return cityId;
  }

  public void setCityId(String cityId) {
    this.cityId = cityId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

}

