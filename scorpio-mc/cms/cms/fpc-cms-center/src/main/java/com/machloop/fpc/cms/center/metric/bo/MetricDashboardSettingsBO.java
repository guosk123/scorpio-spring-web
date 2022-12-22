package com.machloop.fpc.cms.center.metric.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author chenxiao
 * create at 2022/7/15
 */
public class MetricDashboardSettingsBO implements LogAudit {
  private String id;

  private String parameters;

  private String percentParameter;

  private String timeWindowParameter;

  private String operatorId;


  @Override
  public String toString() {
    return "MetricDashboardSettingsBO{" + "id='" + id + '\'' + ", parameters='" + parameters + '\''
        + ", percentParameter='" + percentParameter + '\'' + ", timeWindowParameter='"
        + timeWindowParameter + '\'' + ", operatorId='" + operatorId + '\'' + '}';
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getParameters() {
    return parameters;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  public String getPercentParameter() {
    return percentParameter;
  }

  public void setPercentParameter(String percentParameter) {
    this.percentParameter = percentParameter;
  }

  public String getTimeWindowParameter() {
    return timeWindowParameter;
  }

  public void setTimeWindowParameter(String timeWindowParameter) {
    this.timeWindowParameter = timeWindowParameter;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

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
    String percentParameterText;
    String timeWindowParameterText;
    switch (percentParameter) {
      case "0":
        percentParameterText = "连接成功率";
        break;
      case "1":
        percentParameterText = "客户端重传率";
        break;
      default:
        percentParameterText = "服务端重传率";
    }
    switch (timeWindowParameter) {
      case "0":
        timeWindowParameterText = "流量趋势图";
        break;
      default:
        timeWindowParameterText = "告警分布图";
    }

    builder.append("参数数组=").append(parameters).append(";");
    builder.append("百分比参数=").append(percentParameterText).append(";");
    builder.append("时间窗口参数=").append(timeWindowParameterText).append("。");
    return builder.toString();
  }
}
