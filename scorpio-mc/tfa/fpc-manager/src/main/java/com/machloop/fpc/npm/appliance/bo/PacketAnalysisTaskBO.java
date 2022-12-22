package com.machloop.fpc.npm.appliance.bo;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2021年6月16日, fpc-manager
 */
public class PacketAnalysisTaskBO implements LogAudit {

  private String id;
  private String name;
  private String mode;
  private String source;
  private String filePath;
  private String configuration;
  private String executionTrace;
  private String status;
  private String createTime;
  private String operatorId;

  private String modeText;
  private String sourceText;
  private String statusText;

  private String sendPolicyIds;

  @Override
  public String toAuditLogText(int auditLogAction) {

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("新建分析任务：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除分析任务：");
        break;
      default:
        return "";
    }

    builder.append("任务名称=").append(name).append(";");
    builder.append("任务模式=").append(mode).append(";");
    builder.append("任务来源=").append(source).append(";");
    builder.append("文件路径=").append(filePath).append(";");
    builder.append("任务配置=").append(configuration).append(";");
    builder.append("外发策略ID=").append(sendPolicyIds).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "PacketAnalysisTaskBO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", mode='"
        + mode + '\'' + ", source='" + source + '\'' + ", filePath='" + filePath + '\''
        + ", configuration='" + configuration + '\'' + ", executionTrace='" + executionTrace + '\''
        + ", status='" + status + '\'' + ", createTime='" + createTime + '\'' + ", operatorId='"
        + operatorId + '\'' + ", modeText='" + modeText + '\'' + ", sourceText='" + sourceText
        + '\'' + ", statusText='" + statusText + '\'' + ", sendPolicyIds='" + sendPolicyIds + '\''
        + '}';
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

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public String getExecutionTrace() {
    return executionTrace;
  }

  public void setExecutionTrace(String executionTrace) {
    this.executionTrace = executionTrace;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public String getModeText() {
    return modeText;
  }

  public void setModeText(String modeText) {
    this.modeText = modeText;
  }

  public String getSourceText() {
    return sourceText;
  }

  public void setSourceText(String sourceText) {
    this.sourceText = sourceText;
  }

  public String getSendPolicyIds() {
    return sendPolicyIds;
  }

  public void setSendPolicyIds(String sendPolicyIds) {
    this.sendPolicyIds = sendPolicyIds;
  }
}
