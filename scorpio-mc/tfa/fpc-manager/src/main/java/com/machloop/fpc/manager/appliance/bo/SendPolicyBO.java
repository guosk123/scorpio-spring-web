package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
public class SendPolicyBO implements LogAudit {

  private String id;

  private String name;

  private String externalReceiverId;

  private String sendRuleId;

  private String sendPolicyInCmsId;

  private String state;

  private String networkId;

  private String packetAnalysisTaskId;


  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加外发策略：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改外发策略：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除外发策略：");
        break;
      default:
        return "";
    }

    builder.append("外发策略名称=").append(name).append(";");
    builder.append("外发服务器=").append(externalReceiverId).append(";");
    builder.append("外发规则=").append(sendRuleId).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "SendPolicyBO{" + "id='" + id + '\'' + ", name='" + name + '\''
        + ", externalReceiverId='" + externalReceiverId + '\'' + ", sendRuleId='" + sendRuleId
        + '\'' + ", sendPolicyInCmsId='" + sendPolicyInCmsId + '\'' + ", state='" + state + '\''
        + ", networkId='" + networkId + '\'' + ", packetAnalysisTaskId='" + packetAnalysisTaskId
        + '\'' + '}';
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

  public String getExternalReceiverId() {
    return externalReceiverId;
  }

  public void setExternalReceiverId(String externalReceiverId) {
    this.externalReceiverId = externalReceiverId;
  }

  public String getSendRuleId() {
    return sendRuleId;
  }

  public void setSendRuleId(String sendRuleId) {
    this.sendRuleId = sendRuleId;
  }

  public String getSendPolicyInCmsId() {
    return sendPolicyInCmsId;
  }

  public void setSendPolicyInCmsId(String sendPolicyInCmsId) {
    this.sendPolicyInCmsId = sendPolicyInCmsId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getPacketAnalysisTaskId() {
    return packetAnalysisTaskId;
  }

  public void setPacketAnalysisTaskId(String packetAnalysisTaskId) {
    this.packetAnalysisTaskId = packetAnalysisTaskId;
  }
}
