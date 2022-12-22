package com.machloop.fpc.cms.center.appliance.bo;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月12日, fpc-cms-center
 */
public class SyslogSendupRuleBO implements LogAudit {

  private String id;
  private String assignId;
  private String name;
  private String syslogServerAddress;
  private String sendType;
  private String sendTime;
  private int interval;
  private int threshold;
  private String severity;
  private String facility;
  private String encodeType;
  private String separator;
  private String networkAlertContent;
  private String serviceAlertContent;
  private String systemAlarmContent;
  private String systemLogContent;
  private String dataSourceFormat;
  private String dataSource;
  private String connectInfo;

  private Date updateTime;

  @Override
  public String toString() {
    return "SyslogSendupRuleBO [id=" + id + ", assignId=" + assignId + ", name=" + name
        + ", syslogServerAddress=" + syslogServerAddress + ", sendType=" + sendType + ", sendTime="
        + sendTime + ", interval=" + interval + ", threshold=" + threshold + ", severity="
        + severity + ", facility=" + facility + ", encodeType=" + encodeType + ", separator="
        + separator + ", networkAlertContent=" + networkAlertContent + ", serviceAlertContent="
        + serviceAlertContent + ", systemAlarmContent=" + systemAlarmContent + ", systemLogContent="
        + systemLogContent + ", dataSourceFormat=" + dataSourceFormat + ", dataSource=" + dataSource
        + ", connectInfo=" + connectInfo + ", updateTime=" + updateTime + "]";
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
        builder.append("添加syslog外发规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改syslog外发规则：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除syslog外发规则：");
        break;
      default:
        return "";
    }

    builder.append("syslog外发规则名=").append(name).append(";");
    builder.append("syslog服务器地址=").append(syslogServerAddress).append(";");
    builder.append("发送方式=").append(sendType).append(";");
    builder.append("发送时间=").append(sendTime).append(";");
    builder.append("发送间隔=").append(interval).append(";");
    builder.append("发送阈值=").append(threshold).append(";");
    builder.append("syslog等级=").append(severity).append(";");
    builder.append("syslog类型=").append(facility).append(";");
    builder.append("编码方式=").append(encodeType).append(";");
    builder.append("分隔符=").append(separator).append(";");
    builder.append("网络告警消息内容=").append(networkAlertContent).append(";");
    builder.append("业务告警消息内容=").append(serviceAlertContent).append(";");
    builder.append("系统告警消息内容=").append(networkAlertContent).append(";");
    builder.append("系统日志消息内容=").append(networkAlertContent).append("。");
    return builder.toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSyslogServerAddress() {
    return syslogServerAddress;
  }

  public void setSyslogServerAddress(String syslogServerAddress) {
    this.syslogServerAddress = syslogServerAddress;
  }

  public String getSendType() {
    return sendType;
  }

  public void setSendType(String sendType) {
    this.sendType = sendType;
  }

  public String getSendTime() {
    return sendTime;
  }

  public void setSendTime(String sendTime) {
    this.sendTime = sendTime;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public int getThreshold() {
    return threshold;
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getFacility() {
    return facility;
  }

  public void setFacility(String facility) {
    this.facility = facility;
  }

  public String getEncodeType() {
    return encodeType;
  }

  public void setEncodeType(String encodeType) {
    this.encodeType = encodeType;
  }

  public String getSeparator() {
    return separator;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }

  public String getNetworkAlertContent() {
    return networkAlertContent;
  }

  public void setNetworkAlertContent(String networkAlertContent) {
    this.networkAlertContent = networkAlertContent;
  }

  public String getServiceAlertContent() {
    return serviceAlertContent;
  }

  public void setServiceAlertContent(String serviceAlertContent) {
    this.serviceAlertContent = serviceAlertContent;
  }

  public String getSystemAlarmContent() {
    return systemAlarmContent;
  }

  public void setSystemAlarmContent(String systemAlarmContent) {
    this.systemAlarmContent = systemAlarmContent;
  }

  public String getSystemLogContent() {
    return systemLogContent;
  }

  public void setSystemLogContent(String systemLogContent) {
    this.systemLogContent = systemLogContent;
  }

  public String getDataSourceFormat() {
    return dataSourceFormat;
  }

  public void setDataSourceFormat(String dataSourceFormat) {
    this.dataSourceFormat = dataSourceFormat;
  }

  public String getDataSource() {
    return dataSource;
  }

  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  public String getConnectInfo() {
    return connectInfo;
  }

  public void setConnectInfo(String connectInfo) {
    this.connectInfo = connectInfo;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

}
