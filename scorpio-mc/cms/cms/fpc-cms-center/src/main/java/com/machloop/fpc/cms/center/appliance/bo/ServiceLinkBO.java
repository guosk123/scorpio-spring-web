package com.machloop.fpc.cms.center.appliance.bo;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月19日, fpc-cms-center
 */
public class ServiceLinkBO {

  private String id;
  private String assignId;
  private String serviceId;
  private String link;
  private String metric;
  private String timestamp;
  private String operatorId;

  @Override
  public String toString() {
    return "ServiceLinkBO [id=" + id + ", assignId=" + assignId + ", serviceId=" + serviceId
        + ", link=" + link + ", metric=" + metric + ", timestamp=" + timestamp + ", operatorId="
        + operatorId + "]";
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

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
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

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
