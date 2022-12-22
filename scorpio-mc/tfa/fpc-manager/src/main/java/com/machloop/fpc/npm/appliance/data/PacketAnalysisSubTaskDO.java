package com.machloop.fpc.npm.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2021年6月16日, fpc-manager
 */
public class PacketAnalysisSubTaskDO extends BaseOperateDO {

  private String name;
  private String taskId;
  private Date packetStartTime;
  private Date packetEndTime;
  private long size;
  private String filePath;
  private String status;
  private String executionTrace;
  private int executionProgress;
  private String executionResult;

  @Override
  public String toString() {
    return "PacketAnalysisSubTaskDO [name=" + name + ", taskId=" + taskId + ", packetStartTime="
        + packetStartTime + ", packetEndTime=" + packetEndTime + ", size=" + size + ", filePath="
        + filePath + ", status=" + status + ", executionTrace=" + executionTrace
        + ", executionProgress=" + executionProgress + ", executionResult=" + executionResult + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public Date getPacketStartTime() {
    return packetStartTime;
  }

  public void setPacketStartTime(Date packetStartTime) {
    this.packetStartTime = packetStartTime;
  }

  public Date getPacketEndTime() {
    return packetEndTime;
  }

  public void setPacketEndTime(Date packetEndTime) {
    this.packetEndTime = packetEndTime;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getExecutionTrace() {
    return executionTrace;
  }

  public void setExecutionTrace(String executionTrace) {
    this.executionTrace = executionTrace;
  }

  public int getExecutionProgress() {
    return executionProgress;
  }

  public void setExecutionProgress(int executionProgress) {
    this.executionProgress = executionProgress;
  }

  public String getExecutionResult() {
    return executionResult;
  }

  public void setExecutionResult(String executionResult) {
    this.executionResult = executionResult;
  }

}
