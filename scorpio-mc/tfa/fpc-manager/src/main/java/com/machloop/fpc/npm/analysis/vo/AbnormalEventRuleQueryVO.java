package com.machloop.fpc.npm.analysis.vo;

/**
 * @author guosk
 *
 * create at 2021年7月16日, fpc-manager
 */
public class AbnormalEventRuleQueryVO {

  private Integer type;
  private String content;
  private String source;
  private String status;

  @Override
  public String toString() {
    return "AbnormalEventRuleQueryVO [type=" + type + ", content=" + content + ", source=" + source
        + ", status=" + status + "]";
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
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

}
