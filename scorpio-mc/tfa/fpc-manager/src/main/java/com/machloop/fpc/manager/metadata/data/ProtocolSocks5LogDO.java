package com.machloop.fpc.manager.metadata.data;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
public class ProtocolSocks5LogDO extends AbstractLogRecordDO {

  private String atyp;
  private String bindAddr;
  private int bindPort;
  private String username;
  private String password;
  private String authMethod;
  private String authResult;
  private String cmd;
  private String cmdResult;
  private int channelState;

  @Override
  public String toString() {
    return "ProtocolSocks5LogDO [atyp=" + atyp + ", bindAddr=" + bindAddr + ", bindPort=" + bindPort
        + ", username=" + username + ", password=" + password + ", authMethod=" + authMethod
        + ", authResult=" + authResult + ", cmd=" + cmd + ", cmdResult=" + cmdResult
        + ", channelState=" + channelState + "]";
  }

  public String getAtyp() {
    return atyp;
  }

  public void setAtyp(String atyp) {
    this.atyp = atyp;
  }

  public String getBindAddr() {
    return bindAddr;
  }

  public void setBindAddr(String bindAddr) {
    this.bindAddr = bindAddr;
  }

  public int getBindPort() {
    return bindPort;
  }

  public void setBindPort(int bindPort) {
    this.bindPort = bindPort;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAuthMethod() {
    return authMethod;
  }

  public void setAuthMethod(String authMethod) {
    this.authMethod = authMethod;
  }

  public String getAuthResult() {
    return authResult;
  }

  public void setAuthResult(String authResult) {
    this.authResult = authResult;
  }

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public String getCmdResult() {
    return cmdResult;
  }

  public void setCmdResult(String cmdResult) {
    this.cmdResult = cmdResult;
  }

  public int getChannelState() {
    return channelState;
  }

  public void setChannelState(int channelState) {
    this.channelState = channelState;
  }

}
