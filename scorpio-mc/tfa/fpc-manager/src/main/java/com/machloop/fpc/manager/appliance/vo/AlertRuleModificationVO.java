package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * @author guosk
 *
 * create at 2020年11月2日, fpc-manager
 */
public class AlertRuleModificationVO {

  @NotEmpty(message = "配置名称不能为空")
  private String name;
  @NotEmpty(message = "配置类型不能为空")
  private String category;
  @Range(min = 0, max = 3, message = "告警级别格式错误")
  @Digits(integer = 1, fraction = 0, message = "告警级别格式错误")
  private String level;
  private String thresholdSettings;
  private String trendSettings;
  private String advancedSettings;
  @NotEmpty(message = "告警触发配置不能为空")
  private String refire;
  private String networkIds;
  private String serviceIds;
  @Length(max = 255, message = "描述信息最多可输入255个字符")
  private String description;
  private String customTimeId;

  @Override
  public String toString() {
    return "AlertRuleModificationVO [name=" + name + ", category=" + category + ", level=" + level
        + ", thresholdSettings=" + thresholdSettings + ", trendSettings=" + trendSettings
        + ", advancedSettings=" + advancedSettings + ", refire=" + refire + ", networkIds="
        + networkIds + ", serviceIds=" + serviceIds + ", description=" + description
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
