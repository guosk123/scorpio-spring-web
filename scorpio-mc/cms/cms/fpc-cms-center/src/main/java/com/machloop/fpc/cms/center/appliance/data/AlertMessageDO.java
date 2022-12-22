package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
public class AlertMessageDO extends BaseDO {

  private String alertId;
  private String networkId;
  private String serviceId;
  private String name;
  private String category;
  private String level;
  private String alertDefine;
  private String components;
  private String ariseTime;

  private String status;
  private String solverId;
  private String solveTime;
  private String reason;

  @Override
  public String toString() {
    return "AlertMessageDO [alertId=" + alertId + ", networkId=" + networkId + ", serviceId="
        + serviceId + ", name=" + name + ", category=" + category + ", level=" + level
        + ", alertDefine=" + alertDefine + ", components=" + components + ", ariseTime=" + ariseTime
        + ", status=" + status + ", solverId=" + solverId + ", solveTime=" + solveTime + ", reason="
        + reason + "]";
  }

  public String getAlertId() {
    return alertId;
  }

  public void setAlertId(String alertId) {
    this.alertId = alertId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getAlertDefine() {
    return alertDefine;
  }

  public void setAlertDefine(String alertDefine) {
    this.alertDefine = alertDefine;
  }

  public String getComponents() {
    return components;
  }

  public void setComponents(String components) {
    this.components = components;
  }

  public String getAriseTime() {
    return ariseTime;
  }

  public void setAriseTime(String ariseTime) {
    this.ariseTime = ariseTime;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getSolverId() {
    return solverId;
  }

  public void setSolverId(String solverId) {
    this.solverId = solverId;
  }

  public String getSolveTime() {
    return solveTime;
  }

  public void setSolveTime(String solveTime) {
    this.solveTime = solveTime;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

}
