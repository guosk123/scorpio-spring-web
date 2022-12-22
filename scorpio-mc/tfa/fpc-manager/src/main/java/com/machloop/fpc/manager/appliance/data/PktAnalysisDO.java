package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年4月18日, fpc-manager
 */
public class PktAnalysisDO extends BaseOperateDO {
  private String id;
  private String protocol;
  private String fileName;
  private String parseStatus;
  private String parseLog;
  private String description;

  @Override
  public String toString() {
    return "PktAnalysisDO [id=" + id + ", protocol=" + protocol + ", fileName=" + fileName
        + ", parseStatus=" + parseStatus + ", parseLog=" + parseLog + ", description=" + description
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

}
