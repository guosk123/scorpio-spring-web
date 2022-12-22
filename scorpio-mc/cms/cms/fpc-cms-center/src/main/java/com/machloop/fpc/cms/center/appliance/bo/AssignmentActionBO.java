package com.machloop.fpc.cms.center.appliance.bo;

public class AssignmentActionBO {

  private String fpcSerialNumber;
  private String fpcName;
  private String assignmentState;
  private String assignmentStateText;
  private String connectStatus;
  private String connectStatusText;
  private String assignmentTime;

  @Override
  public String toString() {
    return "AssignmentActionBO [fpcSerialNumber=" + fpcSerialNumber + ", fpcName=" + fpcName
        + ", assignmentState=" + assignmentState + ", assignmentStateText=" + assignmentStateText
        + ", connectStatus=" + connectStatus + ", connectStatusText=" + connectStatusText
        + ", assignmentTime=" + assignmentTime + "]";
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

  public String getAssignmentTime() {
    return assignmentTime;
  }

  public void setAssignmentTime(String assignmentTime) {
    this.assignmentTime = assignmentTime;
  }

}
