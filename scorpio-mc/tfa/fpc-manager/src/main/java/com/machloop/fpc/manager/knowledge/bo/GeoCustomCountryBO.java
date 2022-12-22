package com.machloop.fpc.manager.knowledge.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2021年8月25日, fpc-manager
 */
public class GeoCustomCountryBO implements LogAudit {

  private String id;
  private String ipSettingId;
  private String name;
  private String countryId;
  private String longitude;
  private String latitude;
  private String ipAddress;
  private String customCountryInCmsId;
  private String description;

  private String createTime;
  private String updateTime;

  private String operatorId;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加自定义地区：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改自定义地区：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除自定义地区：");
        break;
      default:
        return "";
    }
    builder.append("名称=").append(name).append(";");
    builder.append("地区ID=").append(countryId).append(";");
    builder.append("经度=").append(longitude).append(";");
    builder.append("纬度=").append(latitude).append(";");
    builder.append("IP地址=").append(ipAddress).append(";");
    builder.append("描述=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "GeoCustomCountryBO [id=" + id + ", ipSettingId=" + ipSettingId + ", name=" + name
        + ", countryId=" + countryId + ", longitude=" + longitude + ", latitude=" + latitude
        + ", ipAddress=" + ipAddress + ", customCountryInCmsId=" + customCountryInCmsId
        + ", description=" + description + ", createTime=" + createTime + ", updateTime="
        + updateTime + ", operatorId=" + operatorId + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIpSettingId() {
    return ipSettingId;
  }

  public void setIpSettingId(String ipSettingId) {
    this.ipSettingId = ipSettingId;
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

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
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

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
