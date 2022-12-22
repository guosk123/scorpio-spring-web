package com.machloop.fpc.manager.metadata.vo;

/**
 * @author minjiajun
 *
 * create at 2022年5月30日, fpc-manager
 */
public class ProtocolSocks4LogVO extends AbstractLogRecordVO {

  private String cmd;
  private String requestRemotePort;
  private String requestRemoteIp;
  private String userId;
  private String domainName;
  private String cmdResult;
  private String responseRemoteIp;
  private String responseRemotePort;
  private int channelState;

  @Override
  public String toString() {
    return "ProtocolSocks4LogVO [cmd=" + cmd + ", requestRemotePort=" + requestRemotePort
        + ", requestRemoteIp=" + requestRemoteIp + ", userId=" + userId + ", domainName="
        + domainName + ", cmdResult=" + cmdResult + ", responseRemoteIp=" + responseRemoteIp
        + ", responseRemotePort=" + responseRemotePort + ", channelState=" + channelState + "]";
  }

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public String getRequestRemotePort() {
    return requestRemotePort;
  }

  public void setRequestRemotePort(String requestRemotePort) {
    this.requestRemotePort = requestRemotePort;
  }

  public String getRequestRemoteIp() {
    return requestRemoteIp;
  }

  public void setRequestRemoteIp(String requestRemoteIp) {
    this.requestRemoteIp = requestRemoteIp;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  public String getCmdResult() {
    return cmdResult;
  }

  public void setCmdResult(String cmdResult) {
    this.cmdResult = cmdResult;
  }

  public String getResponseRemoteIp() {
    return responseRemoteIp;
  }

  public void setResponseRemoteIp(String responseRemoteIp) {
    this.responseRemoteIp = responseRemoteIp;
  }

  public String getResponseRemotePort() {
    return responseRemotePort;
  }

  public void setResponseRemotePort(String responseRemotePort) {
    this.responseRemotePort = responseRemotePort;
  }

  public int getChannelState() {
    return channelState;
  }

  public void setChannelState(int channelState) {
    this.channelState = channelState;
  }

}
