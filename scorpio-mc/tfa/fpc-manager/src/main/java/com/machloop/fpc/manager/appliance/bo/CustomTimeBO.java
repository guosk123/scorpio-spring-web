package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang3.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
public class CustomTimeBO implements LogAudit {

  private String id;
  private String name;
  private String type;
  private String period;
  private String customTimeSetting;

  private String createTime;
  private String operatorId;
  
  private String customTimeInCmsId;

  @Override
  public String toString() {
    return "CustomTimeBO [id=" + id + ", name=" + name + ", type=" + type + ", period=" + period
        + ", customTimeSetting=" + customTimeSetting + ", createTime=" + createTime
        + ", operatorId=" + operatorId + ", customTimeInCmsId=" + customTimeInCmsId + "]";
  }

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加自定义时间：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改自定义时间：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除自定义时间：");
        break;
      default:
        return "";
    }

    builder.append("自定义时间名称=").append(name).append(";");
    builder.append("自定义时间类型=").append(StringUtils.equals("0", type) ? "周期性时间" : "一次性时间")
        .append(";");
    builder.append("星期=").append(period).append(";");
    builder.append("自定义时间配置=").append(customTimeSetting).append(";");
    return builder.toString();
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public String getCustomTimeSetting() {
    return customTimeSetting;
  }

  public void setCustomTimeSetting(String customTimeSetting) {
    this.customTimeSetting = customTimeSetting;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getCustomTimeInCmsId() {
    return customTimeInCmsId;
  }

  public void setCustomTimeInCmsId(String customTimeInCmsId) {
    this.customTimeInCmsId = customTimeInCmsId;
  }

}
