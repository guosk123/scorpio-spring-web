package com.machloop.fpc.cms.center.broker.bo;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月21日, fpc-cms-center
 */
public class FpcNetworkBO {

  private String id;
  private String fpcNetworkId;
  private String fpcNetworkName;
  private int bandwidth;
  private String fpcSerialNumber;

  @Override
  public String toString() {
    return "FpcNetworkBO [id=" + id + ", fpcNetworkId=" + fpcNetworkId + ", fpcNetworkName="
        + fpcNetworkName + ", bandwidth=" + bandwidth + ", fpcSerialNumber=" + fpcSerialNumber
        + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFpcNetworkId() {
    return fpcNetworkId;
  }

  public void setFpcNetworkId(String fpcNetworkId) {
    this.fpcNetworkId = fpcNetworkId;
  }

  public String getFpcNetworkName() {
    return fpcNetworkName;
  }

  public void setFpcNetworkName(String fpcNetworkName) {
    this.fpcNetworkName = fpcNetworkName;
  }

  public int getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
  }

  public String getFpcSerialNumber() {
    return fpcSerialNumber;
  }

  public void setFpcSerialNumber(String fpcSerialNumber) {
    this.fpcSerialNumber = fpcSerialNumber;
  }

}
