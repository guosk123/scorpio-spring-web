package com.machloop.fpc.manager.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public class TransmitTaskDO extends BaseOperateDO {

  private String name;
  private String assignTaskId;
  private Date assignTaskTime;
  private String source;


  private Date filterStartTime;
  private Date filterEndTime;
  private String filterNetworkId;
  private String filterPacketFileId;
  private String filterConditionType;
  private String filterTuple;
  private String filterBpf;
  private String filterRaw;

  private String mode;
  private String replayNetif;
  private int replayRate;
  private String replayRateUnit;
  private String forwardAction;
  private String ipTunnel;

  private String state;
  private String description;

  private Date executionStartTime;
  private Date executionEndTime;
  private int executionProgress;
  private String executionCachePath;
  private String executionDownloadUrl;
  private String executionTrace;
  private Date transferTime;
  private String replayRule;

  @Override
  public String toString() {
    return "TransmitTaskDO{" + "name='" + name + '\'' + ", assignTaskId='" + assignTaskId + '\''
        + ", assignTaskTime=" + assignTaskTime + ", source='" + source + '\'' + ", filterStartTime="
        + filterStartTime + ", filterEndTime=" + filterEndTime + ", filterNetworkId='"
        + filterNetworkId + '\'' + ", filterPacketFileId='" + filterPacketFileId + '\''
        + ", filterConditionType='" + filterConditionType + '\'' + ", filterTuple='" + filterTuple
        + '\'' + ", filterBpf='" + filterBpf + '\'' + ", filterRaw='" + filterRaw + '\''
        + ", mode='" + mode + '\'' + ", replayNetif='" + replayNetif + '\'' + ", replayRate="
        + replayRate + ", replayRateUnit='" + replayRateUnit + '\'' + ", forwardAction='"
        + forwardAction + '\'' + ", ipTunnel='" + ipTunnel + '\'' + ", state='" + state + '\''
        + ", description='" + description + '\'' + ", executionStartTime=" + executionStartTime
        + ", executionEndTime=" + executionEndTime + ", executionProgress=" + executionProgress
        + ", executionCachePath='" + executionCachePath + '\'' + ", executionDownloadUrl='"
        + executionDownloadUrl + '\'' + ", executionTrace='" + executionTrace + '\''
        + ", transferTime=" + transferTime + ", replayRule='" + replayRule + '\'' + '}';
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

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getExecutionStartTime() {
    return executionStartTime;
  }

  public void setExecutionStartTime(Date executionStartTime) {
    this.executionStartTime = executionStartTime;
  }

  public Date getExecutionEndTime() {
    return executionEndTime;
  }

  public void setExecutionEndTime(Date executionEndTime) {
    this.executionEndTime = executionEndTime;
  }

  public int getExecutionProgress() {
    return executionProgress;
  }

  public void setExecutionProgress(int executionProgress) {
    this.executionProgress = executionProgress;
  }

  public String getExecutionCachePath() {
    return executionCachePath;
  }

  public void setExecutionCachePath(String executionCachePath) {
    this.executionCachePath = executionCachePath;
  }

  public String getExecutionDownloadUrl() {
    return executionDownloadUrl;
  }

  public void setExecutionDownloadUrl(String executionDownloadUrl) {
    this.executionDownloadUrl = executionDownloadUrl;
  }

  public String getExecutionTrace() {
    return executionTrace;
  }

  public void setExecutionTrace(String executionTrace) {
    this.executionTrace = executionTrace;
  }

  public Date getTransferTime() {
    return transferTime;
  }

  public void setTransferTime(Date transferTime) {
    this.transferTime = transferTime;
  }
}
