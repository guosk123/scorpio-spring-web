package com.machloop.fpc.cms.center.appliance.bo;

public class AssignmentTaskRecordBO {

  private String taskId;
  private String fpcTaskId;
  private String fpcSerialNumber;
  private String fpcName;
  private String fpcIp;
  private String connectStatus;
  private String connectStatusText;
  private String assignmentState;
  private String assignmentStateText;
  private String executionState;
  private String executionStateText;
  private String executionTrace;
  private String executionStartTime;
  private String executionEndTime;
  private int executionProgress;
  private String executionCachePath;
  private String pcapFileUrl;

  @Override
  public String toString() {
    return "AssignmentTaskRecordBO [taskId=" + taskId + ", fpcTaskId=" + fpcTaskId
        + ", fpcSerialNumber=" + fpcSerialNumber + ", fpcName=" + fpcName + ", fpcIp=" + fpcIp
        + ", connectStatus=" + connectStatus + ", connectStatusText=" + connectStatusText
        + ", assignmentState=" + assignmentState + ", assignmentStateText=" + assignmentStateText
        + ", executionState=" + executionState + ", executionStateText=" + executionStateText
        + ", executionTrace=" + executionTrace + ", executionStartTime=" + executionStartTime
        + ", executionEndTime=" + executionEndTime + ", executionProgress=" + executionProgress
        + ", executionCachePath=" + executionCachePath + ", pcapFileUrl=" + pcapFileUrl + "]";
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

  public String getFpcName() {
    return fpcName;
  }

  public void setFpcName(String fpcName) {
    this.fpcName = fpcName;
  }

  public String getFpcIp() {
    return fpcIp;
  }

  public void setFpcIp(String fpcIp) {
    this.fpcIp = fpcIp;
  }

  public String getConnectStatus() {
    return connectStatus;
  }

  public void setConnectStatus(String connectStatus) {
    this.connectStatus = connectStatus;
  }

  public String getConnectStatusText() {
    return connectStatusText;
  }

  public void setConnectStatusText(String connectStatusText) {
    this.connectStatusText = connectStatusText;
  }

  public String getAssignmentState() {
    return assignmentState;
  }

  public void setAssignmentState(String assignmentState) {
    this.assignmentState = assignmentState;
  }

  public String getAssignmentStateText() {
    return assignmentStateText;
  }

  public void setAssignmentStateText(String assignmentStateText) {
    this.assignmentStateText = assignmentStateText;
  }

  public String getExecutionState() {
    return executionState;
  }

  public void setExecutionState(String executionState) {
    this.executionState = executionState;
  }

  public String getExecutionStateText() {
    return executionStateText;
  }

  public void setExecutionStateText(String executionStateText) {
    this.executionStateText = executionStateText;
  }

  public String getExecutionTrace() {
    return executionTrace;
  }

  public void setExecutionTrace(String executionTrace) {
    this.executionTrace = executionTrace;
  }

  public String getExecutionStartTime() {
    return executionStartTime;
  }

  public void setExecutionStartTime(String executionStartTime) {
    this.executionStartTime = executionStartTime;
  }

  public String getExecutionEndTime() {
    return executionEndTime;
  }

  public void setExecutionEndTime(String executionEndTime) {
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

}
