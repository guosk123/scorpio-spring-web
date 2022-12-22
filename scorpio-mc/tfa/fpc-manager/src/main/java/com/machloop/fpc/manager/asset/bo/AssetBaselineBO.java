package com.machloop.fpc.manager.asset.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月6日, fpc-manager
 */
public class AssetBaselineBO implements LogAudit {

  private String id;
  private String ipAddress;
  private String type;
  private String baseline;
  private String description;
  private String updateTime;

  @Override
  public String toString() {
    return "AssetBaselineBO [id=" + id + ", ipAddress=" + ipAddress + ", type=" + type
        + ", baseline=" + baseline + ", description=" + description + ", updateTime=" + updateTime
        + "]";
  }

  /**
   * @see com.machloop.alpha.webapp.base.LogAudit#toAuditLogText(int)
   */
  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加资产基线配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改资产基线配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除资产基线配置：");
        break;
      default:
        return "";
    }
    builder.append("ip地址=").append(ipAddress).append("；");
    builder.append("类型=").append(type).append("；");
    builder.append("基线状态=").append(baseline).append("；");
    builder.append("描述=").append(description).append("；");
    builder.append("更新时间=").append(updateTime).append("。");

    return builder.toString();
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getBaseline() {
    return baseline;
  }

  public void setBaseline(String baseline) {
    this.baseline = baseline;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

}
