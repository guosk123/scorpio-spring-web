package com.machloop.fpc.npm.analysis.bo;

import java.util.Date;

/**
 * @author chenshimiao
 *
 * create at 2022/9/8 3:47 PM,cms
 * @version 1.0
 */
public class SuricataRuleDateBO {

  private Date timestamp;
  private int sid;
  private String srcRole;
  private String destRole;
  private String msg;
  private String target;
  private String srcIpv4;
  private String srcIpv6;
  private String destIpv4;
  private String destIpv6;

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
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

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getSrcIpv4() {
    return srcIpv4;
  }

  public void setSrcIpv4(String srcIpv4) {
    this.srcIpv4 = srcIpv4;
  }

  public String getSrcIpv6() {
    return srcIpv6;
  }

  public void setSrcIpv6(String srcIpv6) {
    this.srcIpv6 = srcIpv6;
  }

  public String getDestIpv4() {
    return destIpv4;
  }

  public void setDestIpv4(String destIpv4) {
    this.destIpv4 = destIpv4;
  }

  public String getDestIpv6() {
    return destIpv6;
  }

  public void setDestIpv6(String destIpv6) {
    this.destIpv6 = destIpv6;
  }
}
