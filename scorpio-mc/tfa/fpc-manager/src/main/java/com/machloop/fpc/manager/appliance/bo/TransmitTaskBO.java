package com.machloop.fpc.manager.appliance.bo;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.common.FpcConstants;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public class TransmitTaskBO implements LogAudit {

  private String id;
  @NotEmpty(message = "任务名称为空")
  private String name;
  private String source;


  @NotEmpty(message = "过滤条件开始时间为空")
  private String filterStartTime;
  @NotEmpty(message = "过滤条件截止时间为空")
  private String filterEndTime;
  private String filterNetworkId;
  private String filterPacketFileId;
  private String filterConditionType;
  private String filterTuple;
  private String filterBpf;
  private String filterRaw;

  @NotEmpty(message = "导出模式为空")
  private String mode;
  private String replayNetif;
  private int replayRate;
  private String replayRateUnit;
  private String replayRule;
  private String forwardAction;
  private String ipTunnel;

  private String state;
  private String description;

  private String executionStartTime;
  private String executionEndTime;
  private int executionProgress;
  private String executionCachePath;
  private String executionTrace;

  private String operatorId;

  private String filterNetworkName;
  private String filterPacketFileName;
  private String modeText;
  private String replayRateUnitText;
  private String forwardActionText;
  private String filterConditionTypeText;


  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
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
      case LogHelper.AUDIT_LOG_ACTION_REDO:
        builder.append("redo查询任务：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_STOP:
        builder.append("停止查询任务：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DOWNLOAD:
        builder.append("下载查询文件：");
        break;
      default:
        return "";
    }
    builder.append("任务名称=").append(name).append(";");
    builder.append("时间范围=").append(filterStartTime).append(" - ").append(filterEndTime).append(";");
    builder.append("过滤网络=").append(filterNetworkId).append(";");
    builder.append("过滤离线文件子任务=").append(filterPacketFileId).append(";");
    if (StringUtils.equals(filterConditionType, FpcConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE)) {
      builder.append("过滤类型=").append("六元组").append(";");
      builder.append("过滤条件=").append(filterTuple).append(";");
    } else if (StringUtils.equals(filterConditionType,
        FpcConstants.TRANSMIT_TASK_FILTER_TYPE_BPF)) {
      builder.append("过滤类型=").append("BPF语法").append(";");
      builder.append("过滤条件=").append(filterBpf).append(";");
    } else if (StringUtils.equals(filterConditionType,
        FpcConstants.TRANSMIT_TASK_FILTER_TYPE_MIX)) {
      builder.append("过滤类型=").append("混合条件").append(";");
      builder.append("过滤条件=").append(filterBpf).append(";").append(filterTuple).append(";");
    }
    builder.append("过滤原始内容=").append(filterRaw).append(";");
    builder.append("导出模式=").append(modeText).append(";");
    builder.append("任务来源=").append(source).append(";");
    if (StringUtils.equals(mode, FpcConstants.TRANSMIT_TASK_MODE_REPLAY)) {
      builder.append("重放接口名=").append(replayNetif).append(";");
      builder.append("重放速率=").append(replayRate).append(replayRateUnitText).append(";");
      builder.append("转发策略=").append(forwardActionText).append(";");
      builder.append("隧道封装=").append(ipTunnel).append(";");
      builder.append("重放规则=").append(replayRule).append(";");
    }
    if (auditLogAction == LogHelper.AUDIT_LOG_ACTION_DOWNLOAD) {
      builder.append("任务执行时间=").append(executionStartTime).append(" - ").append(executionEndTime)
          .append(";");
      builder.append("文件路径=").append(executionCachePath).append(";");
    }
    builder.append("备注=").append(description).append("。");
    return builder.toString();

  }

  @Override
  public String toString() {
    return "TransmitTaskBO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", source='" + source
        + '\'' + ", filterStartTime='" + filterStartTime + '\'' + ", filterEndTime='"
        + filterEndTime + '\'' + ", filterNetworkId='" + filterNetworkId + '\''
        + ", filterPacketFileId='" + filterPacketFileId + '\'' + ", filterConditionType='"
        + filterConditionType + '\'' + ", filterTuple='" + filterTuple + '\'' + ", filterBpf='"
        + filterBpf + '\'' + ", filterRaw='" + filterRaw + '\'' + ", mode='" + mode + '\''
        + ", replayNetif='" + replayNetif + '\'' + ", replayRate=" + replayRate
        + ", replayRateUnit='" + replayRateUnit + '\'' + ", forwardAction='" + forwardAction + '\''
        + ", ipTunnel='" + ipTunnel + '\'' + ", state='" + state + '\'' + ", description='"
        + description + '\'' + ", executionStartTime='" + executionStartTime + '\''
        + ", executionEndTime='" + executionEndTime + '\'' + ", executionProgress="
        + executionProgress + ", executionCachePath='" + executionCachePath + '\''
        + ", executionTrace='" + executionTrace + '\'' + ", operatorId='" + operatorId + '\''
        + ", filterNetworkName='" + filterNetworkName + '\'' + ", filterPacketFileName='"
        + filterPacketFileName + '\'' + ", modeText='" + modeText + '\'' + ", replayRateUnitText='"
        + replayRateUnitText + '\'' + ", forwardActionText='" + forwardActionText + '\''
        + ", filterConditionTypeText='" + filterConditionTypeText + '\'' + ", replayRule='"
        + replayRule + '\'' + '}';
  }

  public String getReplayRule() {
    return replayRule;
  }

  public void setReplayRule(String replayRule) {
    this.replayRule = replayRule;
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

  public String getFilterNetworkId() {
    return filterNetworkId;
  }

  public void setFilterNetworkId(String filterNetworkId) {
    this.filterNetworkId = filterNetworkId;
  }

  public String getFilterPacketFileId() {
    return filterPacketFileId;
  }

  public void setFilterPacketFileId(String filterPacketFileId) {
    this.filterPacketFileId = filterPacketFileId;
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

  public String getIpTunnel() {
    return ipTunnel;
  }

  public void setIpTunnel(String ipTunnel) {
    this.ipTunnel = ipTunnel;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
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

  public String getExecutionCachePath() {
    return executionCachePath;
  }

  public void setExecutionCachePath(String executionCachePath) {
    this.executionCachePath = executionCachePath;
  }

  public String getExecutionTrace() {
    return executionTrace;
  }

  public void setExecutionTrace(String executionTrace) {
    this.executionTrace = executionTrace;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getFilterNetworkName() {
    return filterNetworkName;
  }

  public void setFilterNetworkName(String filterNetworkName) {
    this.filterNetworkName = filterNetworkName;
  }

  public String getFilterPacketFileName() {
    return filterPacketFileName;
  }

  public void setFilterPacketFileName(String filterPacketFileName) {
    this.filterPacketFileName = filterPacketFileName;
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

  public String getFilterConditionTypeText() {
    return filterConditionTypeText;
  }

  public void setFilterConditionTypeText(String filterConditionTypeText) {
    this.filterConditionTypeText = filterConditionTypeText;
  }

}
