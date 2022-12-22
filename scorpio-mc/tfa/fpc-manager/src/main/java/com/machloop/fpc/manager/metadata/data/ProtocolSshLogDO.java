package com.machloop.fpc.manager.metadata.data;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
public class ProtocolSshLogDO extends AbstractLogRecordDO {

  private String clientVersion;
  private String clientSoftware;
  private String clientComments;
  private String serverVersion;
  private String serverSoftware;
  private String serverComments;
  private String serverKey;
  private String serverKeyType;

  @Override
  public String toString() {
    return "ProtocolSshLogDO [clientVersion=" + clientVersion + ", clientSoftware=" + clientSoftware
        + ", clientComments=" + clientComments + ", serverVersion=" + serverVersion
        + ", serverSoftware=" + serverSoftware + ", serverComments=" + serverComments
        + ", serverKey=" + serverKey + ", serverKeyType=" + serverKeyType + "]";
  }

  public String getClientVersion() {
    return clientVersion;
  }

  public void setClientVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }

  public String getClientSoftware() {
    return clientSoftware;
  }

  public void setClientSoftware(String clientSoftware) {
    this.clientSoftware = clientSoftware;
  }

  public String getClientComments() {
    return clientComments;
  }

  public void setClientComments(String clientComments) {
    this.clientComments = clientComments;
  }

  public String getServerVersion() {
    return serverVersion;
  }

  public void setServerVersion(String serverVersion) {
    this.serverVersion = serverVersion;
  }

  public String getServerSoftware() {
    return serverSoftware;
  }

  public void setServerSoftware(String serverSoftware) {
    this.serverSoftware = serverSoftware;
  }

  public String getServerComments() {
    return serverComments;
  }

  public void setServerComments(String serverComments) {
    this.serverComments = serverComments;
  }

  public String getServerKey() {
    return serverKey;
  }

  public void setServerKey(String serverKey) {
    this.serverKey = serverKey;
  }

  public String getServerKeyType() {
    return serverKeyType;
  }

  public void setServerKeyType(String serverKeyType) {
    this.serverKeyType = serverKeyType;
  }

}
