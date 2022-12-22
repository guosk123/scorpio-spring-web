package com.machloop.fpc.manager.restapi.vo;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

/**
 * @author chenxiao
 * create at 2022/7/7
 */
public class PacketAnalysisTaskRestAPIVO {

  @NotEmpty(message = "离线分析任务名称不能为空")
  private String name;
  @NotEmpty(message = "离线分析任务模式不能为空")
  private String mode;
  @NotEmpty(message = "离线分析任务所选文件或目录不能为空")
  private List<String> filePath;
  private Map<String, Object> configuration;

  private List<String> sendPolicyIds;

  @Override
  public String toString() {
    return "PacketAnalysisTaskRestAPIVO{" + "name='" + name + '\'' + ", mode='" + mode + '\''
        + ", filePath=" + filePath + ", configuration=" + configuration + ", sendPolicyIds="
        + sendPolicyIds + '}';
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

  public List<String> getFilePath() {
    return filePath;
  }

  public void setFilePath(List<String> filePath) {
    this.filePath = filePath;
  }

  public Map<String, Object> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Map<String, Object> configuration) {
    this.configuration = configuration;
  }

  public List<String> getSendPolicyIds() {
    return sendPolicyIds;
  }

  public void setSendPolicyIds(List<String> sendPolicyIds) {
    this.sendPolicyIds = sendPolicyIds;
  }
}
