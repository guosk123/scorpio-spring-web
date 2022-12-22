package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author ChenXiao
 *
 * create at 2022/5/11 21:11,IntelliJ IDEA
 *
 */
public class ForwardPolicyBO implements LogAudit {

  private String id;
  private String name;
  private String ruleId;
  private String netifName;
  private String ipTunnel;
  private String loadBalance;
  private String forwardPolicyInCmsId;
  private String description;
  private String operatorId;

  private String createTime;
  private String updateTime;
  private String metricTime;

  private String networkId;

  private Double totalBandWidth;

  private String state = "1";

  @Override
  public String toString() {
    return "ForwardPolicyBO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", ruleId='"
        + ruleId + '\'' + ", netifName='" + netifName + '\'' + ", ipTunnel='" + ipTunnel + '\''
        + ", loadBalance='" + loadBalance + '\'' + ", forwardPolicyInCmsId='" + forwardPolicyInCmsId
        + '\'' + ", description='" + description + '\'' + ", operatorId='" + operatorId + '\''
        + ", createTime='" + createTime + '\'' + ", updateTime='" + updateTime + '\''
        + ", metricTime='" + metricTime + '\'' + ", networkId='" + networkId + '\''
        + ", totalBandWidth=" + totalBandWidth + ", state='" + state + '\'' + '}';
  }

  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加转发策略：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改转发策略：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除转发策略：");
        break;
      default:
        return "";
    }

    builder.append("名称=").append(name).append(";");
    builder.append("作用网络=").append(networkId).append(";");
    builder.append("转发接口数组=").append(netifName).append(";");
    builder.append("ipTunnel=").append(ipTunnel).append(";");
    builder.append("负载均衡=").append(loadBalance).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }

  public String getMetricTime() {
    return metricTime;
  }

  public void setMetricTime(String metricTime) {
    this.metricTime = metricTime;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Double getTotalBandWidth() {
    return totalBandWidth;
  }

  public void setTotalBandWidth(Double totalBandWidth) {
    this.totalBandWidth = totalBandWidth;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }


}
