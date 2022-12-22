package com.machloop.fpc.manager.appliance.bo;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author chenshimiao
 *
 * create at 2022/8/9 9:05 AM,cms
 * @version 1.0
 */
public class FilterRuleBO implements LogAudit {

  private String id;
  private String name;
  private String tuple;
  private String storageRuleInCmsId;
  private String state;
  private String description;
  private String createTime;
  private String updateTime;
  private String deleteTime;
  private List<String> networkId;
  private Integer priority;

  @Override
  public String toString() {
    return "FilterRuleBO [id=" + id + ", name=" + name + ", tuple=" + tuple
        + ", storageRuleInCmsId=" + storageRuleInCmsId + ", state=" + state + ", description="
        + description + ", createTime=" + createTime + ", updateTime=" + updateTime
        + ", deleteTime=" + deleteTime + ", networkId=" + networkId + ", priority" + priority + "]";
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

  public String getTuple() {
    return tuple;
  }

  public void setTuple(String tuple) {
    this.tuple = tuple;
  }

  public String getStorageRuleInCmsId() {
    return storageRuleInCmsId;
  }

  public void setStorageRuleInCmsId(String storageRuleInCmsId) {
    this.storageRuleInCmsId = storageRuleInCmsId;
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

  public String getDeleteTime() {
    return deleteTime;
  }

  public void setDeleteTime(String deleteTime) {
    this.deleteTime = deleteTime;
  }

  public List<String> getNetworkId() {
    return networkId;
  }

  public void setNetworkId(List<String> networkId) {
    this.networkId = networkId;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加存储过滤规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改存储过滤规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除存储过滤规则：");
        break;
      default:
        return "";
    }
    builder.append("过滤条件名称=").append(name).append(";");
    builder.append("过滤条件为=").append(tuple).append(";");
    builder.append("过滤状态为=").append(state).append(";");
    builder.append("优先级为=").append(priority).append(";");
    builder.append("描述=").append(description).append(";");
    return builder.toString();
  }
}
