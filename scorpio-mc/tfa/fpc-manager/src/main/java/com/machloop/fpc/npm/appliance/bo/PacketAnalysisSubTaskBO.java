package com.machloop.fpc.npm.appliance.bo;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2021年6月16日, fpc-manager
 */
public class PacketAnalysisSubTaskBO implements LogAudit {

  private String id;
  private String name;
  private String taskId;
  private long size;
  private String filePath;
  private String status;
  private String packetStartTime;
  private String packetEndTime;
  private String executionTrace;
  private int executionProgress;
  private String executionResult;
  private String createTime;
  private String operatorId;

  private String statusText;

  @Override
  public String toAuditLogText(int auditLogAction) {
    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("上传数据包文件：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除离线分析子任务：");
        break;
      default:
        return "";
    }
    builder.append("文件名称=").append(name).append(";");
    builder.append("主任务ID=").append(taskId).append(";");
    builder.append("数据包内记录数据的时间范围=").append(packetStartTime).append(" - ").append(packetEndTime)
        .append(";");
    builder.append("文件大小（byte）=").append(size).append(";");
    builder.append("文件路径=").append(filePath).append(";");
    builder.append("分析状态=").append(status).append(";");
    builder.append("分析进度=").append(executionProgress).append(";");
    builder.append("分析结果=").append(executionResult).append("。");

    return builder.toString();
  }

  @Override
  public String toString() {
    return "PacketAnalysisSubTaskBO [id=" + id + ", name=" + name + ", taskId=" + taskId + ", size="
        + size + ", filePath=" + filePath + ", status=" + status + ", packetStartTime="
        + packetStartTime + ", packetEndTime=" + packetEndTime + ", executionTrace="
        + executionTrace + ", executionProgress=" + executionProgress + ", executionResult="
        + executionResult + ", createTime=" + createTime + ", operatorId=" + operatorId
        + ", statusText=" + statusText + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getPacketStartTime() {
    return packetStartTime;
  }

  public void setPacketStartTime(String packetStartTime) {
    this.packetStartTime = packetStartTime;
  }

  public String getPacketEndTime() {
    return packetEndTime;
  }

  public void setPacketEndTime(String packetEndTime) {
    this.packetEndTime = packetEndTime;
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

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getStatusText() {
    return statusText;
  }

  public void setStatusText(String statusText) {
    this.statusText = statusText;
  }

}
