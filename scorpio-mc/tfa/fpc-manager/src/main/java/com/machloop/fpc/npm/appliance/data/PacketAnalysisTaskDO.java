package com.machloop.fpc.npm.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2021年6月16日, fpc-manager
 */
public class PacketAnalysisTaskDO extends BaseOperateDO {

  private String name;
  private String mode;
  private String source;
  private String filePath;
  private String configuration;
  private String executionTrace;
  private String status;

  @Override
  public String toString() {
    return "PacketAnalysisTaskDO [name=" + name + ", mode=" + mode + ", source=" + source
        + ", filePath=" + filePath + ", configuration=" + configuration + ", executionTrace="
        + executionTrace + ", status=" + status + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public String getExecutionTrace() {
    return executionTrace;
  }

  public void setExecutionTrace(String executionTrace) {
    this.executionTrace = executionTrace;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
