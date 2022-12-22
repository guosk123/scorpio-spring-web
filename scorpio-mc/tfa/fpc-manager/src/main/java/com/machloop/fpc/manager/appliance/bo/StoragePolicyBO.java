package com.machloop.fpc.manager.appliance.bo;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

public class StoragePolicyBO implements LogAudit {

  private String id;
  private String compressAction;
  private String encryptAction;
  private String encryptAlgorithm;
  private String operatorId;

  private String compressActionText;
  private String encryptActionText;

  @Override
  public String toAuditLogText(int auditLogAction) {

    StringBuilder builder = new StringBuilder();
    if (auditLogAction == LogHelper.AUDIT_LOG_ACTION_UPDATE) {
      builder.append("修改流量存储设置：");
    }

    builder.append("压缩策略=").append(compressActionText).append(";");
    builder.append("加密策略=").append(encryptActionText).append(";");
    builder.append("加密算法=").append(encryptAlgorithm).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "StoragePolicyBO [id=" + id + ", compressAction=" + compressAction + ", encryptAction="
        + encryptAction + ", encryptAlgorithm=" + encryptAlgorithm + ", operatorId=" + operatorId
        + ", compressActionText=" + compressActionText + ", encryptActionText=" + encryptActionText
        + "]";
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCompressAction() {
    return compressAction;
  }

  public void setCompressAction(String compressAction) {
    this.compressAction = compressAction;
  }

  public String getEncryptAction() {
    return encryptAction;
  }

  public void setEncryptAction(String encryptAction) {
    this.encryptAction = encryptAction;
  }

  public String getEncryptAlgorithm() {
    return encryptAlgorithm;
  }

  public void setEncryptAlgorithm(String encryptAlgorithm) {
    this.encryptAlgorithm = encryptAlgorithm;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getCompressActionText() {
    return compressActionText;
  }

  public void setCompressActionText(String compressActionText) {
    this.compressActionText = compressActionText;
  }

  public String getEncryptActionText() {
    return encryptActionText;
  }

  public void setEncryptActionText(String encryptActionText) {
    this.encryptActionText = encryptActionText;
  }

}
