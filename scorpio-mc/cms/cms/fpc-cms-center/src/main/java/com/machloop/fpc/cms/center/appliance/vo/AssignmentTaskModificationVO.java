package com.machloop.fpc.cms.center.appliance.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

public class AssignmentTaskModificationVO {

  @Length(min = 1, max = 30, message = "任务名称不能为空，最多可输入30个字符")
  private String name;
  private String filterTuple;
  private String filterRaw;
  @Length(max = 255, message = "描述最多可输入255个字符")
  private String description;

  @NotEmpty(message = "所选探针不能为空")
  private String fpcSerialNumber;

  @Override
  public String toString() {
    return "AssignmentTaskModificationVO [name=" + name + ", filterTuple=" + filterTuple
        + ", filterRaw=" + filterRaw + ", description=" + description + ", fpcSerialNumber="
        + fpcSerialNumber + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFilterTuple() {
    return filterTuple;
  }

  public void setFilterTuple(String filterTuple) {
    this.filterTuple = filterTuple;
  }

  public String getFilterRaw() {
    return filterRaw;
  }

  public void setFilterRaw(String filterRaw) {
    this.filterRaw = filterRaw;
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
