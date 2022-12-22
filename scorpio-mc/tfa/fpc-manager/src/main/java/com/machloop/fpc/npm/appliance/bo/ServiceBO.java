package com.machloop.fpc.npm.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2020年11月12日, fpc-manager
 */
public class ServiceBO implements LogAudit {

  private String id;
  private String name;
  private String networkIds;
  private String application;
  private String serviceInCmsId;
  private String description;
  private String createTime;

  private String networkNames;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加业务：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改业务：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除业务：");
        break;
      default:
        return "";
    }
    builder.append("名称=").append(name).append(";");
    builder.append("所在网络ID集合=").append(networkIds).append(";");
    builder.append("应用集合=").append(application).append(";");
    builder.append("描述=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "ServiceBO [id=" + id + ", name=" + name + ", networkIds=" + networkIds
        + ", application=" + application + ", serviceInCmsId=" + serviceInCmsId + ", description="
        + description + ", createTime=" + createTime + ", networkNames=" + networkNames + "]";
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

  public String getNetworkIds() {
    return networkIds;
  }

  public void setNetworkIds(String networkIds) {
    this.networkIds = networkIds;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getServiceInCmsId() {
    return serviceInCmsId;
  }

  public void setServiceInCmsId(String serviceInCmsId) {
    this.serviceInCmsId = serviceInCmsId;
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

  public String getNetworkNames() {
    return networkNames;
  }

  public void setNetworkNames(String networkNames) {
    this.networkNames = networkNames;
  }

}
