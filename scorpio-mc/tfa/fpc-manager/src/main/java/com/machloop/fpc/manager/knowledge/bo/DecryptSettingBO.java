package com.machloop.fpc.manager.knowledge.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月20日, fpc-manager
 */
public class DecryptSettingBO implements LogAudit {
  private String id;

  private String ipAddress;
  private String port;
  private String protocol;
  private String certHash;

  private String createTime;
  private String updateTime;

  private String operatorId;

  @Override
  public String toAuditLogText(int auditLogAction) {

    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加TLS协议私钥配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改TLS协议私钥配置：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除TLS协议私钥配置：");
        break;
      default:
        return "";
    }
    builder.append("IP地址=").append(ipAddress).append(";");
    builder.append("端口=").append(port).append(";");
    builder.append("协议=").append(protocol).append(";");
    builder.append("证书哈希值=").append(certHash).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "DecryptSettingBO [id=" + id + ", ipAddress=" + ipAddress + ", port=" + port
        + ", protocol=" + protocol + ", certHash=" + certHash + ", createTime=" + createTime
        + ", updateTime=" + updateTime + ", operatorId=" + operatorId + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getCertHash() {
    return certHash;
  }

  public void setCertHash(String certHash) {
    this.certHash = certHash;
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

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }
}
