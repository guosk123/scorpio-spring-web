package com.machloop.fpc.cms.center.appliance.vo;

import java.util.Date;

import javax.validation.constraints.NotEmpty;

/**
 * @author "Minjiajun"
 *
 * create at 2021年12月16日, fpc-cms-center
 */
public class NetworkPolicyCreationVO {

  @NotEmpty(message = "网络id不能为空")
  private String networkId;
  @NotEmpty(message = "策略类型不能为空")
  private String policyType;
  @NotEmpty(message = "策略id不能为空")
  private String policyId;
  private Date timestamp;
  private String operatorId;

  @Override
  public String toString() {
    return "NetworkPolicyCreationVO [networkId=" + networkId + ", policyType=" + policyType
        + ", policyId=" + policyId + ", timestamp=" + timestamp + ", operatorId=" + operatorId
        + "]";
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
}
