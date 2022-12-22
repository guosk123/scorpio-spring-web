package com.machloop.fpc.npm.appliance.vo;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * @author guosk
 *
 * create at 2020年11月12日, fpc-manager
 */
public class NetworkModificationVO {

  @Length(min = 1, max = 30, message = "网络名称不能为空，最多可输入30个字符")
  private String name;
  @Range(min = 0, max = 1, message = "流量方向格式错误")
  @Digits(integer = 1, fraction = 0, message = "流量方向格式错误")
  private String netifType;
  @NotEmpty(message = "网络业务接口不能为空")
  private String netif;
  @NotEmpty(message = "网络额外配置不能为空")
  private String extraSettings;
  private String insideIpAddress;
  @NotEmpty(message = "捕获规则不能为空")
  private String ingestPolicyId;
  @NotEmpty(message = "过滤规则不能为空")
  private String filterRuleIds;

  private String sendPolicyIds;

  @Override
  public String toString() {
    return "NetworkModificationVO{" + "name='" + name + '\'' + ", netifType='" + netifType + '\''
        + ", netif='" + netif + '\'' + ", extraSettings='" + extraSettings + '\''
        + ", insideIpAddress='" + insideIpAddress + '\'' + ", ingestPolicyId='" + ingestPolicyId
        + '\'' + ", filterRuleIds='" + filterRuleIds + '\'' + ", sendPolicyIds='" + sendPolicyIds
        + '\'' + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNetifType() {
    return netifType;
  }

  public void setNetifType(String netifType) {
    this.netifType = netifType;
  }

  public String getNetif() {
    return netif;
  }

  public void setNetif(String netif) {
    this.netif = netif;
  }

  public String getExtraSettings() {
    return extraSettings;
  }

  public void setExtraSettings(String extraSettings) {
    this.extraSettings = extraSettings;
  }

  public String getInsideIpAddress() {
    return insideIpAddress;
  }

  public void setInsideIpAddress(String insideIpAddress) {
    this.insideIpAddress = insideIpAddress;
  }

  public String getIngestPolicyId() {
    return ingestPolicyId;
  }

  public void setIngestPolicyId(String ingestPolicyId) {
    this.ingestPolicyId = ingestPolicyId;
  }

  public String getFilterRuleIds() {
    return filterRuleIds;
  }

  public void setFilterRuleIds(String filterRuleIds) {
    this.filterRuleIds = filterRuleIds;
  }

  public String getSendPolicyIds() {
    return sendPolicyIds;
  }

  public void setSendPolicyIds(String sendPolicyIds) {
    this.sendPolicyIds = sendPolicyIds;
  }
}
