package com.machloop.fpc.manager.analysis.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
public class ScenarioTaskDO extends BaseOperateDO {

  private String name;

  private Date analysisStartTime;
  private Date analysisEndTime;
  private String type;
  private String description;
  private Date executionStartTime;
  private Date executionEndTime;
  private int executionProgress;
  private String executionTrace;
  private String state;

  @Override
  public String toString() {
    return "ScenarioTaskDO [name=" + name + ", analysisStartTime=" + analysisStartTime
        + ", analysisEndTime=" + analysisEndTime + ", type=" + type + ", description=" + description
        + ", executionStartTime=" + executionStartTime + ", executionEndTime=" + executionEndTime
        + ", executionProgress=" + executionProgress + ", executionTrace=" + executionTrace
        + ", state=" + state + ", toString()=" + super.toString() + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getAnalysisStartTime() {
    return analysisStartTime;
  }

  public void setAnalysisStartTime(Date analysisStartTime) {
    this.analysisStartTime = analysisStartTime;
  }

  public Date getAnalysisEndTime() {
    return analysisEndTime;
  }

  public void setAnalysisEndTime(Date analysisEndTime) {
    this.analysisEndTime = analysisEndTime;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getExecutionStartTime() {
    return executionStartTime;
  }

  public void setExecutionStartTime(Date executionStartTime) {
    this.executionStartTime = executionStartTime;
  }

  public Date getExecutionEndTime() {
    return executionEndTime;
  }

  public void setExecutionEndTime(Date executionEndTime) {
    this.executionEndTime = executionEndTime;
  }

  public int getExecutionProgress() {
    return executionProgress;
  }

  public void setExecutionProgress(int executionProgress) {
    this.executionProgress = executionProgress;
  }

  public String getExecutionTrace() {
    return executionTrace;
  }

  public void setExecutionTrace(String executionTrace) {
    this.executionTrace = executionTrace;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}
