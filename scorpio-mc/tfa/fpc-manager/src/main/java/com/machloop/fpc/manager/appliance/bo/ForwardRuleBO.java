package com.machloop.fpc.manager.appliance.bo;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;
import org.apache.commons.lang.StringUtils;

/**
 * @author ChenXiao
 *
 * create at 2022/5/7 16:21,IntelliJ IDEA
 *
 */
public class ForwardRuleBO implements LogAudit {

  private String id;
  private String name;
  private String defaultAction;
  private String exceptBpf;
  private String exceptTuple;

  private String forwardRuleInCmsId;
  private String description;
  private String operatorId;

  private String createTime;
  private String updateTime;

  private String defaultActionText;

  private int referenceCount;



  @Override public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加转发规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改转发规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除转发规则：");
        break;
      default:
        return "";
    }
    builder.append("名称=").append(name).append(";");
    builder.append("默认策略=").append(defaultActionText).append(";");
    builder.append("过滤BPF=").append(exceptBpf).append(";");
    builder.append("过滤条件=").append(exceptTuple).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }

  @Override public String toString() {
    return "ForwardRuleBO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", defaultAction='"
        + defaultAction + '\'' + ", exceptBpf='" + exceptBpf + '\'' + ", exceptTuple='"
        + exceptTuple + '\'' + ", forwardRuleInCmsId='" + forwardRuleInCmsId + '\''
        + ", description='" + description + '\'' + ", operatorId='" + operatorId + '\''
        + ", createTime='" + createTime + '\'' + ", updateTime='" + updateTime + '\''
        + ", defaultActionText='" + defaultActionText + '\'' + ", referenceCount=" + referenceCount
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

  public String getForwardRuleInCmsId() {
    return forwardRuleInCmsId;
  }

  public void setForwardRuleInCmsId(String forwardRuleInCmsId) {
    this.forwardRuleInCmsId = forwardRuleInCmsId;
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

  public int getReferenceCount() {
    return referenceCount;
  }

  public void setReferenceCount(int referenceCount) {
    this.referenceCount = referenceCount;
  }
}
