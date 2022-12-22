package com.machloop.fpc.cms.center.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

public class AssignmentTaskRecordDO extends BaseDO {

  private String taskId;
  private String fpcTaskId;
  private String fpcSerialNumber;
  private String messageId;
  private String assignmentState;
  private String executionState;
  private String executionTrace;
  private Date executionStartTime;
  private Date executionEndTime;
  private int executionProgress;
  private String executionCachePath;
  private String pcapFileUrl;
  private String operatorId;
  private Date assignmentTime;

  @Override
  public String toString() {
    return "AssignmentTaskRecordDO [taskId=" + taskId + ", fpcTaskId=" + fpcTaskId
        + ", fpcSerialNumber=" + fpcSerialNumber + ", messageId=" + messageId + ", assignmentState="
        + assignmentState + ", executionState=" + executionState + ", executionTrace="
        + executionTrace + ", executionStartTime=" + executionStartTime + ", executionEndTime="
        + executionEndTime + ", executionProgress=" + executionProgress + ", executionCachePath="
        + executionCachePath + ", pcapFileUrl=" + pcapFileUrl + ", operatorId=" + operatorId
        + ", assignmentTime=" + assignmentTime + "]";
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getFpcTaskId() {
    return fpcTaskId;
  }

  public void setFpcTaskId(String fpcTaskId) {
    this.fpcTaskId = fpcTaskId;
  }

  public String getFpcSerialNumber() {
    return fpcSerialNumber;
  }

  public void setFpcSerialNumber(String fpcSerialNumber) {
    this.fpcSerialNumber = fpcSerialNumber;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getAssignmentState() {
    return assignmentState;
  }

  public void setAssignmentState(String assignmentState) {
    this.assignmentState = assignmentState;
  }

  public String getExecutionState() {
    return executionState;
  }

  public void setExecutionState(String executionState) {
    this.executionState = executionState;
  }

  public String getExecutionTrace() {
    return executionTrace;
  }

  public void setExecutionTrace(String executionTrace) {
    this.executionTrace = executionTrace;
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

  public String getPcapFileUrl() {
    return pcapFileUrl;
  }

  public void setPcapFileUrl(String pcapFileUrl) {
    this.pcapFileUrl = pcapFileUrl;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public Date getAssignmentTime() {
    return assignmentTime;
  }

  public void setAssignmentTime(Date assignmentTime) {
    this.assignmentTime = assignmentTime;
  }

}
