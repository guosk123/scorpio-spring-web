package com.machloop.fpc.manager.appliance.vo;

/**
 * @author guosk
 *
 * create at 2021年10月25日, fpc-manager
 */
public class TransmitTaskQueryVO {

  private String name;
  private String filterNetworkId;
  private String filterPacketFileId;
  private String filterConditionType;
  private String mode;
  private String state;
  private String source;

  private String sourceType;

  @Override
  public String toString() {
    return "TransmitTaskQueryVO{" +
            "name='" + name + '\'' +
            ", filterNetworkId='" + filterNetworkId + '\'' +
            ", filterPacketFileId='" + filterPacketFileId + '\'' +
            ", filterConditionType='" + filterConditionType + '\'' +
            ", mode='" + mode + '\'' +
            ", state='" + state + '\'' +
            ", source='" + source + '\'' +
            ", sourceType='" + sourceType + '\'' +
            '}';
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFilterNetworkId() {
    return filterNetworkId;
  }

  public void setFilterNetworkId(String filterNetworkId) {
    this.filterNetworkId = filterNetworkId;
  }

  public String getFilterPacketFileId() {
    return filterPacketFileId;
  }

  public void setFilterPacketFileId(String filterPacketFileId) {
    this.filterPacketFileId = filterPacketFileId;
  }

  public String getFilterConditionType() {
    return filterConditionType;
  }

  public void setFilterConditionType(String filterConditionType) {
    this.filterConditionType = filterConditionType;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

}
