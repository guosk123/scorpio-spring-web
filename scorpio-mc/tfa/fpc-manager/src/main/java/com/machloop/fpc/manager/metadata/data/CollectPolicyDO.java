package com.machloop.fpc.manager.metadata.data;

import com.machloop.alpha.common.base.BaseOperateDO;

public class CollectPolicyDO extends BaseOperateDO {

  private String name;
  private int orderNo;
  private String ipAddress;
  private long ipStart;
  private long ipEnd;
  private String l7ProtocolId;
  private String level;
  private String state;

  @Override
  public String toString() {
    return "CollectPolicyDO [name=" + name + ", orderNo=" + orderNo + ", ipAddress=" + ipAddress
        + ", ipStart=" + ipStart + ", ipEnd=" + ipEnd + ", l7ProtocolId=" + l7ProtocolId
        + ", level=" + level + ", state=" + state + "]";
  }

  public int getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(int orderNo) {
    this.orderNo = orderNo;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public long getIpStart() {
    return ipStart;
  }

  public void setIpStart(long ipStart) {
    this.ipStart = ipStart;
  }

  public long getIpEnd() {
    return ipEnd;
  }

  public void setIpEnd(long ipEnd) {
    this.ipEnd = ipEnd;
  }

  public String getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(String l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

}
