package com.machloop.fpc.cms.center.metric.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author ChenXiao
 * create at 2022/11/25
 */


public class MetricIpDetectionsLayoutsBO implements LogAudit {

  private String id;

  private String layouts;


  @Override
  public String toString() {
    return "MetricIpDetectionsLayoutsBO{" + "id='" + id + '\'' + ", layouts='" + layouts + '\''
        + '}';
  }

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加IP画像界面布局配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改IP画像界面布局配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除IP画像界面布局配置：");
        break;
      default:
        return "";
    }

    builder.append("IP画像界面布局配置=").append(layouts).append("。");
    return builder.toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLayouts() {
    return layouts;
  }

  public void setLayouts(String layouts) {
    this.layouts = layouts;
  }

}

