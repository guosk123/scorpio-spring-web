package com.scorpio.metric.system.data;

public class MonitorNetwork {

  private String netifName;
  private long bytesRx;
  private long packetsRx;
  private long bytesTx;
  private long packetsTx;

  @Override
  public String toString() {
    return "MonitorNetwork [netifName=" + netifName + ", bytesRx=" + bytesRx + ", packetsRx="
        + packetsRx + ", bytesTx=" + bytesTx + ", packetsTx=" + packetsTx + "]";
  }

  public String getNetifName() {
    return netifName;
  }

  public void setNetifName(String netifName) {
    this.netifName = netifName;
  }

  public long getBytesRx() {
    return bytesRx;
  }

  public void setBytesRx(long bytesRx) {
    this.bytesRx = bytesRx;
  }

  public long getPacketsRx() {
    return packetsRx;
  }

  public void setPacketsRx(long packetsRx) {
    this.packetsRx = packetsRx;
  }

  public long getBytesTx() {
    return bytesTx;
  }

  public void setBytesTx(long bytesTx) {
    this.bytesTx = bytesTx;
  }

  public long getPacketsTx() {
    return packetsTx;
  }

  public void setPacketsTx(long packetsTx) {
    this.packetsTx = packetsTx;
  }
}
