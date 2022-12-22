package com.machloop.fpc.npm.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2022年3月15日, fpc-manager
 */
public class PacketAnalysisTaskLogDO extends BaseDO {

  private String taskId;
  private String subTaskId;
  private String status;
  private String content;
  private Date ariseTime;

  @Override
  public String toString() {
    return "PacketAnalysisTaskLogDO [taskId=" + taskId + ", subTaskId=" + subTaskId + ", status="
        + status + ", content=" + content + ", ariseTime=" + ariseTime + "]";
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

  public Date getAriseTime() {
    return ariseTime;
  }

  public void setAriseTime(Date ariseTime) {
    this.ariseTime = ariseTime;
  }

}
