package com.machloop.fpc.cms.npm.analysis.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/13 10:38 AM,cms
 * @version 1.0
 */
public class SuricataRuleClasstypeBO implements LogAudit {

  private String id;
  private String assignId;
  private String name;
  private String createTime;
  private String updateTime;

  private int ruleSize;
  private long alertSize;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加suricata规则分类：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改suricata规则分类：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除suricata规则分类：");
        break;
      default:
        return "";
    }
    builder.append("分类名称=").append(name).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "SuricataRuleClasstypeBO [id=" + id + ", assignId=" + assignId + ", name=" + name
        + ", createTime=" + createTime + ", updateTime=" + updateTime + ", ruleSize=" + ruleSize
        + ", alertSize=" + alertSize + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public int getRuleSize() {
    return ruleSize;
  }

  public void setRuleSize(int ruleSize) {
    this.ruleSize = ruleSize;
  }

  public long getAlertSize() {
    return alertSize;
  }

  public void setAlertSize(long alertSize) {
    this.alertSize = alertSize;
  }

}
