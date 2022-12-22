package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
public class AlertRuleBO implements LogAudit {

  private String id;
  private String name;
  private String category;
  private String level;
  private String thresholdSettings;
  private String trendSettings;
  private String advancedSettings;
  private String refire;
  private String status;
  private String networkIds;
  private String serviceIds;
  private String alertRuleInCmsId;
  private String description;
  private String customTimeId;

  private String createTime;
  private String operatorId;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加业务告警配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改业务告警配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除业务告警配置：");
        break;
      default:
        return "";
    }
    builder.append("名称=").append(name).append(";");
    builder.append("类别=").append(category).append(";");
    builder.append("级别=").append(level).append(";");
    builder.append("阈值告警配置=").append(thresholdSettings).append(";");
    builder.append("基线告警配置=").append(trendSettings).append(";");
    builder.append("组合告警配置=").append(advancedSettings).append(";");
    builder.append("告警触发配置=").append(refire).append(";");
    builder.append("启用状态=").append(StringUtils.equals(status, Constants.BOOL_YES) ? "启用" : "禁用");
    builder.append("告警作用网络=").append(networkIds).append(";");
    builder.append("告警作用业务=").append(serviceIds).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "AlertRuleBO [id=" + id + ", name=" + name + ", category=" + category + ", level="
        + level + ", thresholdSettings=" + thresholdSettings + ", trendSettings=" + trendSettings
        + ", advancedSettings=" + advancedSettings + ", refire=" + refire + ", status=" + status
        + ", networkIds=" + networkIds + ", serviceIds=" + serviceIds + ", alertRuleInCmsId="
        + alertRuleInCmsId + ", description=" + description + ", customTimeId=" + customTimeId
        + ", createTime=" + createTime + ", operatorId=" + operatorId + "]";
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

  public String getThresholdSettings() {
    return thresholdSettings;
  }

  public void setThresholdSettings(String thresholdSettings) {
    this.thresholdSettings = thresholdSettings;
  }

  public String getTrendSettings() {
    return trendSettings;
  }

  public void setTrendSettings(String trendSettings) {
    this.trendSettings = trendSettings;
  }

  public String getAdvancedSettings() {
    return advancedSettings;
  }

  public void setAdvancedSettings(String advancedSettings) {
    this.advancedSettings = advancedSettings;
  }

  public String getRefire() {
    return refire;
  }

  public void setRefire(String refire) {
    this.refire = refire;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getNetworkIds() {
    return networkIds;
  }

  public void setNetworkIds(String networkIds) {
    this.networkIds = networkIds;
  }

  public String getServiceIds() {
    return serviceIds;
  }

  public void setServiceIds(String serviceIds) {
    this.serviceIds = serviceIds;
  }

  public String getAlertRuleInCmsId() {
    return alertRuleInCmsId;
  }

  public void setAlertRuleInCmsId(String alertRuleInCmsId) {
    this.alertRuleInCmsId = alertRuleInCmsId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCustomTimeId() {
    return customTimeId;
  }

  public void setCustomTimeId(String customTimeId) {
    this.customTimeId = customTimeId;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
