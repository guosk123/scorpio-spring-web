package com.machloop.fpc.cms.center.metadata.vo;

import java.util.List;

public abstract class AbstractLogRecordVO {

  private String policyName;
  private String level;
  private String flowId;
  private List<String> networkId;
  private List<String> serviceId;
  private String applicationId;

  private String srcIp;
  private int srcPort;
  private String destIp;
  private int destPort;

  private String startTime;
  private String endTime;

  @Override
  public String toString() {
    return "AbstractLogRecordVO [policyName=" + policyName + ", level=" + level + ", flowId="
        + flowId + ", networkId=" + networkId + ", serviceId=" + serviceId + ", applicationId="
        + applicationId + ", srcIp=" + srcIp + ", srcPort=" + srcPort + ", destIp=" + destIp
        + ", destPort=" + destPort + ", startTime=" + startTime + ", endTime=" + endTime + "]";
  }

  public String getPolicyName() {
    return policyName;
  }

  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getFlowId() {
    return flowId;
  }

  public void setFlowId(String flowId) {
    this.flowId = flowId;
  }

  public List<String> getNetworkId() {
    return networkId;
  }

  public void setNetworkId(List<String> networkId) {
    this.networkId = networkId;
  }

  public List<String> getServiceId() {
    return serviceId;
  }

  public void setServiceId(List<String> serviceId) {
    this.serviceId = serviceId;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public String getSrcIp() {
    return srcIp;
  }

  public void setSrcIp(String srcIp) {
    this.srcIp = srcIp;
  }

  public int getSrcPort() {
    return srcPort;
  }

  public void setSrcPort(int srcPort) {
    this.srcPort = srcPort;
  }

  public String getDestIp() {
    return destIp;
  }

  public void setDestIp(String destIp) {
    this.destIp = destIp;
  }

  public int getDestPort() {
    return destPort;
  }

  public void setDestPort(int destPort) {
    this.destPort = destPort;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

}
