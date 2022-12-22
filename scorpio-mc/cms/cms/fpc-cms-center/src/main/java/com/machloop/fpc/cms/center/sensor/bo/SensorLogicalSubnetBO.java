package com.machloop.fpc.cms.center.sensor.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
public class SensorLogicalSubnetBO implements LogAudit {

  private String id;
  private String assignId;
  private String name;
  private String networkInSensorIds;
  private String type;
  private String configuration;
  private int bandwidth;
  private String subnetInCmsId;
  private String operatorId;

  private String networkInSensorNames;

  // 子网所在的的集群节点状态、详情
  private String status;
  private String statusDetail;

  @Override
  public String toString() {
    return "SensorLogicalSubnetBO [id=" + id + ", assignId=" + assignId + ", name=" + name
        + ", networkInSensorIds=" + networkInSensorIds + ", type=" + type + ", configuration="
        + configuration + ", bandwidth=" + bandwidth + ", subnetInCmsId=" + subnetInCmsId
        + ", operatorId=" + operatorId + ", networkInSensorNames=" + networkInSensorNames
        + ", status=" + status + ", statusDetail=" + statusDetail + "]";
  }

  /**
   * @see com.machloop.alpha.webapp.base.LogAudit#toAuditLogText(int)
   */
  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加逻辑子网：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改逻辑子网：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除逻辑子网：");
        break;
      default:
        return "";
    }
    builder.append("子网名称=").append(name).append(";");
    builder.append("子网类型=").append(type).append(";");
    builder.append("配置=").append(configuration).append(";");
    builder.append("所属网络ID=").append(networkInSensorIds).append(";");
    builder.append("带宽=").append(bandwidth).append("。");
    return builder.toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNetworkInSensorIds() {
    return networkInSensorIds;
  }

  public void setNetworkInSensorIds(String networkInSensorIds) {
    this.networkInSensorIds = networkInSensorIds;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public int getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
  }

  public String getSubnetInCmsId() {
    return subnetInCmsId;
  }

  public void setSubnetInCmsId(String subnetInCmsId) {
    this.subnetInCmsId = subnetInCmsId;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getNetworkInSensorNames() {
    return networkInSensorNames;
  }

  public void setNetworkInSensorNames(String networkInSensorNames) {
    this.networkInSensorNames = networkInSensorNames;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatusDetail() {
    return statusDetail;
  }

  public void setStatusDetail(String statusDetail) {
    this.statusDetail = statusDetail;
  }

}
