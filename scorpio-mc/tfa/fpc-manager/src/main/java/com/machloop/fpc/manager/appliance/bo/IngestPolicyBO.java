package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public class IngestPolicyBO implements LogAudit {

  private String id;
  private String name;
  private String defaultAction;
  private String exceptBpf;
  private String exceptTuple;
  private String deduplication;
  private String ingestPolicyInCmsId;
  private String description;
  private String operatorId;

  private String createTime;
  private String updateTime;

  private String defaultActionText;
  private String deduplicationText;

  private int referenceCount;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加捕获规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改捕获规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除捕获规则：");
        break;
      default:
        return "";
    }

    builder.append("名称=").append(name).append(";");
    builder.append("默认策略=").append(defaultActionText).append(";");
    builder.append("过滤BPF=").append(exceptBpf).append(";");
    builder.append("过滤条件=").append(exceptTuple).append(";");
    builder.append("报文去重=").append(deduplicationText).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "IngestPolicyBO [id=" + id + ", name=" + name + ", defaultAction=" + defaultAction
        + ", exceptBpf=" + exceptBpf + ", exceptTuple=" + exceptTuple + ", deduplication="
        + deduplication + ", ingestPolicyInCmsId=" + ingestPolicyInCmsId + ", description="
        + description + ", operatorId=" + operatorId + ", createTime=" + createTime
        + ", updateTime=" + updateTime + ", defaultActionText=" + defaultActionText
        + ", deduplicationText=" + deduplicationText + ", referenceCount=" + referenceCount + "]";
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

  public String getDefaultAction() {
    return defaultAction;
  }

  public void setDefaultAction(String defaultAction) {
    this.defaultAction = defaultAction;
  }

  public String getExceptBpf() {
    return exceptBpf;
  }

  public void setExceptBpf(String exceptBpf) {
    this.exceptBpf = exceptBpf;
  }

  public String getExceptTuple() {
    return exceptTuple;
  }

  public void setExceptTuple(String exceptTuple) {
    this.exceptTuple = exceptTuple;
  }

  public String getDeduplication() {
    return deduplication;
  }

  public void setDeduplication(String deduplication) {
    this.deduplication = deduplication;
  }

  public String getIngestPolicyInCmsId() {
    return ingestPolicyInCmsId;
  }

  public void setIngestPolicyInCmsId(String ingestPolicyInCmsId) {
    this.ingestPolicyInCmsId = ingestPolicyInCmsId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
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

  public String getDefaultActionText() {
    return defaultActionText;
  }

  public void setDefaultActionText(String defaultActionText) {
    this.defaultActionText = defaultActionText;
  }

  public String getDeduplicationText() {
    return deduplicationText;
  }

  public void setDeduplicationText(String deduplicationText) {
    this.deduplicationText = deduplicationText;
  }

  public int getReferenceCount() {
    return referenceCount;
  }

  public void setReferenceCount(int referenceCount) {
    this.referenceCount = referenceCount;
  }

}
