package com.machloop.fpc.manager.analysis.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
public class StandardProtocolBO implements LogAudit {
  private String id;

  private String l7ProtocolId;
  private String ipProtocol;
  private String port;
  private String source;
  private String sourceText;
  private String description;

  private String createTime;
  private String updateTime;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加标准协议配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改标准协议配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除标准协议配置：");
        break;
      default:
        return "";
    }
    builder.append("应用层协议=").append(l7ProtocolId).append(";");
    builder.append("传输层协议=").append(ipProtocol).append(";");
    builder.append("端口=").append(port).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "StandardProtocolBO [id=" + id + ", l7ProtocolId=" + l7ProtocolId + ", ipProtocol="
        + ipProtocol + ", port=" + port + ", source=" + source + ", sourceText=" + sourceText
        + ", description=" + description + ", createTime=" + createTime + ", updateTime="
        + updateTime + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(String l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public String getIpProtocol() {
    return ipProtocol;
  }

  public void setIpProtocol(String ipProtocol) {
    this.ipProtocol = ipProtocol;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getSourceText() {
    return sourceText;
  }

  public void setSourceText(String sourceText) {
    this.sourceText = sourceText;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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
