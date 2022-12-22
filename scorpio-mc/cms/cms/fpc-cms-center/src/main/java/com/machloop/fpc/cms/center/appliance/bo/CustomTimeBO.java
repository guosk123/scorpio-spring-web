package com.machloop.fpc.cms.center.appliance.bo;

/**
 * @author minjiajun
 *
 * create at 2022年7月18日, fpc-cms-center
 */
public class CustomTimeBO {

  private String id;
  private String assignId;
  private String name;
  private String type;
  private String period;
  private String customTimeSetting;

  private String createTime;
  private String operatorId;

  @Override
  public String toString() {
    return "CustomTimeBO [id=" + id + ", assignId=" + assignId + ", name=" + name + ", type=" + type
        + ", period=" + period + ", customTimeSetting=" + customTimeSetting + ", createTime="
        + createTime + ", operatorId=" + operatorId + "]";
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public String getCustomTimeSetting() {
    return customTimeSetting;
  }

  public void setCustomTimeSetting(String customTimeSetting) {
    this.customTimeSetting = customTimeSetting;
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
