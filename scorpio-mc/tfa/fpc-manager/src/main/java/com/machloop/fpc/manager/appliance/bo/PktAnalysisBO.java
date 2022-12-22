package com.machloop.fpc.manager.appliance.bo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author "Minjiajun"
 *
 * create at 2022年4月18日, fpc-manager
 */
public class PktAnalysisBO implements LogAudit{

  private String id;
  private String protocol;
  private String fileName;
  private String parseStatus;
  private String parseLog;
  private String description;
  private String createTime;
  private String updateTime;
  private String operatorId;

  /**
   * @see com.machloop.alpha.webapp.base.LogAudit#toAuditLogText(int)
   */
  @Override
  public String toAuditLogText(int auditLogAction) {
    
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_SAVE:
        builder.append("添加数据包在线分析脚本：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改数据包在线分析脚本：");
        break;
      case LogHelper.AUDIT_LOG_ACTION_DELETE:
        builder.append("删除数据包在线分析脚本：");
        break;
      default:
        return "";
    }
    builder.append("脚本名称=").append(fileName).append(";");
    builder.append("协议=").append(protocol).append(";");
    builder.append("备注=").append(description).append("。");
    return builder.toString();
  }
  
  @Override
  public String toString() {
    return "PktAnalysisBO [id=" + id + ", protocol=" + protocol + ", fileName=" + fileName
        + ", parseStatus=" + parseStatus + ", parseLog=" + parseLog + ", description=" + description
        + ", createTime=" + createTime + ", updateTime=" + updateTime + ", operatorId=" + operatorId
        + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getParseStatus() {
    return parseStatus;
  }

  public void setParseStatus(String parseStatus) {
    this.parseStatus = parseStatus;
  }

  public String getParseLog() {
    return parseLog;
  }

  public void setParseLog(String parseLog) {
    this.parseLog = parseLog;
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

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
