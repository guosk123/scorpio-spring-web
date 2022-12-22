package com.machloop.fpc.cms.center.metadata.vo;

import java.util.Map;

/**
 * @author chenshimiao
 *
 * create at 2022/8/1 10:59 AM,cms
 * @version 1.0
 */
public class ProtocolLdapLogVO extends AbstractLogRecordVO {

  private int opType;

  private int resStatus;

  private Map<String, String> reqContent;

  private Map<String, String> resContent;

  @Override
  public String toString() {
    return "ProtocolIdapLogVO [" + "opType=" + opType + ", resStatus='" + resStatus
        + ", reqContent=" + reqContent + ", resContent=" + resContent + "]";
  }

  public int getOpType() {
    return opType;
  }

  public void setOpType(int opType) {
    this.opType = opType;
  }

  public int getResStatus() {
    return resStatus;
  }

  public void setResStatus(int resStatus) {
    this.resStatus = resStatus;
  }

  public Map<String, String> getReqContent() {
    return reqContent;
  }

  public void setReqContent(Map<String, String> reqContent) {
    this.reqContent = reqContent;
  }

  public Map<String, String> getResContent() {
    return resContent;
  }

  public void setResContent(Map<String, String> resContent) {
    this.resContent = resContent;
  }
}