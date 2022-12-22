package com.machloop.fpc.manager.knowledge.bo;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2021年8月25日, fpc-manager
 */
public class GeoIpSettingBO implements LogAudit {

  private String countryId;
  private String provinceId;
  private String cityId;
  private String ipAddress;

  @Override
  public String toAuditLogText(int auditLogAction) {
    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改地区IP：");
        break;
      default:
        return "";
    }

    builder.append("国家ID=").append(countryId).append(";");
    builder.append("省份ID=").append(provinceId).append(";");
    builder.append("城市ID=").append(cityId).append(";");
    builder.append("IP地址=").append(ipAddress).append("。");

    return builder.toString();
  }

  @Override
  public String toString() {
    return "GeoIpSettingBO [countryId=" + countryId + ", provinceId=" + provinceId + ", cityId="
        + cityId + ", ipAddress=" + ipAddress + "]";
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
