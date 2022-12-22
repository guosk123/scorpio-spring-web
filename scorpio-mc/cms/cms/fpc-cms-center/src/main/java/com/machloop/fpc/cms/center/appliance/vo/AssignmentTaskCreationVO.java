package com.machloop.fpc.cms.center.appliance.vo;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

public class AssignmentTaskCreationVO {

  @Length(min = 1, max = 30, message = "任务名称不能为空，最多可输入30个字符")
  private String name;
  @NotEmpty(message = "过滤条件开始时间为空")
  private String filterStartTime;
  @NotEmpty(message = "过滤条件截止时间为空")
  private String filterEndTime;
  @Range(min = 0, max = 2, message = "过滤语法类型不合法")
  @Digits(integer = 1, fraction = 0, message = "过滤语法类型不合法")
  private String filterConditionType = "2";
  private String filterTuple;
  private String filterBpf;
  private String filterRaw;
  @NotEmpty(message = "导出模式不能为空")
  private String mode;
  @Length(max = 255, message = "描述最多可输入255个字符")
  private String description;

  @NotEmpty(message = "所选探针不能为空")
  private String fpcSerialNumber;

  @Override
  public String toString() {
    return "AssignmentTaskCreationVO [name=" + name + ", filterStartTime=" + filterStartTime
        + ", filterEndTime=" + filterEndTime + ", filterConditionType=" + filterConditionType
        + ", filterTuple=" + filterTuple + ", filterBpf=" + filterBpf + ", filterRaw=" + filterRaw
        + ", mode=" + mode + ", description=" + description + ", fpcSerialNumber=" + fpcSerialNumber
        + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFilterStartTime() {
    return filterStartTime;
  }

  public void setFilterStartTime(String filterStartTime) {
    this.filterStartTime = filterStartTime;
  }

  public String getFilterEndTime() {
    return filterEndTime;
  }

  public void setFilterEndTime(String filterEndTime) {
    this.filterEndTime = filterEndTime;
  }

  public String getFilterConditionType() {
    return filterConditionType;
  }

  public void setFilterConditionType(String filterConditionType) {
    this.filterConditionType = filterConditionType;
  }

  public String getFilterTuple() {
    return filterTuple;
  }

  public void setFilterTuple(String filterTuple) {
    this.filterTuple = filterTuple;
  }

  public String getFilterBpf() {
    return filterBpf;
  }

  public void setFilterBpf(String filterBpf) {
    this.filterBpf = filterBpf;
  }

  public String getFilterRaw() {
    return filterRaw;
  }

  public void setFilterRaw(String filterRaw) {
    this.filterRaw = filterRaw;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFpcSerialNumber() {
    return fpcSerialNumber;
  }

  public void setFpcSerialNumber(String fpcSerialNumber) {
    this.fpcSerialNumber = fpcSerialNumber;
  }

}
