package com.machloop.fpc.manager.restapi.vo;

import java.util.List;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import com.machloop.fpc.npm.appliance.data.NetworkNetifDO;

/**
 * @author guosk
 *
 * create at 2021年6月23日, fpc-manager
 */
public class NetworkVO {

  @Length(min = 1, max = 30, message = "网络名称不能为空，最多可输入30个字符")
  private String name;
  @Digits(integer = 1, fraction = 0, message = "流量方向格式错误")
  @Range(min = 0, max = 1, message = "流量方向格式错误")
  @NotEmpty(message = "流量方向不能为空")
  private String netifType;
  private List<NetworkNetifDO> netif;
  @Digits(integer = 1, fraction = 0, message = "会话详单生成标志格式错误")
  @Range(min = 0, max = 1, message = "会话详单生成标志格式错误")
  @NotEmpty(message = "会话详单生成标志不能为空")
  private String flowlogDefaultAction;
  private String flowlogExceptStatistics;
  private String flowlogExceptStatus;
  @Digits(integer = 1, fraction = 0, message = "应用层协议详单生成标志格式错误")
  @Range(min = 0, max = 1, message = "应用层协议详单生成标志格式错误")
  @NotEmpty(message = "应用层协议详单生成标志不能为空")
  private String metadataDefaultAction;
  @Digits(integer = 1, fraction = 0, message = "区分VLAN建流分析标志格式错误")
  @Range(min = 0, max = 1, message = "区分VLAN建流分析标志格式错误")
  @NotEmpty(message = "区分VLAN建流分析标志不能为空")
  private String sessionVlanAction;
  private String insideIpAddress;
  @NotEmpty(message = "捕获策略不能为空")
  private String ingestPolicyId;
  @NotEmpty(message = "存储过滤策略不能为空")
  private String filterRuleId;

  private List<String> sendPolicyIds;

  @Override
  public String toString() {
    return "NetworkVO{" + "name='" + name + '\'' + ", netifType='" + netifType + '\'' + ", netif="
        + netif + ", flowlogDefaultAction='" + flowlogDefaultAction + '\''
        + ", flowlogExceptStatistics='" + flowlogExceptStatistics + '\'' + ", flowlogExceptStatus='"
        + flowlogExceptStatus + '\'' + ", metadataDefaultAction='" + metadataDefaultAction + '\''
        + ", sessionVlanAction='" + sessionVlanAction + '\'' + ", insideIpAddress='"
        + insideIpAddress + '\'' + ", ingestPolicyId='" + ingestPolicyId + '\'' + ", filterRuleId='"
        + filterRuleId + '\'' + ", sendPolicyIds=" + sendPolicyIds + '}';
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

  public List<NetworkNetifDO> getNetif() {
    return netif;
  }

  public void setNetif(List<NetworkNetifDO> netif) {
    this.netif = netif;
  }

  public String getFlowlogDefaultAction() {
    return flowlogDefaultAction;
  }

  public void setFlowlogDefaultAction(String flowlogDefaultAction) {
    this.flowlogDefaultAction = flowlogDefaultAction;
  }

  public String getFlowlogExceptStatistics() {
    return flowlogExceptStatistics;
  }

  public void setFlowlogExceptStatistics(String flowlogExceptStatistics) {
    this.flowlogExceptStatistics = flowlogExceptStatistics;
  }

  public String getFlowlogExceptStatus() {
    return flowlogExceptStatus;
  }

  public void setFlowlogExceptStatus(String flowlogExceptStatus) {
    this.flowlogExceptStatus = flowlogExceptStatus;
  }

  public String getMetadataDefaultAction() {
    return metadataDefaultAction;
  }

  public void setMetadataDefaultAction(String metadataDefaultAction) {
    this.metadataDefaultAction = metadataDefaultAction;
  }

  public String getSessionVlanAction() {
    return sessionVlanAction;
  }

  public void setSessionVlanAction(String sessionVlanAction) {
    this.sessionVlanAction = sessionVlanAction;
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

  public String getFilterRuleId() {
    return filterRuleId;
  }

  public void setFilterRuleId(String filterRuleId) {
    this.filterRuleId = filterRuleId;
  }

  public List<String> getSendPolicyIds() {
    return sendPolicyIds;
  }

  public void setSendPolicyIds(List<String> sendPolicyIds) {
    this.sendPolicyIds = sendPolicyIds;
  }
}
