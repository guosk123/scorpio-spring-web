package com.machloop.fpc.cms.center.appliance.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月12日, fpc-cms-center
 */
public class SmtpConfigurationVO {

  private String mailUsername;
  private String mailAddress;
  @NotEmpty(message = "邮件服务器不能为空")
  private String smtpServer;
  private int serverPort;
  private String encrypt;
  @NotEmpty(message = "登录用户名不能为空")
  private String loginUser;
  @NotEmpty(message = "登录密码不能为空")
  private String loginPassword;

  @Override
  public String toString() {
    return "SmtpConfigurationVO [mailUsername=" + mailUsername + ", mailAddress=" + mailAddress
        + ", smtpServer=" + smtpServer + ", serverPort=" + serverPort + ", encrypt=" + encrypt
        + ", loginUser=" + loginUser + ", loginPassword=" + loginPassword + "]";
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
