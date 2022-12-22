package com.machloop.fpc.manager.appliance.vo;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * @author ChenXiao
 *
 * create at 2022/5/11 20:00,IntelliJ IDEA
 *
 */
public class ForwardPolicyCreationVO {

  @Length(min = 1, max = 30, message = "策略名称不能为空，最多可输入30个字符")
  private String name;

  @NotEmpty(message = "实时转发规则不能为空")
  private String ruleId;

  @NotEmpty(message = "转发接口选项不能为空")
  private String netifName;

  private String ipTunnel;

  private String loadBalance;

  @Length(max = 255, message = "描述长度不在可允许范围内")
  private String description;

  private String networkId;

  @Override
  public String toString() {
    return "ForwardPolicyCreationVO{" + "name='" + name + '\'' + ", ruleId='" + ruleId + '\''
        + ", netifName='" + netifName + '\'' + ", ipTunnel='" + ipTunnel + '\'' + ", loadBalance='"
        + loadBalance + '\'' + ", description='" + description + '\'' + ", networkId='" + networkId
        + '\'' + '}';
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
