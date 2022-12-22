package com.machloop.fpc.manager.analysis.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月18日, fpc-manager
 */
public class ThreatIntelligenceModificationVO {

  @NotEmpty(message = "修改威胁情报类型不能为空")
  private String type;
  @NotEmpty(message = "修改威胁情报内容不能为空")
  private String content;

  @Override
  public String toString() {
    return "ThreatIntelligenceModificationVO [type=" + type + ", content=" + content + "]";
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

}
