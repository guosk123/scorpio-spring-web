package com.machloop.fpc.cms.center.metadata.data;

public class ProtocolArpLogDO extends AbstractLogRecordDO {

  private String srcMac;
  private String destMac;
  private int type;

  @Override
  public String toString() {
    return "ProtocolArpLogDO [srcMac=" + srcMac + ", destMac=" + destMac + ", type=" + type + "]";
  }

  public String getSrcMac() {
    return srcMac;
  }

  public void setSrcMac(String srcMac) {
    this.srcMac = srcMac;
  }

  public String getDestMac() {
    return destMac;
  }

  public void setDestMac(String destMac) {
    this.destMac = destMac;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

}
