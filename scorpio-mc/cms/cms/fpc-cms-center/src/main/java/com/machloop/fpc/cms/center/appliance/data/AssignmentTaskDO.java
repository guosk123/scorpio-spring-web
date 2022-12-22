package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

import java.util.Date;

public class AssignmentTaskDO extends BaseOperateDO {

  private String assignTaskId;
  private Date assignTaskTime;
  private String name;
  private String source;



  private Date filterStartTime;
  private Date filterEndTime;
  private String filterNetworkId;
  private String filterConditionType;
  private String filterTuple;
  private String filterBpf;
  private String filterRaw;

  private String mode;
  private String replayNetif;
  private int replayRate;
  private String replayRateUnit;
  private String forwardAction;
  private String description;

  @Override
  public String toString() {
    return "AssignmentTaskDO{" +
            "assignTaskId='" + assignTaskId + '\'' +
            ", assignTaskTime=" + assignTaskTime +
            ", name='" + name + '\'' +
            ", source='" + source + '\'' +
            ", filterStartTime=" + filterStartTime +
            ", filterEndTime=" + filterEndTime +
            ", filterNetworkId='" + filterNetworkId + '\'' +
            ", filterConditionType='" + filterConditionType + '\'' +
            ", filterTuple='" + filterTuple + '\'' +
            ", filterBpf='" + filterBpf + '\'' +
            ", filterRaw='" + filterRaw + '\'' +
            ", mode='" + mode + '\'' +
            ", replayNetif='" + replayNetif + '\'' +
            ", replayRate=" + replayRate +
            ", replayRateUnit='" + replayRateUnit + '\'' +
            ", forwardAction='" + forwardAction + '\'' +
            ", description='" + description + '\'' +
            '}';
  }


  public String getAssignTaskId() {
    return assignTaskId;
  }

  public void setAssignTaskId(String assignTaskId) {
    this.assignTaskId = assignTaskId;
  }

  public Date getAssignTaskTime() {
    return assignTaskTime;
  }

  public void setAssignTaskTime(Date assignTaskTime) {
    this.assignTaskTime = assignTaskTime;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public Date getFilterStartTime() {
    return filterStartTime;
  }

  public void setFilterStartTime(Date filterStartTime) {
    this.filterStartTime = filterStartTime;
  }

  public Date getFilterEndTime() {
    return filterEndTime;
  }

  public void setFilterEndTime(Date filterEndTime) {
    this.filterEndTime = filterEndTime;
  }

  public String getFilterNetworkId() {
    return filterNetworkId;
  }

  public void setFilterNetworkId(String filterNetworkId) {
    this.filterNetworkId = filterNetworkId;
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

  public String getReplayNetif() {
    return replayNetif;
  }

  public void setReplayNetif(String replayNetif) {
    this.replayNetif = replayNetif;
  }

  public int getReplayRate() {
    return replayRate;
  }

  public void setReplayRate(int replayRate) {
    this.replayRate = replayRate;
  }

  public String getReplayRateUnit() {
    return replayRateUnit;
  }

  public void setReplayRateUnit(String replayRateUnit) {
    this.replayRateUnit = replayRateUnit;
  }

  public String getForwardAction() {
    return forwardAction;
  }

  public void setForwardAction(String forwardAction) {
    this.forwardAction = forwardAction;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
