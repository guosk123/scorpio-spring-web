package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author liumeng
 *
 * create at 2018年12月12日, fpc-manager
 */
public class HostInsideBO implements LogAudit {

  private String id;

  private String ipAddress;
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
        builder.append("添加内部网络：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改内部网络：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除内部网络：");
        break;
      default:
        return "";
    }
    builder.append("IP地址=").append(ipAddress).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();

  }

  @Override
  public String toString() {
    return "HostInsideBO [id=" + id + ", ipAddress=" + ipAddress + ", description=" + description
        + ", createTime=" + createTime + ", updateTime=" + updateTime + ", operatorId=" + operatorId
        + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
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
