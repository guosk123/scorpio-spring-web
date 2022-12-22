package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月11日, fpc-cms-center
 */
public class SmtpConfigurationDO extends BaseOperateDO {

  private String id;
  private String mailUsername;
  private String mailAddress;
  private String smtpServer;
  private int serverPort;
  private String encrypt;
  private String loginUser;
  private String loginPassword;

  @Override
  public String toString() {
    return "SmtpConfigurationDO [id=" + id + ", mailUsername=" + mailUsername + ", mailAddress="
        + mailAddress + ", smtpServer=" + smtpServer + ", serverPort=" + serverPort + ", encrypt="
        + encrypt + ", loginUser=" + loginUser + ", loginPassword=" + loginPassword + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMailUsername() {
    return mailUsername;
  }

  public void setMailUsername(String mailUsername) {
    this.mailUsername = mailUsername;
  }

  public String getMailAddress() {
    return mailAddress;
  }

  public void setMailAddress(String mailAddress) {
    this.mailAddress = mailAddress;
  }

  public String getSmtpServer() {
    return smtpServer;
  }

  public void setSmtpServer(String smtpServer) {
    this.smtpServer = smtpServer;
  }

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public String getEncrypt() {
    return encrypt;
  }

  public void setEncrypt(String encrypt) {
    this.encrypt = encrypt;
  }

  public String getLoginUser() {
    return loginUser;
  }

  public void setLoginUser(String loginUser) {
    this.loginUser = loginUser;
  }

  public String getLoginPassword() {
    return loginPassword;
  }

  public void setLoginPassword(String loginPassword) {
    this.loginPassword = loginPassword;
  }

}
