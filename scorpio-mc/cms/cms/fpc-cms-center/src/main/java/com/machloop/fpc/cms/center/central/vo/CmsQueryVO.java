package com.machloop.fpc.cms.center.central.vo;

public class CmsQueryVO {

  private String ip;
  private String name;
  private String licenseStatus;
  private String connectStatus;

  @Override
  public String toString() {
    return "CmsQueryVO [ip=" + ip + ", name=" + name + ", licenseStatus=" + licenseStatus
        + ", connectStatus=" + connectStatus + "]";
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLicenseStatus() {
    return licenseStatus;
  }

  public void setLicenseStatus(String licenseStatus) {
    this.licenseStatus = licenseStatus;
  }

  public String getConnectStatus() {
    return connectStatus;
  }

  public void setConnectStatus(String connectStatus) {
    this.connectStatus = connectStatus;
  }

}
