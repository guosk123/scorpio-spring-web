package com.machloop.fpc.manager.metric.vo;

/**
 * @author chenxiao
 * create at 2022/7/14
 */
public class MetricDashboardSettingsModificationVO {

  private String parameters;

  private String percentParameter;

  private String timeWindowParameter;

  @Override
  public String toString() {
    return "MetricDashboardSettingsModificationVO{" + "parameters='" + parameters + '\''
        + ", percentParameter='" + percentParameter + '\'' + ", timeWindowParameter='"
        + timeWindowParameter + '\'' + '}';
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
}
