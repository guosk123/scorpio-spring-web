package com.machloop.fpc.npm.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public class PacketAnalysisTaskPolicyDO extends BaseDO {


  private String packetAnalysisTaskId;
  private String policyType;
  private String policyId;
  private Date timestamp;
  private String operatorId;

  @Override
  public String toString() {
    return "PacketAnalysisTaskPolicyDO{" + "packetAnalysisTaskId='" + packetAnalysisTaskId + '\''
        + ", policyType='" + policyType + '\'' + ", policyId='" + policyId + '\'' + ", timestamp="
        + timestamp + ", operatorId='" + operatorId + '\'' + '}';
  }

  public String getPacketAnalysisTaskId() {
    return packetAnalysisTaskId;
  }

  public void setPacketAnalysisTaskId(String packetAnalysisTaskId) {
    this.packetAnalysisTaskId = packetAnalysisTaskId;
  }

  public String getPolicyType() {
    return policyType;
  }

  public void setPolicyType(String policyType) {
    this.policyType = policyType;
  }

  public String getPolicyId() {
    return policyId;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }
}
