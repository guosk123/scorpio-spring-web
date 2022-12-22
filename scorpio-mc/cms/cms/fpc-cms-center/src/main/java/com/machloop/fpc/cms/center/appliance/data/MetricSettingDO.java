package com.machloop.fpc.cms.center.appliance.data;

import java.util.Date;
import com.machloop.alpha.common.base.BaseDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public class MetricSettingDO extends BaseDO {

  private String assignId;
  private String sourceType;
  private String networkId;
  private String serviceId;
  private String packetFileId;
  private String metric;
  private String value;
  private Date updateTime;
  private String operatorId;

  @Override
  public String toString() {
    return "MetricSettingDO [assignId=" + assignId + ", sourceType=" + sourceType + ", networkId="
        + networkId + ", serviceId=" + serviceId + ", packetFileId=" + packetFileId + ", metric="
        + metric + ", value=" + value + ", updateTime=" + updateTime + ", operatorId=" + operatorId
        + "]";
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
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
