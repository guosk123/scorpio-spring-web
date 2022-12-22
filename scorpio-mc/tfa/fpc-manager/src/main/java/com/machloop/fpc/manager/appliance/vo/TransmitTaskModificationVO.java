package com.machloop.fpc.manager.appliance.vo;

import org.hibernate.validator.constraints.Length;

/**
 * @author liyongjun
 *
 * create at 2020年3月5日, fpc-manager
 */
public class TransmitTaskModificationVO {

  @Length(min = 1, max = 30, message = "任务名称不能为空，最多可输入30个字符")
  private String name;
  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;
  private String filterTuple;
  private String filterRaw;
  private String replayNetif;
  private int replayRate;
  private String replayRateUnit;
  private String ipTunnel;
  private String replayRule;
  private String filterPacketFileId;

  @Override
  public String toString() {
    return "TransmitTaskModificationVO{" + "name='" + name + '\'' + ", description='" + description
        + '\'' + ", filterTuple='" + filterTuple + '\'' + ", filterRaw='" + filterRaw + '\''
        + ", replayNetif='" + replayNetif + '\'' + ", replayRate=" + replayRate
        + ", replayRateUnit='" + replayRateUnit + '\'' + ", ipTunnel='" + ipTunnel + '\''
        + ", replayRule='" + replayRule + '\'' + ", filterPacketFileId='" + filterPacketFileId
        + '\'' + '}';
  }

  public String getFilterPacketFileId() {
    return filterPacketFileId;
  }

  public void setFilterPacketFileId(String filterPacketFileId) {
    this.filterPacketFileId = filterPacketFileId;
  }

  public String getReplayRule() {
    return replayRule;
  }

  public void setReplayRule(String replayRule) {
    this.replayRule = replayRule;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFilterTuple() {
    return filterTuple;
  }

  public void setFilterTuple(String filterTuple) {
    this.filterTuple = filterTuple;
  }

  public String getFilterRaw() {
    return filterRaw;
  }

  public void setFilterRaw(String filterRaw) {
    this.filterRaw = filterRaw;
  }

  public String getReplayNetif() {
    return replayNetif;
  }

  public void setReplayNetif(String replayNetif) {
    this.replayNetif = replayNetif;
  }

  public int getReplayRate() {
    return replayRate;
  }

  public void setReplayRate(int replayRate) {
    this.replayRate = replayRate;
  }

  public String getReplayRateUnit() {
    return replayRateUnit;
  }

  public void setReplayRateUnit(String replayRateUnit) {
    this.replayRateUnit = replayRateUnit;
  }

  public String getIpTunnel() {
    return ipTunnel;
  }

  public void setIpTunnel(String ipTunnel) {
    this.ipTunnel = ipTunnel;
  }
}
