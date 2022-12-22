package com.machloop.fpc.manager.appliance.vo;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月11日, fpc-manager
 */
public class SmtpConfigurationModificationVO {

  private String mailUsername;
  private String mailAddress;
  private String smtpServer;
  private int serverPort;
  private String encrypt;
  private String loginUser;
  private String loginPassword;

  @Override
  public String toString() {
    return "SmtpConfigurationModificationVO [mailUsername=" + mailUsername + ", mailAddress="
        + mailAddress + ", smtpServer=" + smtpServer + ", serverPort=" + serverPort + ", encrypt="
        + encrypt + ", loginUser=" + loginUser + ", loginPassword=" + loginPassword + "]";
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
