package com.machloop.fpc.manager.metric.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.machloop.fpc.manager.global.data.AbstractDataRecordDO;

/**
 * @author guosk
 *
 * create at 2021年4月22日, fpc-manager
 */
public class MetricIpConversationDataRecordDO extends AbstractDataRecordDO {

  @JsonProperty("ip_a_address")
  private String ipAAddress;
  @JsonProperty("ip_b_address")
  private String ipBAddress;
  private long downstreamBytes;
  private long upstreamBytes;
  private long downstreamPackets;
  private long upstreamPackets;
  private long activeEstablishedSessions;
  private long passiveEstablishedSessions;

  @Override
  public String toString() {
    return "MetricIpConversationDataRecordDO [ipAAddress=" + ipAAddress + ", ipBAddress="
        + ipBAddress + ", downstreamBytes=" + downstreamBytes + ", upstreamBytes=" + upstreamBytes
        + ", downstreamPackets=" + downstreamPackets + ", upstreamPackets=" + upstreamPackets
        + ", activeEstablishedSessions=" + activeEstablishedSessions
        + ", passiveEstablishedSessions=" + passiveEstablishedSessions + "]";
  }

  public String getIpAAddress() {
    return ipAAddress;
  }

  public void setIpAAddress(String ipAAddress) {
    this.ipAAddress = ipAAddress;
  }

  public String getIpBAddress() {
    return ipBAddress;
  }

  public void setIpBAddress(String ipBAddress) {
    this.ipBAddress = ipBAddress;
  }

  public long getDownstreamBytes() {
    return downstreamBytes;
  }

  public void setDownstreamBytes(long downstreamBytes) {
    this.downstreamBytes = downstreamBytes;
  }

  public long getUpstreamBytes() {
    return upstreamBytes;
  }

  public void setUpstreamBytes(long upstreamBytes) {
    this.upstreamBytes = upstreamBytes;
  }

  public long getDownstreamPackets() {
    return downstreamPackets;
  }

  public void setDownstreamPackets(long downstreamPackets) {
    this.downstreamPackets = downstreamPackets;
  }

  public long getUpstreamPackets() {
    return upstreamPackets;
  }

  public void setUpstreamPackets(long upstreamPackets) {
    this.upstreamPackets = upstreamPackets;
  }

  public long getActiveEstablishedSessions() {
    return activeEstablishedSessions;
  }

  public void setActiveEstablishedSessions(long activeEstablishedSessions) {
    this.activeEstablishedSessions = activeEstablishedSessions;
  }

  public long getPassiveEstablishedSessions() {
    return passiveEstablishedSessions;
  }

  public void setPassiveEstablishedSessions(long passiveEstablishedSessions) {
    this.passiveEstablishedSessions = passiveEstablishedSessions;
  }

}
