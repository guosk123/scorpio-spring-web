package com.machloop.fpc.manager.metadata.data;

/**
 * @author ChenXiao
 * create at 2022/9/8
 */
public class ProtocolRtpLogDO extends AbstractLogRecordDO {


  private String inviteTime;
  private String from;
  private String to;
  private String ipProtocol;
  private long ssrc;
  private int status;
  private long rtpTotalPackets;
  private long rtpLossPackets;
  private String rtpLossPacketsRate;
  private long jitterMax;
  private long jitterMean;
  private String payload;
  private String inviteSrcIp;
  private int inviteSrcPort;
  private String inviteDestIp;
  private int inviteDestPort;
  private String inviteIpProtocol;
  private String sipFlowId;

  @Override
  public String toString() {
    return "ProtocolRtpLogDO{" + "inviteTime='" + inviteTime + '\'' + ", from='" + from + '\''
        + ", to='" + to + '\'' + ", ipProtocol='" + ipProtocol + '\'' + ", ssrc=" + ssrc
        + ", status=" + status + ", rtpTotalPackets=" + rtpTotalPackets + ", rtpLossPackets="
        + rtpLossPackets + ", rtpLossPacketsRate='" + rtpLossPacketsRate + '\'' + ", jitterMax="
        + jitterMax + ", jitterMean=" + jitterMean + ", payload='" + payload + '\''
        + ", inviteSrcIp='" + inviteSrcIp + '\'' + ", inviteSrcPort=" + inviteSrcPort
        + ", inviteDestIp='" + inviteDestIp + '\'' + ", inviteDestPort=" + inviteDestPort
        + ", inviteIpProtocol='" + inviteIpProtocol + '\'' + ", sipFlowId='" + sipFlowId + '\''
        + '}';
  }

  public String getInviteTime() {
    return inviteTime;
  }

  public void setInviteTime(String inviteTime) {
    this.inviteTime = inviteTime;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getIpProtocol() {
    return ipProtocol;
  }

  public void setIpProtocol(String ipProtocol) {
    this.ipProtocol = ipProtocol;
  }

  public long getSsrc() {
    return ssrc;
  }

  public void setSsrc(long ssrc) {
    this.ssrc = ssrc;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public long getRtpTotalPackets() {
    return rtpTotalPackets;
  }

  public void setRtpTotalPackets(long rtpTotalPackets) {
    this.rtpTotalPackets = rtpTotalPackets;
  }

  public long getRtpLossPackets() {
    return rtpLossPackets;
  }

  public void setRtpLossPackets(long rtpLossPackets) {
    this.rtpLossPackets = rtpLossPackets;
  }

  public long getJitterMax() {
    return jitterMax;
  }

  public void setJitterMax(long jitterMax) {
    this.jitterMax = jitterMax;
  }

  public long getJitterMean() {
    return jitterMean;
  }

  public void setJitterMean(long jitterMean) {
    this.jitterMean = jitterMean;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getInviteSrcIp() {
    return inviteSrcIp;
  }

  public void setInviteSrcIp(String inviteSrcIp) {
    this.inviteSrcIp = inviteSrcIp;
  }

  public int getInviteSrcPort() {
    return inviteSrcPort;
  }

  public void setInviteSrcPort(int inviteSrcPort) {
    this.inviteSrcPort = inviteSrcPort;
  }

  public String getInviteDestIp() {
    return inviteDestIp;
  }

  public void setInviteDestIp(String inviteDestIp) {
    this.inviteDestIp = inviteDestIp;
  }

  public int getInviteDestPort() {
    return inviteDestPort;
  }

  public void setInviteDestPort(int inviteDestPort) {
    this.inviteDestPort = inviteDestPort;
  }

  public String getInviteIpProtocol() {
    return inviteIpProtocol;
  }

  public void setInviteIpProtocol(String inviteIpProtocol) {
    this.inviteIpProtocol = inviteIpProtocol;
  }

  public String getSipFlowId() {
    return sipFlowId;
  }

  public void setSipFlowId(String sipFlowId) {
    this.sipFlowId = sipFlowId;
  }

  public String getRtpLossPacketsRate() {
    return rtpLossPacketsRate;
  }

  public void setRtpLossPacketsRate(String rtpLossPacketsRate) {
    this.rtpLossPacketsRate = rtpLossPacketsRate;
  }
}
