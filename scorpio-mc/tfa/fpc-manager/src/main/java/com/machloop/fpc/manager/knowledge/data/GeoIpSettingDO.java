package com.machloop.fpc.manager.knowledge.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2021年8月25日, fpc-manager
 */
public class GeoIpSettingDO extends BaseOperateDO {

  private String countryId;
  private String provinceId;
  private String cityId;
  private String ipAddress;
  private String geoipSettingInCmsId;

  @Override
  public String toString() {
    return "GeoIpSettingDO [countryId=" + countryId + ", provinceId=" + provinceId + ", cityId="
        + cityId + ", ipAddress=" + ipAddress + ", geoipSettingInCmsId=" + geoipSettingInCmsId
        + "]";
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

  public String getGeoipSettingInCmsId() {
    return geoipSettingInCmsId;
  }

  public void setGeoipSettingInCmsId(String geoipSettingInCmsId) {
    this.geoipSettingInCmsId = geoipSettingInCmsId;
  }

}
