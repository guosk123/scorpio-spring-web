package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author ChenXiao
 * create at 2022/11/9
 */
public class NatConfigBO implements LogAudit {

  private String id;

  private String natAction;


  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加NAT关联配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改NAT关联配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除NAT关联配置：");
        break;
      default:
        return "";
    }

    builder.append("NAT关联配置状态=")
        .append(StringUtils.equals(natAction, Constants.BOOL_NO) ? "关闭" : "开启").append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "NatConfigBO{" + "id='" + id + '\'' + ", natAction='" + natAction + '\'' + '}';
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNatAction() {
    return natAction;
  }

  public void setNatAction(String natAction) {
    this.natAction = natAction;
  }
}
