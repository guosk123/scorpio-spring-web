package com.machloop.fpc.manager.metadata.data;

/**
 * @author ChenXiao
 * create at 2022/10/27
 */
public class FileRestoreInfoDO {

  private String flowId;
  private String networkId;
  private String timestamp;
  private String srcIp;
  private int srcPort;
  private String destIp;
  private int destPort;
  private String md5;
  private String sha1;
  private String sha256;
  private String name;
  private long size;
  private String magic;
  private String l7Protocol;
  private int state;

  @Override
  public String toString() {
    return "FileRestoreInfoDO{" + "flowId='" + flowId + '\'' + ", networkId='" + networkId + '\''
        + ", timestamp='" + timestamp + '\'' + ", srcIp='" + srcIp + '\'' + ", srcPort=" + srcPort
        + ", destIp='" + destIp + '\'' + ", destPort=" + destPort + ", md5='" + md5 + '\''
        + ", sha1='" + sha1 + '\'' + ", sha256='" + sha256 + '\'' + ", name='" + name + '\''
        + ", size=" + size + ", magic='" + magic + '\'' + ", l7Protocol='" + l7Protocol + '\''
        + ", state=" + state + '}';
  }

  public String getFlowId() {
    return flowId;
  }

  public void setFlowId(String flowId) {
    this.flowId = flowId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getSrcIp() {
    return srcIp;
  }

  public void setSrcIp(String srcIp) {
    this.srcIp = srcIp;
  }

  public int getSrcPort() {
    return srcPort;
  }

  public void setSrcPort(int srcPort) {
    this.srcPort = srcPort;
  }

  public String getDestIp() {
    return destIp;
  }

  public void setDestIp(String destIp) {
    this.destIp = destIp;
  }

  public int getDestPort() {
    return destPort;
  }

  public void setDestPort(int destPort) {
    this.destPort = destPort;
  }

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public String getSha1() {
    return sha1;
  }

  public void setSha1(String sha1) {
    this.sha1 = sha1;
  }

  public String getSha256() {
    return sha256;
  }

  public void setSha256(String sha256) {
    this.sha256 = sha256;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getMagic() {
    return magic;
  }

  public void setMagic(String magic) {
    this.magic = magic;
  }

  public String getL7Protocol() {
    return l7Protocol;
  }

  public void setL7Protocol(String l7Protocol) {
    this.l7Protocol = l7Protocol;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }
}
