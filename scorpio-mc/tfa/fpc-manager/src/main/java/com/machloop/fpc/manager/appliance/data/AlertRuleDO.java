package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
public class AlertRuleDO extends BaseOperateDO {

  private String name;
  private String category;
  private String level;
  private String thresholdSettings;
  private String trendSettings;
  private String advancedSettings;
  private String refire;
  private String status;
  private String alertRuleInCmsId;
  private String description;
  private String customTimeId;

  @Override
  public String toString() {
    return "AlertRuleDO [name=" + name + ", category=" + category + ", level=" + level
        + ", thresholdSettings=" + thresholdSettings + ", trendSettings=" + trendSettings
        + ", advancedSettings=" + advancedSettings + ", refire=" + refire + ", status=" + status
        + ", alertRuleInCmsId=" + alertRuleInCmsId + ", description=" + description
        + ", customTimeId=" + customTimeId + "]";
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

}
