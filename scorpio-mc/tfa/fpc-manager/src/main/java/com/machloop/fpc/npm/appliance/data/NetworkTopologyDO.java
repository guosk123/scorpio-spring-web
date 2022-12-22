package com.machloop.fpc.npm.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2021年7月10日, fpc-manager
 */
public class NetworkTopologyDO extends BaseDO {

  private String topology;
  private String metric;
  private Date timestamp;
  private String operatorId;

  @Override
  public String toString() {
    return "NetworkTopologyDO [topology=" + topology + ", metric=" + metric + ", timestamp="
        + timestamp + ", operatorId=" + operatorId + "]";
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
