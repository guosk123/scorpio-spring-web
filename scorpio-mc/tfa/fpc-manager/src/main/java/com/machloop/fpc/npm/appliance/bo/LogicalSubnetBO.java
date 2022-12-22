package com.machloop.fpc.npm.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public class LogicalSubnetBO implements LogAudit {

  private String id;
  private String name;
  private String networkId;
  private int bandwidth;
  private String type;
  private String configuration;
  private String subnetInCmsId;
  private String description;
  private String createTime;

  private String networkName;
  private String typeText;
  private long totalBandwidth;

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
    builder.append("名称=").append(name).append(";");
    builder.append("所属网络=").append(networkId).append(";");
    builder.append("总带宽=").append(bandwidth).append(";");
    builder.append("子网类型=").append(type).append(";");
    builder.append("子网配置=").append(configuration).append(";");
    builder.append("子网描述=").append(description).append("。");

    return builder.toString();
  }

  @Override
  public String toString() {
    return "LogicalSubnetBO [id=" + id + ", name=" + name + ", networkId=" + networkId
        + ", bandwidth=" + bandwidth + ", type=" + type + ", configuration=" + configuration
        + ", subnetInCmsId=" + subnetInCmsId + ", description=" + description + ", createTime="
        + createTime + ", networkName=" + networkName + ", typeText=" + typeText
        + ", totalBandwidth=" + totalBandwidth + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public int getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
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

  public String getSubnetInCmsId() {
    return subnetInCmsId;
  }

  public void setSubnetInCmsId(String subnetInCmsId) {
    this.subnetInCmsId = subnetInCmsId;
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

  public String getNetworkName() {
    return networkName;
  }

  public void setNetworkName(String networkName) {
    this.networkName = networkName;
  }

  public String getTypeText() {
    return typeText;
  }

  public void setTypeText(String typeText) {
    this.typeText = typeText;
  }

  public long getTotalBandwidth() {
    return totalBandwidth;
  }

  public void setTotalBandwidth(long totalBandwidth) {
    this.totalBandwidth = totalBandwidth;
  }

}
