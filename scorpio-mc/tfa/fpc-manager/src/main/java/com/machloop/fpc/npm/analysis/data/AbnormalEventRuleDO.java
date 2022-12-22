package com.machloop.fpc.npm.analysis.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2021年7月15日, fpc-manager
 */
public class AbnormalEventRuleDO extends BaseDO {

  private int type;
  private String content;
  private String source;
  private String status;
  private String description;
  private String operatorId;
  private Date timestamp;

  @Override
  public String toString() {
    return "AbnormalEventRuleDO [type=" + type + ", content=" + content + ", source=" + source
        + ", status=" + status + ", description=" + description + ", operatorId=" + operatorId
        + ", timestamp=" + timestamp + "]";
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

}
