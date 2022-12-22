package com.machloop.fpc.manager.metadata.data;

public class ProtocolTelnetLogDO extends AbstractLogRecordDO {

  private String username;
  private String password;
  private String cmd;
  private String reply;

  @Override
  public String toString() {
    return "ProtocolTelnetLogDO [username=" + username + ", password=" + password + ", cmd=" + cmd
        + ", reply=" + reply + "]";
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

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public String getReply() {
    return reply;
  }

  public void setReply(String reply) {
    this.reply = reply;
  }

}
