package com.machloop.fpc.cms.center.metadata.data;

public class ProtocolFtpLogDO extends AbstractLogRecordDO {

  private String user;
  private String cmdSeq;
  private String cmd;
  private String reply;
  private String filename;
  private String dataChannelIp;
  private Integer dataChannelPort;

  @Override
  public String toString() {
    return "ProtocolFtpLogDO [user=" + user + ", cmdSeq=" + cmdSeq + ", cmd=" + cmd + ", reply="
        + reply + ", filename=" + filename + ", dataChannelIp=" + dataChannelIp
        + ", dataChannelPort=" + dataChannelPort + "]";
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getCmdSeq() {
    return cmdSeq;
  }

  public void setCmdSeq(String cmdSeq) {
    this.cmdSeq = cmdSeq;
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

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getDataChannelIp() {
    return dataChannelIp;
  }

  public void setDataChannelIp(String dataChannelIp) {
    this.dataChannelIp = dataChannelIp;
  }

  public Integer getDataChannelPort() {
    return dataChannelPort;
  }

  public void setDataChannelPort(Integer dataChannelPort) {
    this.dataChannelPort = dataChannelPort;
  }

}
