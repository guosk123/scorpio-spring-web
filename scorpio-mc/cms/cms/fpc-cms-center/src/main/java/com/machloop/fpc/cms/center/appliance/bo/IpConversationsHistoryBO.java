package com.machloop.fpc.cms.center.appliance.bo;


import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author ChenXiao
 * create at 2022/10/11
 */
public class IpConversationsHistoryBO implements LogAudit {

  private String id;

  @NotEmpty(message = "历史画布名称为空")
  private String name;

  private String data;

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加历史画布：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改历史画布：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除历史画布：");
        break;
      default:
        return "";
    }

    builder.append("名称=").append(name).append(";");
    builder.append("历史画布数据=").append(data).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "IpConversationsHistoryBO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", data='"
        + data + '\'' + '}';
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

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
