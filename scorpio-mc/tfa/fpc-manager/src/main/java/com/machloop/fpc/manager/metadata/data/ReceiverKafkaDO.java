package com.machloop.fpc.manager.metadata.data;

import com.machloop.alpha.common.base.BaseOperateDO;

public class ReceiverKafkaDO extends BaseOperateDO {

  private String name;
  private String receiverAddress;
  private String kerberosCertification;
  private String keytabFilePath;
  private int keyRestoreTime;
  private String saslKerberosServiceName;
  private String saslKerberosPrincipal;
  private String securityProtocol;
  private String authenticationMechanism;
  private String state;

  @Override
  public String toString() {
    return "FdrReceiverKafkaDO [name=" + name + ", receiverAddress=" + receiverAddress
        + ", kerberosCertification=" + kerberosCertification + ", keytabFilePath=" + keytabFilePath
        + ", keyRestoreTime=" + keyRestoreTime + ", saslKerberosServiceName="
        + saslKerberosServiceName + ", saslKerberosPrincipal=" + saslKerberosPrincipal
        + ", securityProtocol=" + securityProtocol + ", authenticationMechanism="
        + authenticationMechanism + ", state=" + state + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getReceiverAddress() {
    return receiverAddress;
  }

  public void setReceiverAddress(String receiverAddress) {
    this.receiverAddress = receiverAddress;
  }

  public String getKerberosCertification() {
    return kerberosCertification;
  }

  public void setKerberosCertification(String kerberosCertification) {
    this.kerberosCertification = kerberosCertification;
  }

  public String getKeytabFilePath() {
    return keytabFilePath;
  }

  public void setKeytabFilePath(String keytabFilePath) {
    this.keytabFilePath = keytabFilePath;
  }

  public int getKeyRestoreTime() {
    return keyRestoreTime;
  }

  public void setKeyRestoreTime(int keyRestoreTime) {
    this.keyRestoreTime = keyRestoreTime;
  }

  public String getSaslKerberosServiceName() {
    return saslKerberosServiceName;
  }

  public void setSaslKerberosServiceName(String saslKerberosServiceName) {
    this.saslKerberosServiceName = saslKerberosServiceName;
  }

  public String getSaslKerberosPrincipal() {
    return saslKerberosPrincipal;
  }

  public void setSaslKerberosPrincipal(String saslKerberosPrincipal) {
    this.saslKerberosPrincipal = saslKerberosPrincipal;
  }

  public String getSecurityProtocol() {
    return securityProtocol;
  }

  public void setSecurityProtocol(String securityProtocol) {
    this.securityProtocol = securityProtocol;
  }

  public String getAuthenticationMechanism() {
    return authenticationMechanism;
  }

  public void setAuthenticationMechanism(String authenticationMechanism) {
    this.authenticationMechanism = authenticationMechanism;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

}
