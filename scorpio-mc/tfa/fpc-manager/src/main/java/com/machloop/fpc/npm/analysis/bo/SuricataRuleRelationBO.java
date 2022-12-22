package com.machloop.fpc.npm.analysis.bo;

/**
 * @author chenshimiao
 *
 * create at 2022/8/31 2:52 PM,cms
 * @version 1.0
 */
public class SuricataRuleRelationBO {

  private String timestamp;
  private int sid;
  private String srcRole;
  private String destRole;
  private String msg;

  @Override
  public String toString() {
    return "SuricataRuleRelation[" + "timestamp='" + timestamp + ", sid=" + sid + ", attackRole='"
        + srcRole + ", targetRole='" + destRole + ", msg='" + msg + ']';
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public int getSid() {
    return sid;
  }

  public void setSid(int sid) {
    this.sid = sid;
  }

  public String getSrcRole() {
    return srcRole;
  }

  public void setSrcRole(String srcRole) {
    this.srcRole = srcRole;
  }

  public String getDestRole() {
    return destRole;
  }

  public void setDestRole(String destRole) {
    this.destRole = destRole;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }
}
