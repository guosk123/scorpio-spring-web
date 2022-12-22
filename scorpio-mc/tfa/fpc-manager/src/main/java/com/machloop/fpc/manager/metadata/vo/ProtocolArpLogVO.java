package com.machloop.fpc.manager.metadata.vo;

/**
 * @author guosk
 *
 * create at 2021年7月1日, fpc-manager
 */
public class ProtocolArpLogVO extends AbstractLogRecordVO {

  private String srcMac;
  private String destMac;
  private int type;

  @Override
  public String toString() {
    return "ProtocolArpLogVO [srcMac=" + srcMac + ", destMac=" + destMac + ", type=" + type + "]";
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
