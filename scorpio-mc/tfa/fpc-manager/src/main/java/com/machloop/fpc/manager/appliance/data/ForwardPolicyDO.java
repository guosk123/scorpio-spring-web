package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/11 21:27,IntelliJ IDEA
 *
 */
public class ForwardPolicyDO extends BaseOperateDO {

  private String name;
  private String ruleId;
  private String netifName;
  private String ipTunnel;
  private String loadBalance;
  private String forwardPolicyInCmsId;
  private String description;

  private String state="1";

  @Override public String toString() {
    return "ForwardPolicyDO{" + "name='" + name + '\'' + ", ruleId='" + ruleId + '\''
        + ", netifName='" + netifName + '\'' + ", ipTunnel='" + ipTunnel + '\'' + ", loadBalance='"
        + loadBalance + '\'' + ", forwardPolicyInCmsId='" + forwardPolicyInCmsId + '\''
        + ", description='" + description + '\'' + ", state='" + state + '\'' + '}';
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRuleId() {
    return ruleId;
  }

  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
  }

  public String getNetifName() {
    return netifName;
  }

  public void setNetifName(String netifName) {
    this.netifName = netifName;
  }

  public String getIpTunnel() {
    return ipTunnel;
  }

  public void setIpTunnel(String ipTunnel) {
    this.ipTunnel = ipTunnel;
  }

  public String getLoadBalance() {
    return loadBalance;
  }

  public void setLoadBalance(String loadBalance) {
    this.loadBalance = loadBalance;
  }

  public String getForwardPolicyInCmsId() {
    return forwardPolicyInCmsId;
  }

  public void setForwardPolicyInCmsId(String forwardPolicyInCmsId) {
    this.forwardPolicyInCmsId = forwardPolicyInCmsId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
