package com.machloop.fpc.cms.center.system.bo;

public class LicenseItemBO {

  private String serialNo;

  private String fpcId;

  private String fpcIp;
 
  @Override
  public String toString() {
    return "LicenseItemBO [serialNo=" + serialNo + ", fpcId=" + fpcId + ", fpcIp=" + fpcIp + "]";
  }

  public String getSerialNo() {
    return serialNo;
  }

  public void setSerialNo(String serialNo) {
    this.serialNo = serialNo;
  }

  public String getFpcId() {
    return fpcId;
  }

  public void setFpcId(String fpcId) {
    this.fpcId = fpcId;
  }

  public String getFpcIp() {
    return fpcIp;
  }

  public void setFpcIp(String fpcIp) {
    this.fpcIp = fpcIp;
  }
}
