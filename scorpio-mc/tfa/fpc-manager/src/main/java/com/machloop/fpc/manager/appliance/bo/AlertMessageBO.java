package com.machloop.fpc.manager.appliance.bo;

import com.machloop.alpha.webapp.base.LogAudit;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
public class AlertMessageBO implements LogAudit {

  private String id;
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
  private String solver;
  private String solveTime;
  private String reason;

  @Override
  public String toAuditLogText(int auditLogAction) {
    StringBuilder builder = new StringBuilder();
    builder.append("id:" + id + ";");
    builder.append("告警规则id:" + alertId + ";");
    builder.append("网络id:" + networkId + ";");
    builder.append("业务id:" + serviceId + ";");
    builder.append("告警名称:" + name + ";");
    builder.append("告警类型:" + category + ";");
    builder.append("告警级别:" + level + ";");
    builder.append("状态:" + status + ";");
    builder.append("解决时间:" + solveTime + ";");
    builder.append("处理意见:" + reason + ";");
    builder.append("告警时间:" + ariseTime + ";");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "AlertMessageBO [id=" + id + ", alertId=" + alertId + ", networkId=" + networkId
        + ", serviceId=" + serviceId + ", name=" + name + ", category=" + category + ", level="
        + level + ", alertDefine=" + alertDefine + ", components=" + components + ", ariseTime="
        + ariseTime + ", status=" + status + ", solver=" + solver + ", solveTime=" + solveTime
        + ", reason=" + reason + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getSolver() {
    return solver;
  }

  public void setSolver(String solver) {
    this.solver = solver;
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
