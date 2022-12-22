package com.machloop.fpc.manager.appliance.bo;

import java.util.Date;
import org.apache.commons.lang.StringUtils;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月11日, fpc-manager
 */
public class SmtpConfigurationBO implements LogAudit {

  private String id;
  private String mailUsername;
  private String mailAddress;
  private String smtpServer;
  private int serverPort;
  private String encrypt;
  private String loginUser;
  private String loginPassword;
  
  private Date updateTime;

  @Override
  public String toString() {
    return "SmtpConfigurationBO [id=" + id + ", mailUsername=" + mailUsername + ", mailAddress="
        + mailAddress + ", smtpServer=" + smtpServer + ", serverPort=" + serverPort + ", encrypt="
        + encrypt + ", loginUser=" + loginUser + ", loginPassword=" + loginPassword
        + ", updateTime=" + updateTime + "]";
  }

  /**
   * @see com.machloop.alpha.webapp.base.LogAudit#toAuditLogText(int)
   */
  @Override
  public String toAuditLogText(int auditLogAction) {
    if (StringUtils.isBlank(id)) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改smtp配置：");
        break;
      default:
        return "";
    }

    builder.append("用户名=").append(mailUsername).append(";");
    builder.append("邮件地址=").append(mailAddress).append(";");
    builder.append("邮件服务器=").append(smtpServer).append(";");
    builder.append("服务器端口=").append(serverPort).append(";");
    builder.append("是否加密=").append(encrypt).append(";");
    builder.append("登录用户=").append(loginUser).append(";");
    builder.append("登录密码=").append(loginPassword).append("。");
    return builder.toString();
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

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }
  
}
