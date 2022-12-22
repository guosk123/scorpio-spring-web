package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

public class TransmitTaskCreationVO {

  @Length(min = 1, max = 30, message = "任务名称不能为空，最多可输入30个字符")
  private String name;
  @NotEmpty(message = "过滤条件开始时间为空")
  private String filterStartTime;
  @NotEmpty(message = "过滤条件截止时间为空")
  private String filterEndTime;
  private String filterNetworkId;
  private String filterPacketFileId;
  private String filterConditionType;
  private String filterTuple;
  @Length(max = 1024, message = "描述长度不在可允许范围内")
  private String filterBpf;
  private String filterRaw;

  @NotEmpty(message = "导出模式为空")
  private String mode;
  private String replayNetif;
  private int replayRate;
  private String replayRateUnit;
  private String forwardAction;
  private String ipTunnel;

  private String replayRule;

  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  @Override
  public String toString() {
    return "TransmitTaskCreationVO{" + "name='" + name + '\'' + ", filterStartTime='"
        + filterStartTime + '\'' + ", filterEndTime='" + filterEndTime + '\''
        + ", filterNetworkId='" + filterNetworkId + '\'' + ", filterPacketFileId='"
        + filterPacketFileId + '\'' + ", filterConditionType='" + filterConditionType + '\''
        + ", filterTuple='" + filterTuple + '\'' + ", filterBpf='" + filterBpf + '\''
        + ", filterRaw='" + filterRaw + '\'' + ", mode='" + mode + '\'' + ", replayNetif='"
        + replayNetif + '\'' + ", replayRate=" + replayRate + ", replayRateUnit='" + replayRateUnit
        + '\'' + ", forwardAction='" + forwardAction + '\'' + ", ipTunnel='" + ipTunnel + '\''
        + ", replayRule='" + replayRule + '\'' + ", description='" + description + '\'' + '}';
  }

  public String getReplayRule() {
    return replayRule;
  }

  public void setReplayRule(String replayRule) {
    this.replayRule = replayRule;
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

  public String getFilterNetworkId() {
    return filterNetworkId;
  }

  public void setFilterNetworkId(String filterNetworkId) {
    this.filterNetworkId = filterNetworkId;
  }

  public String getFilterPacketFileId() {
    return filterPacketFileId;
  }

  public void setFilterPacketFileId(String filterPacketFileId) {
    this.filterPacketFileId = filterPacketFileId;
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

  public String getIpTunnel() {
    return ipTunnel;
  }

  public void setIpTunnel(String ipTunnel) {
    this.ipTunnel = ipTunnel;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
