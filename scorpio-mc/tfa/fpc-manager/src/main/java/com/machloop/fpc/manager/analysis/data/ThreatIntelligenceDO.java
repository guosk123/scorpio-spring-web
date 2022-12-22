package com.machloop.fpc.manager.analysis.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月16日, fpc-manager
 */
public class ThreatIntelligenceDO extends BaseDO {

  private String type;
  private String content;
  private String threatCategory;
  private String description;
  private Date timestamp;

  @Override
  public String toString() {
    return "ThreatIntelligenceDO [type=" + type + ", content=" + content + ", threatCategory="
        + threatCategory + ", description=" + description + ", timestamp=" + timestamp
        + ", toString()=" + super.toString() + "]";
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

  public String getThreatCategory() {
    return threatCategory;
  }

  public void setThreatCategory(String threatCategory) {
    this.threatCategory = threatCategory;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
}
