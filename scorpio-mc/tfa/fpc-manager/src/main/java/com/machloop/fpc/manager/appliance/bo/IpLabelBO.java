package com.machloop.fpc.manager.appliance.bo;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;
import org.apache.commons.lang.StringUtils;

/**
 * @author chenshimiao
 *
 * create at 2022/9/6 11:00 AM,cms
 * @version 1.0
 */
public class IpLabelBO implements LogAudit {

  private String id;
  private String name;
  private String ipAddress;
  private String category;
  private String description;
  private String createTime;
  private String updateTime;
  private String operatorId;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加标签：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改标签：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除标签：");
        break;
      default:
        return "";
    }
    builder.append("名称=").append(name).append(";");
    builder.append("IP地址=").append(ipAddress).append(";");
    builder.append("标签分类=").append(category).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "IpLabelBO [id=" + id + ", name=" + name + ", ipAddress=" + ipAddress + ", category="
        + category + ", description=" + description + ", createTime=" + createTime + ", updateTime="
        + updateTime + ", operatorId=" + operatorId + "]";
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

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
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

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }
}
