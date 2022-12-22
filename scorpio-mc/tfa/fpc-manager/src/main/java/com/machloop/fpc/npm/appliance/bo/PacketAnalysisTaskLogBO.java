package com.machloop.fpc.npm.appliance.bo;

/**
 * @author guosk
 *
 * create at 2022年3月15日, fpc-manager
 */
public class PacketAnalysisTaskLogBO {

  private String id;
  private String taskId;
  private String subTaskId;
  private String status;
  private String content;
  private String ariseTime;

  @Override
  public String toString() {
    return "PacketAnalysisTaskLogBO [id=" + id + ", taskId=" + taskId + ", subTaskId=" + subTaskId
        + ", status=" + status + ", content=" + content + ", ariseTime=" + ariseTime + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getSubTaskId() {
    return subTaskId;
  }

  public void setSubTaskId(String subTaskId) {
    this.subTaskId = subTaskId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getAriseTime() {
    return ariseTime;
  }

  public void setAriseTime(String ariseTime) {
    this.ariseTime = ariseTime;
  }

}
