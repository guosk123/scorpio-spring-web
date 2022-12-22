package com.machloop.fpc.cms.center.appliance.data;

import java.util.Date;
import com.machloop.alpha.common.base.BaseDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public class ServiceLinkDO extends BaseDO {

  private String serviceId;
  private String assignId;
  private String link;
  private String metric;
  private Date timestamp;
  private String operatorId;

  @Override
  public String toString() {
    return "ServiceLinkDO [serviceId=" + serviceId + ", assignId=" + assignId + ", link=" + link
        + ", metric=" + metric + ", timestamp=" + timestamp + ", operatorId=" + operatorId + "]";
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
