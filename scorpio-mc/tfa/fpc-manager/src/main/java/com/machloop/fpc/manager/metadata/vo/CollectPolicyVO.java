package com.machloop.fpc.manager.metadata.vo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.common.FpcConstants;

public class CollectPolicyVO implements LogAudit {

  private String id;
  private String name;
  private int orderNo;
  private String ipAddress;
  private String l7ProtocolId;
  private String level;
  private String state;

  private String operatorId;

  @Override
  public String toAuditLogText(int auditLogAction) {
    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加采集策略：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改采集策略：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除采集策略：");
        break;
      default:
        return "";
    }
    builder.append("策略名称=").append(name).append(";");
    builder.append("策略序号=").append(orderNo).append(";");
    builder.append("采集IP/IP段=").append(ipAddress).append(";");
    builder.append("协议ID=").append(l7ProtocolId).append(";");
    builder.append("级别=");
    switch (level) {
      case FpcConstants.LEVEL_LOW:
        builder.append("低");
        break;
      case FpcConstants.LEVEL_MIDDLE:
        builder.append("中");
        break;
      case FpcConstants.LEVEL_HIGH:
        builder.append("高");
        break;
      default:
        builder.append("");
    }
    builder.append(";");
    builder.append("是否启用=").append(StringUtils.equals(state, Constants.BOOL_NO) ? "未启用" : "启用")
        .append(";");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "CollectPolicyVO [id=" + id + ", name=" + name + ", orderNo=" + orderNo + ", ipAddress="
        + ipAddress + ", l7ProtocolId=" + l7ProtocolId + ", level=" + level + ", state=" + state
        + ", operatorId=" + operatorId + "]";
  }

  public int getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(int orderNo) {
    this.orderNo = orderNo;
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

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(String l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
