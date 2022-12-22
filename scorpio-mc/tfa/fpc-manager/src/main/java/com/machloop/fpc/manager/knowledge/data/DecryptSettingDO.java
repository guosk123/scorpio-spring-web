package com.machloop.fpc.manager.knowledge.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月20日, fpc-manager
 */
public class DecryptSettingDO extends BaseOperateDO {
  private String ipAddress;
  private String port;
  private String protocol;
  private String certContent;
  private String certHash;

  @Override
  public String toString() {
    return "DecryptSettingDO [ipAddress=" + ipAddress + ", port=" + port + ", protocol=" + protocol
        + ", certContent=" + certContent + ", certHash=" + certHash + ", toString()="
        + super.toString() + "]";
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getCertContent() {
    return certContent;
  }

  public void setCertContent(String certContent) {
    this.certContent = certContent;
  }

  public String getCertHash() {
    return certHash;
  }

  public void setCertHash(String certHash) {
    this.certHash = certHash;
  }
}

