package com.machloop.fpc.manager.system.bo;

import java.util.Date;

/**
 * @author liumeng
 *
 * create at 2018年12月14日, fpc-manager
 */
public class MonitorMetricBO {

  private String metricName;
  private String metricValue;
  private Date metricTime;

  @Override
  public String toString() {
    return "MonitorMetric [metricName=" + metricName + ", metricValue=" + metricValue
        + ", metricTime=" + metricTime + ", toString()=" + super.toString() + "]";
  }

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public String getMetricValue() {
    return metricValue;
  }

  public void setMetricValue(String metricValue) {
    this.metricValue = metricValue;
  }

  public Date getMetricTime() {
    return metricTime;
  }

  public void setMetricTime(Date metricTime) {
    this.metricTime = metricTime;
  }


}
