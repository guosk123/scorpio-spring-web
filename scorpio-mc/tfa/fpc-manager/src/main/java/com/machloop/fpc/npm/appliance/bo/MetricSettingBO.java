package com.machloop.fpc.npm.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2021年4月6日, fpc-manager
 */
public class MetricSettingBO implements LogAudit {

  private String id;
  private String sourceType;
  private String networkId;
  private String serviceId;
  private String packetFileId;
  private String metric;
  private String value;
  private String metricSettingInCmsId;
  private String updateTime;
  private String operatorId;

  @Override
  public String toString() {
    return "MetricSettingBO [id=" + id + ", sourceType=" + sourceType + ", networkId=" + networkId
        + ", serviceId=" + serviceId + ", packetFileId=" + packetFileId + ", metric=" + metric
        + ", value=" + value + ", metricSettingInCmsId=" + metricSettingInCmsId + ", updateTime="
        + updateTime + ", operatorId=" + operatorId + "]";
  }

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加统计指标：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改统计指标：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除统计指标：");
        break;
      default:
        return "";
    }
    builder.append("数据源=").append(sourceType).append(";");
    builder.append("所属网络=").append(networkId).append(";");
    builder.append("所属业务=").append(serviceId).append(";");
    builder.append("指标=").append(metric).append(";");
    builder.append("参数值值=").append(value).append(";");


    return builder.toString();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getPacketFileId() {
    return packetFileId;
  }

  public void setPacketFileId(String packetFileId) {
    this.packetFileId = packetFileId;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getMetricSettingInCmsId() {
    return metricSettingInCmsId;
  }

  public void setMetricSettingInCmsId(String metricSettingInCmsId) {
    this.metricSettingInCmsId = metricSettingInCmsId;
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
