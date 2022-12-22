package com.machloop.fpc.manager.metric.data;

import java.util.Date;

/**
 * @author chenxiao
 * create at 2022/7/14
 */
public class MetricDashboardSettingsDO {

  private String id;

  private String parameters;

  private String percentParameter;

  private String timeWindowParameter;

  private Date createTime;

  private Date updateTime;

  private String operatorId;


  @Override
  public String toString() {
    return "MetricDashboardSettingsDO{" + "id='" + id + '\'' + ", parameters='" + parameters + '\''
        + ", percentParameter='" + percentParameter + '\'' + ", timeWindowParameter='"
        + timeWindowParameter + '\'' + ", createTime=" + createTime + ", updateTime=" + updateTime
        + ", operatorId='" + operatorId + '\'' + '}';
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

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
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
