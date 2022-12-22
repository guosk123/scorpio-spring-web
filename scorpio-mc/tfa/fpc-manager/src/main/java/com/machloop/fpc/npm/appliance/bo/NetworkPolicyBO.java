package com.machloop.fpc.npm.appliance.bo;

import java.util.Date;

/**
 * @author "Minjiajun"
 *
 * create at 2021年12月27日, fpc-manager
 */
public class NetworkPolicyBO {

  private String id;
  private String networkId;
  private String policyType;
  private String policyId;
  private Date timestamp;
  private String networkPolicyInCmsId;
  private String operatorId;

  @Override
  public String toString() {
    return "NetworkPolicyBO [id=" + id + ", networkId=" + networkId + ", policyType=" + policyType
        + ", policyId=" + policyId + ", timestamp=" + timestamp + ", networkPolicyInCmsId="
        + networkPolicyInCmsId + ", operatorId=" + operatorId + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getNetworkPolicyInCmsId() {
    return networkPolicyInCmsId;
  }

  public void setNetworkPolicyInCmsId(String networkPolicyInCmsId) {
    this.networkPolicyInCmsId = networkPolicyInCmsId;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
