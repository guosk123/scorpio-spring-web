package com.machloop.fpc.cms.center.appliance.bo;

import java.util.Date;

import com.machloop.alpha.webapp.base.LogAudit;

/**
 * @author "Minjiajun"
 *
 * create at 2021年12月1日, fpc-cms-center
 */
public class NetworkPolicyBO implements LogAudit {

  private String id;
  private String assignId;
  private String networkId;
  private String policyType;
  private String policyId;
  private Date timestamp;
  private String operatorId;

  private String policySource;

  @Override
  public String toString() {
    return "NetworkPolicyBO{" + "id='" + id + '\'' + ", assignId='" + assignId + '\''
        + ", networkId='" + networkId + '\'' + ", policyType='" + policyType + '\'' + ", policyId='"
        + policyId + '\'' + ", timestamp=" + timestamp + ", operatorId='" + operatorId + '\''
        + ", policySource='" + policySource + '\'' + '}';
  }

  /**
   * @see com.machloop.alpha.webapp.base.LogAudit#toAuditLogText(int)
   */
  @Override
  public String toAuditLogText(int auditLogAction) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
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

  public String getPolicySource() {
    return policySource;
  }

  public void setPolicySource(String policySource) {
    this.policySource = policySource;
  }
}
