package com.machloop.fpc.npm.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月15日, fpc-manager
 */
public class NetflowConfigBO implements LogAudit {
  private String id;
  private String deviceName;
  private String netifNo;
  private String alias;
  private String deviceType;
  private String protocolVersion;
  private double netifSpeed;
  private String description;
  private String createTime;
  private String updateTime;

  /**
   * @see com.machloop.alpha.webapp.base.LogAudit#toAuditLogText(int)
   */
  @Override
  public String toAuditLogText(int auditLogAction) {

    StringBuilder builder = new StringBuilder();
    if (StringUtils.isBlank(id)) {
      return "";
    }

    if (auditLogAction == LogHelper.AUDIT_LOG_ACTION_UPDATE) {
      builder.append("修改采集源：");
    } else {
      return "";
    }
    builder.append("设备名=").append(deviceName).append(";");
    builder.append("接口名=").append(netifNo).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "NetflowConfigBO [id=" + id + ", deviceName=" + deviceName + ", netifNo=" + netifNo
        + ", alias=" + alias + ", deviceType=" + deviceType + ", protocolVersion=" + protocolVersion
        + ", netifSpeed=" + netifSpeed + ", description=" + description + ", createTime="
        + createTime + ", updateTime=" + updateTime + "]";
  }

  public double getNetifSpeed() {
    return netifSpeed;
  }

  public void setNetifSpeed(double netifSpeed) {
    this.netifSpeed = netifSpeed;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public String getNetifNo() {
    return netifNo;
  }

  public void setNetifNo(String netifNo) {
    this.netifNo = netifNo;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
