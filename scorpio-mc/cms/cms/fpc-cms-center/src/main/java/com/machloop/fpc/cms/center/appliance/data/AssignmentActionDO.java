package com.machloop.fpc.cms.center.appliance.data;

import java.util.Date;

public class AssignmentActionDO {

  private String id;
  private String fpcSerialNumber;
  private String messageId;
  private String assignmentId;
  private String taskPolicyId;
  private String state;
  private String type;
  private String action;
  private Date assignmentTime;

  @Override
  public String toString() {
    return "AssignmentActionDO [id=" + id + ", fpcSerialNumber=" + fpcSerialNumber + ", messageId="
        + messageId + ", assignmentId=" + assignmentId + ", taskPolicyId=" + taskPolicyId
        + ", state=" + state + ", type=" + type + ", action=" + action + ", assignmentTime="
        + assignmentTime + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getAssignmentId() {
    return assignmentId;
  }

  public void setAssignmentId(String assignmentId) {
    this.assignmentId = assignmentId;
  }

  public String getTaskPolicyId() {
    return taskPolicyId;
  }

  public void setTaskPolicyId(String taskPolicyId) {
    this.taskPolicyId = taskPolicyId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Date getAssignmentTime() {
    return assignmentTime;
  }

  public void setAssignmentTime(Date assignmentTime) {
    this.assignmentTime = assignmentTime;
  }

}
