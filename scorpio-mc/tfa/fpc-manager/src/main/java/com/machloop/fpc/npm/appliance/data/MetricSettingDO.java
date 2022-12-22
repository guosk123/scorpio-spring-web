package com.machloop.fpc.npm.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2021年4月6日, fpc-manager
 */
public class MetricSettingDO extends BaseDO {

  private String id;
  private String sourceType;
  private String networkId;
  private String serviceId;
  private String packetFileId;
  private String metric;
  private String value;
  private String metricSettingInCmsId;
  private Date updateTime;
  private String operatorId;

  @Override
  public String toString() {
    return "MetricSettingDO [id=" + id + ", sourceType=" + sourceType + ", networkId=" + networkId
        + ", serviceId=" + serviceId + ", packetFileId=" + packetFileId + ", metric=" + metric
        + ", value=" + value + ", metricSettingInCmsId=" + metricSettingInCmsId + ", updateTime="
        + updateTime + ", operatorId=" + operatorId + "]";
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

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
