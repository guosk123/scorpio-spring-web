package com.machloop.fpc.cms.center.appliance.bo;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.cms.center.CenterConstants;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

public class AssignmentTaskBO implements LogAudit {

  private String id;
  @NotEmpty(message = "任务名称为空")
  private String name;
  private String source;


  @NotEmpty(message = "过滤条件开始时间为空")
  private String filterStartTime;
  @NotEmpty(message = "过滤条件截止时间为空")
  private String filterEndTime;
  private String filterNetworkId;
  private String filterConditionType;
  private String filterTuple;
  private String filterBpf;
  private String filterRaw;

  private String mode;
  private String replayNetif;
  private int replayRate;
  private String replayRateUnit;
  private String forwardAction;

  private String description;

  private String executionStartTime;
  private String executionEndTime;
  private int executionProgress;

  private String operatorId;

  private String assignTaskId;
  private Date assignTaskTime;

  private String filterConditionTypeText;
  private String modeText;
  private String replayRateUnitText;
  private String forwardActionText;

  // assignment
  private String fpcSerialNumber;

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();

    String actionName = "";
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加查询任务：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改查询任务：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除查询任务：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DOWNLOAD:
        builder.append("下载任务PCAP文件：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_ASSIGNMENT:
        actionName = "下发";
        builder.append("下发任务:");
        break;
      case LogHelper.AUDIT_LOG_ACTION_STOP:
        actionName = "停止下发";
        builder.append("停止下发任务:");
        break;
      case LogHelper.AUDIT_LOG_ACTION_CONTINUE:
        actionName = "继续下发";
        builder.append("继续下发任务:");
        break;
      case LogHelper.AUDIT_LOG_ACTION_CANCEL:
        actionName = "取消下发";
        builder.append("取消下发任务:");
        break;
      default:
        return "";
    }
    builder.append("任务名称=").append(name).append(";");
    builder.append("时间范围=").append(filterStartTime).append(" - ").append(filterEndTime).append(";");
    if (StringUtils.equals(filterConditionType, CenterConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE)) {
      builder.append("过滤类型=").append("六元组").append(";");
      builder.append("过滤条件=").append(filterTuple).append(";");
    } else if (StringUtils.equals(filterConditionType,
        CenterConstants.TRANSMIT_TASK_FILTER_TYPE_BPF)) {
      builder.append("过滤类型=").append("BPF语法").append(";");
      builder.append("过滤条件=").append(filterBpf).append(";");
    } else if (StringUtils.equals(filterConditionType,
        CenterConstants.TRANSMIT_TASK_FILTER_TYPE_MIX)) {
      builder.append("过滤类型=").append("混合条件").append(";");
      builder.append("过滤条件=").append(filterBpf).append(";").append(filterTuple).append(";");
    }
    builder.append("原始内容过滤=").append(filterRaw).append(";");
    builder.append("导出模式=").append(modeText).append(";");
    builder.append("任务来源=").append(source).append(";");
    if (StringUtils.equals(mode, CenterConstants.TRANSMIT_TASK_MODE_REPLAY)) {
      builder.append("重放接口名=").append(replayNetif).append(";");
      builder.append("重放速率=").append(replayRate).append(replayRateUnitText).append(";");
      builder.append("转发策略=").append(forwardActionText).append(";");
    }
    if (auditLogAction == LogHelper.AUDIT_LOG_ACTION_DOWNLOAD) {
      builder.append("任务执行时间=").append(executionStartTime).append(" - ").append(executionEndTime)
          .append(";");
    }
    builder.append(actionName).append("设备集合=[").append(fpcSerialNumber).append("];");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "AssignmentTaskBO{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", source='" + source + '\'' +
            ", filterStartTime='" + filterStartTime + '\'' +
            ", filterEndTime='" + filterEndTime + '\'' +
            ", filterNetworkId='" + filterNetworkId + '\'' +
            ", filterConditionType='" + filterConditionType + '\'' +
            ", filterTuple='" + filterTuple + '\'' +
            ", filterBpf='" + filterBpf + '\'' +
            ", filterRaw='" + filterRaw + '\'' +
            ", mode='" + mode + '\'' +
            ", replayNetif='" + replayNetif + '\'' +
            ", replayRate=" + replayRate +
            ", replayRateUnit='" + replayRateUnit + '\'' +
            ", forwardAction='" + forwardAction + '\'' +
            ", description='" + description + '\'' +
            ", executionStartTime='" + executionStartTime + '\'' +
            ", executionEndTime='" + executionEndTime + '\'' +
            ", executionProgress=" + executionProgress +
            ", operatorId='" + operatorId + '\'' +
            ", assignTaskId='" + assignTaskId + '\'' +
            ", assignTaskTime=" + assignTaskTime +
            ", filterConditionTypeText='" + filterConditionTypeText + '\'' +
            ", modeText='" + modeText + '\'' +
            ", replayRateUnitText='" + replayRateUnitText + '\'' +
            ", forwardActionText='" + forwardActionText + '\'' +
            ", fpcSerialNumber='" + fpcSerialNumber + '\'' +
            '}';
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

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getFilterStartTime() {
    return filterStartTime;
  }

  public void setFilterStartTime(String filterStartTime) {
    this.filterStartTime = filterStartTime;
  }

  public String getFilterEndTime() {
    return filterEndTime;
  }

  public void setFilterEndTime(String filterEndTime) {
    this.filterEndTime = filterEndTime;
  }

  public String getFilterNetworkId() {
    return filterNetworkId;
  }

  public void setFilterNetworkId(String filterNetworkId) {
    this.filterNetworkId = filterNetworkId;
  }

  public String getFilterConditionType() {
    return filterConditionType;
  }

  public void setFilterConditionType(String filterConditionType) {
    this.filterConditionType = filterConditionType;
  }

  public String getFilterTuple() {
    return filterTuple;
  }

  public void setFilterTuple(String filterTuple) {
    this.filterTuple = filterTuple;
  }

  public String getFilterBpf() {
    return filterBpf;
  }

  public void setFilterBpf(String filterBpf) {
    this.filterBpf = filterBpf;
  }

  public String getFilterRaw() {
    return filterRaw;
  }

  public void setFilterRaw(String filterRaw) {
    this.filterRaw = filterRaw;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getReplayNetif() {
    return replayNetif;
  }

  public void setReplayNetif(String replayNetif) {
    this.replayNetif = replayNetif;
  }

  public int getReplayRate() {
    return replayRate;
  }

  public void setReplayRate(int replayRate) {
    this.replayRate = replayRate;
  }

  public String getReplayRateUnit() {
    return replayRateUnit;
  }

  public void setReplayRateUnit(String replayRateUnit) {
    this.replayRateUnit = replayRateUnit;
  }

  public String getForwardAction() {
    return forwardAction;
  }

  public void setForwardAction(String forwardAction) {
    this.forwardAction = forwardAction;
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

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getAssignTaskId() {
    return assignTaskId;
  }

  public void setAssignTaskId(String assignTaskId) {
    this.assignTaskId = assignTaskId;
  }

  public Date getAssignTaskTime() {
    return assignTaskTime;
  }

  public void setAssignTaskTime(Date assignTaskTime) {
    this.assignTaskTime = assignTaskTime;
  }

  public String getFilterConditionTypeText() {
    return filterConditionTypeText;
  }

  public void setFilterConditionTypeText(String filterConditionTypeText) {
    this.filterConditionTypeText = filterConditionTypeText;
  }

  public String getModeText() {
    return modeText;
  }

  public void setModeText(String modeText) {
    this.modeText = modeText;
  }

  public String getReplayRateUnitText() {
    return replayRateUnitText;
  }

  public void setReplayRateUnitText(String replayRateUnitText) {
    this.replayRateUnitText = replayRateUnitText;
  }

  public String getForwardActionText() {
    return forwardActionText;
  }

  public void setForwardActionText(String forwardActionText) {
    this.forwardActionText = forwardActionText;
  }

  public String getFpcSerialNumber() {
    return fpcSerialNumber;
  }

  public void setFpcSerialNumber(String fpcSerialNumber) {
    this.fpcSerialNumber = fpcSerialNumber;
  }

}
