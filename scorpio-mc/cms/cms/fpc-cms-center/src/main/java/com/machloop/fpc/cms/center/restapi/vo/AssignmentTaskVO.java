package com.machloop.fpc.cms.center.restapi.vo;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import com.machloop.fpc.cms.center.appliance.bo.FilterTupleBO;

public class AssignmentTaskVO {

  @Length(min = 1, max = 30, message = "任务名称不能为空，最多可输入30个字符")
  private String name;
  @NotEmpty(message = "过滤条件开始时间为空")
  private String filterStartTime;
  @NotEmpty(message = "过滤条件截止时间为空")
  private String filterEndTime;
  private String filterConditionType;
  private List<FilterTupleBO> filterTuple;
  @Length(max = 1024, message = "描述长度不在可允许范围内")
  private String filterBpf;
  private List<List<Map<String, String>>> filterRaw;
  @NotEmpty(message = "导出模式为空")
  private String mode;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;
  @NotEmpty(message = "所选探针不能为空")
  private String fpcSerialNumber;

  @Override
  public String toString() {
    return "AssignmentTaskVO [name=" + name + ", filterStartTime=" + filterStartTime
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

  public List<FilterTupleBO> getFilterTuple() {
    return filterTuple;
  }

  public void setFilterTuple(List<FilterTupleBO> filterTuple) {
    this.filterTuple = filterTuple;
  }

  public String getFilterBpf() {
    return filterBpf;
  }

  public void setFilterBpf(String filterBpf) {
    this.filterBpf = filterBpf;
  }

  public List<List<Map<String, String>>> getFilterRaw() {
    return filterRaw;
  }

  public void setFilterRaw(List<List<Map<String, String>>> filterRaw) {
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
