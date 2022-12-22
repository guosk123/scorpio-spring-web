package com.machloop.fpc.npm.appliance.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author guosk
 *
 * create at 2022年3月16日, fpc-manager
 */
public class PacketAnalysisTaskCreationVO {

  @NotEmpty(message = "离线分析任务名称不能为空")
  private String name;
  @NotEmpty(message = "离线分析任务模式不能为空")
  private String mode;
  @NotEmpty(message = "离线分析任务所选文件或目录不能为空")
  private String filePath;
  private String configuration;

  private String sendPolicyIds;

  @Override
  public String toString() {
    return "PacketAnalysisTaskCreationVO{" + "name='" + name + '\'' + ", mode='" + mode + '\''
        + ", filePath='" + filePath + '\'' + ", configuration='" + configuration + '\''
        + ", sendPolicyIds='" + sendPolicyIds + '\'' + '}';
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

  public String getSendPolicyIds() {
    return sendPolicyIds;
  }

  public void setSendPolicyIds(String sendPolicyIds) {
    this.sendPolicyIds = sendPolicyIds;
  }
}
