package com.machloop.fpc.cms.center.appliance.bo;

/**
 * @author guosk
 *
 * create at 2021年7月10日, fpc-manager
 */
public class NetworkTopologyBO {

  private String id;
  private String topology;
  private String metric;
  private String timestamp;
  private String operatorId;

  @Override
  public String toString() {
    return "NetworkTopologyBO [id=" + id + ", topology=" + topology + ", metric=" + metric
        + ", timestamp=" + timestamp + ", operatorId=" + operatorId + ", getClass()=" + getClass()
        + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTopology() {
    return topology;
  }

  public void setTopology(String topology) {
    this.topology = topology;
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
