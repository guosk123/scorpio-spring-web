package com.machloop.fpc.manager.metadata.data;

import java.util.Map;

public class ProtocolSipLogDO extends AbstractLogRecordDO {

  private String from;

  private String to;

  private String ipProtocol;

  private String type;

  private int seqNum;

  private String callId;

  private String requestUri;

  private String statusCode;

  private Map<String, String> sdp;

  @Override
  public String toString() {
    return "ProtocolSipLogDO{" + "from='" + from + '\'' + ", to='" + to + '\'' + ", ipProtocol='"
        + ipProtocol + '\'' + ", type='" + type + '\'' + ", seqNum=" + seqNum + ", callId='"
        + callId + '\'' + ", requestUri='" + requestUri + '\'' + ", statusCode='" + statusCode
        + '\'' + ", sdp=" + sdp + '}';
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

  public int getSeqNum() {
    return seqNum;
  }

  public void setSeqNum(int seqNum) {
    this.seqNum = seqNum;
  }

  public String getCallId() {
    return callId;
  }

  public void setCallId(String callId) {
    this.callId = callId;
  }

  public String getRequestUri() {
    return requestUri;
  }

  public void setRequestUri(String requestUri) {
    this.requestUri = requestUri;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(String statusCode) {
    this.statusCode = statusCode;
  }

  public Map<String, String> getSdp() {
    return sdp;
  }

  public void setSdp(Map<String, String> sdp) {
    this.sdp = sdp;
  }
}
