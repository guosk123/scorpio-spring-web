package com.machloop.fpc.cms.center.restapi.vo;

import java.util.List;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * @author "Minjiajun"
 *
 * create at 2021年10月21日, fpc-cms-center
 */
public class AlertRuleVO {

  @NotEmpty(message = "告警名称不能为空")
  private String name;
  @NotEmpty(message = "告警类型不能为空")
  private String category;
  @Range(min = 0, max = 3, message = "告警级别格式错误")
  @Digits(integer = 1, fraction = 0, message = "告警级别格式错误")
  private String level;
  private ThresholdSettings thresholdSettings;
  private TrendSettings trendSettings;
  private AdvancedSettings advancedSettings;
  private Refire refire;
  private String networkIds;
  private String serviceIds;
  @Length(max = 255, message = "描述信息最多可输入255个字符")
  private String description;
  @Range(min = 0, max = 1, message = "告警状态不合法")
  @Digits(integer = 1, fraction = 0, message = "告警状态不合法")
  private String status;

  @Override
  public String toString() {
    return "AlertRuleVO [name=" + name + ", category=" + category + ", level=" + level
        + ", thresholdSettings=" + thresholdSettings + ", trendSettings=" + trendSettings
        + ", advancedSettings=" + advancedSettings + ", refire=" + refire + ", networkIds="
        + networkIds + ", serviceIds=" + serviceIds + ", description=" + description + ", status="
        + status + "]";
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

  public ThresholdSettings getThresholdSettings() {
    return thresholdSettings;
  }

  public void setThresholdSettings(ThresholdSettings thresholdSettings) {
    this.thresholdSettings = thresholdSettings;
  }

  public TrendSettings getTrendSettings() {
    return trendSettings;
  }

  public void setTrendSettings(TrendSettings trendSettings) {
    this.trendSettings = trendSettings;
  }

  public AdvancedSettings getAdvancedSettings() {
    return advancedSettings;
  }

  public void setAdvancedSettings(AdvancedSettings advancedSettings) {
    this.advancedSettings = advancedSettings;
  }

  public Refire getRefire() {
    return refire;
  }

  public void setRefire(Refire refire) {
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }


  // 阈值告警配置
  public static class ThresholdSettings {
    private Metrics metrics;
    private FireCriteria fireCriteria;

    @Override
    public String toString() {
      return "ThresholdSettings [metrics=" + metrics + ", fireCriteria=" + fireCriteria + "]";
    }

    public Metrics getMetrics() {
      return metrics;
    }

    public void setMetrics(Metrics metrics) {
      this.metrics = metrics;
    }

    public FireCriteria getFireCriteria() {
      return fireCriteria;
    }

    public void setFireCriteria(FireCriteria fireCriteria) {
      this.fireCriteria = fireCriteria;
    }

  }

  // 基线告警配置
  public static class TrendSettings {
    private Metrics metrics;
    private TrendDefine trend;
    private FireCriteria fireCriteria;

    @Override
    public String toString() {
      return "TrendSettings [metrics=" + metrics + ", trend=" + trend + ", fireCriteria="
          + fireCriteria + "]";
    }

    public Metrics getMetrics() {
      return metrics;
    }

    public void setMetrics(Metrics metrics) {
      this.metrics = metrics;
    }

    public TrendDefine getTrend() {
      return trend;
    }

    public void setTrend(TrendDefine trend) {
      this.trend = trend;
    }

    public FireCriteria getFireCriteria() {
      return fireCriteria;
    }

    public void setFireCriteria(FireCriteria fireCriteria) {
      this.fireCriteria = fireCriteria;
    }

  }

  // 组合告警配置
  public static class AdvancedSettings {
    private FilterCondition fireCriteria;
    private int windowSeconds;

    @Override
    public String toString() {
      return "AdvancedSettings [fireCriteria=" + fireCriteria + ", windowSeconds=" + windowSeconds
          + "]";
    }

    public FilterCondition getFireCriteria() {
      return fireCriteria;
    }

    public void setFireCriteria(FilterCondition fireCriteria) {
      this.fireCriteria = fireCriteria;
    }

    public int getWindowSeconds() {
      return windowSeconds;
    }

    public void setWindowSeconds(int windowSeconds) {
      this.windowSeconds = windowSeconds;
    }

  }

  // 组合告警条件
  public static class FilterCondition {
    private String operator; // and | or
    private List<FilterGroup> group;

    @Override
    public String toString() {
      return "FilterCondition [operator=" + operator + ", group=" + group + "]";
    }

    public String getOperator() {
      return operator;
    }

    public void setOperator(String operator) {
      this.operator = operator;
    }

    public List<FilterGroup> getGroup() {
      return group;
    }

    public void setGroup(List<FilterGroup> group) {
      this.group = group;
    }

  }

  // 单组条件配置
  public static class FilterGroup {
    private String operator;
    @JsonInclude(Include.NON_NULL)
    private String alertRef;
    @JsonInclude(Include.NON_NULL)
    private List<FilterGroup> group;

    @Override
    public String toString() {
      return "FilterGroup [operator=" + operator + ", alertRef=" + alertRef + ", group=" + group
          + "]";
    }

    public String getOperator() {
      return operator;
    }

    public void setOperator(String operator) {
      this.operator = operator;
    }

    public String getAlertRef() {
      return alertRef;
    }

    public void setAlertRef(String alertRef) {
      this.alertRef = alertRef;
    }

    public List<FilterGroup> getGroup() {
      return group;
    }

    public void setGroup(List<FilterGroup> group) {
      this.group = group;
    }

  }

  // 告警触发条件
  public static class FireCriteria {
    private int windowSeconds;
    private String calculation;
    private String operator;
    private double operand;

    @Override
    public String toString() {
      return "FireCriteria [windowSeconds=" + windowSeconds + ", calculation=" + calculation
          + ", operator=" + operator + ", operand=" + operand + "]";
    }

    public int getWindowSeconds() {
      return windowSeconds;
    }

    public void setWindowSeconds(int windowSeconds) {
      this.windowSeconds = windowSeconds;
    }

    public String getCalculation() {
      return calculation;
    }

    public void setCalculation(String calculation) {
      this.calculation = calculation;
    }

    public String getOperator() {
      return operator;
    }

    public void setOperator(String operator) {
      this.operator = operator;
    }

    public double getOperand() {
      return operand;
    }

    public void setOperand(double operand) {
      this.operand = operand;
    }

  }

  // 基线定义
  public static class TrendDefine {
    private String weightingModel;
    private String windowingModel;
    private int windowingCount;

    @Override
    public String toString() {
      return "TrendDefine [weightingModel=" + weightingModel + ", windowingModel=" + windowingModel
          + ", windowingCount=" + windowingCount + ", getClass()=" + getClass() + ", hashCode()="
          + hashCode() + ", toString()=" + super.toString() + "]";
    }

    public String getWeightingModel() {
      return weightingModel;
    }

    public void setWeightingModel(String weightingModel) {
      this.weightingModel = weightingModel;
    }

    public String getWindowingModel() {
      return windowingModel;
    }

    public void setWindowingModel(String windowingModel) {
      this.windowingModel = windowingModel;
    }

    public int getWindowingCount() {
      return windowingCount;
    }

    public void setWindowingCount(int windowingCount) {
      this.windowingCount = windowingCount;
    }

  }

  // 组合指标
  public static class Metrics {
    private boolean isRatio = false;
    private Metric numerator;
    private Metric denominator;

    @Override
    public String toString() {
      return "Metrics [isRatio=" + isRatio + ", numerator=" + numerator + ", denominator="
          + denominator + "]";
    }

    public boolean getIsRatio() {
      return isRatio;
    }

    public void setIsRatio(boolean isRatio) {
      this.isRatio = isRatio;
    }

    public Metric getNumerator() {
      return numerator;
    }

    public void setNumerator(Metric numerator) {
      this.numerator = numerator;
    }

    public Metric getDenominator() {
      return denominator;
    }

    public void setDenominator(Metric denominator) {
      this.denominator = denominator;
    }

  }

  // 指标配置
  public static class Metric {
    private String metric;
    private String sourceType;
    private String sourceValue;

    @Override
    public String toString() {
      return "Metric [metric=" + metric + ", sourceType=" + sourceType + ", sourceValue="
          + sourceValue + "]";
    }

    public String getMetric() {
      return metric;
    }

    public void setMetric(String metric) {
      this.metric = metric;
    }

    public String getSourceType() {
      return sourceType;
    }

    public void setSourceType(String sourceType) {
      this.sourceType = sourceType;
    }

    public String getSourceValue() {
      return sourceValue;
    }

    public void setSourceValue(String sourceValue) {
      this.sourceValue = sourceValue;
    }

  }

  // 告警间隔配置
  public static class Refire {
    private String type;
    private int seconds;

    @Override
    public String toString() {
      return "Refire [type=" + type + ", seconds=" + seconds + "]";
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public int getSeconds() {
      return seconds;
    }

    public void setSeconds(int seconds) {
      this.seconds = seconds;
    }

  }

}

