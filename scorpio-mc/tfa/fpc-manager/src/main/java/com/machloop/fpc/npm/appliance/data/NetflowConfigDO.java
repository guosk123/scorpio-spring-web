package com.machloop.fpc.npm.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月8日, fpc-manager
 */
public class NetflowConfigDO extends BaseOperateDO {
  private String id;
  private String deviceType;
  private String deviceName;
  private String netifNo;
  private String alias;
  private double netifSpeed;
  private String protocolVersion;
  private String description;
  private String operatorId;
  private Date updateTime;

  @Override
  public String toString() {
    return "NetflowConfigDO [id=" + id + ", deviceType=" + deviceType + ", deviceName=" + deviceName
        + ", netifNo=" + netifNo + ", netifSpeed=" + netifSpeed + ", protocolVersion="
        + protocolVersion + ", alias=" + alias + ", description=" + description + ", updateTime="
        + updateTime + ", operatorId=" + operatorId + super.toString() + "]";
  }
  
  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
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

  public double getNetifSpeed() {
    return netifSpeed;
  }

  public void setNetifSpeed(double netifSpeed) {
    this.netifSpeed = netifSpeed;
  }

  public String getProtocolVersion() {
    return protocolVersion;
  }

  public void setProtocolVersion(String protocolVersion) {
    this.protocolVersion = protocolVersion;
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
