package com.machloop.fpc.manager.analysis.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

public class ScenarioTaskBO implements LogAudit {

  private String id;

  private String name;

  private String analysisStartTime;
  private String analysisEndTime;
  private String type;
  private String typeText;
  private String description;
  private String executionStartTime;
  private String executionEndTime;
  private int executionProgress;
  private String executionTrace;
  private String state;

  private String createTime;
  private String updateTime;

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加场景分析任务：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改场景分析任务：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除场景分析任务：");
        break;
      default:
        return "";
    }
    builder.append("任务名称=").append(name).append(";");
    builder.append("分析数据开始时间=").append(analysisStartTime).append(";");
    builder.append("分析数据结束时间=").append(analysisEndTime).append(";");
    builder.append("分析场景类型=").append(typeText).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "ScenarioTaskBO [id=" + id + ", name=" + name + ", analysisStartTime="
        + analysisStartTime + ", analysisEndTime=" + analysisEndTime + ", type=" + type
        + ", typeText=" + typeText + ", description=" + description + ", executionStartTime="
        + executionStartTime + ", executionEndTime=" + executionEndTime + ", executionProgress="
        + executionProgress + ", executionTrace=" + executionTrace + ", state=" + state
        + ", createTime=" + createTime + ", updateTime=" + updateTime + "]";
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

  public String getAnalysisStartTime() {
    return analysisStartTime;
  }

  public void setAnalysisStartTime(String analysisStartTime) {
    this.analysisStartTime = analysisStartTime;
  }

  public String getAnalysisEndTime() {
    return analysisEndTime;
  }

  public void setAnalysisEndTime(String analysisEndTime) {
    this.analysisEndTime = analysisEndTime;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTypeText() {
    return typeText;
  }

  public void setTypeText(String typeText) {
    this.typeText = typeText;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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

  public String getExecutionTrace() {
    return executionTrace;
  }

  public void setExecutionTrace(String executionTrace) {
    this.executionTrace = executionTrace;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }
}
