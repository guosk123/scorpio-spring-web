package com.machloop.fpc.manager.metadata.vo;

import java.util.List;

/**
 * @author guosk
 *
 * create at 2021年5月12日, fpc-manager
 */
public class ProtocolOspfLogVO extends AbstractLogRecordVO {

  private int version;
  private int messageType;
  private int packetLength;
  private long sourceOspfRouter;
  private long areaId;
  private List<String> linkStateIpv4Address;
  private List<String> linkStateIpv6Address;
  private String message;

  @Override
  public String toString() {
    return "ProtocolOspfLogVO [version=" + version + ", messageType=" + messageType
        + ", packetLength=" + packetLength + ", sourceOspfRouter=" + sourceOspfRouter + ", areaId="
        + areaId + ", linkStateIpv4Address=" + linkStateIpv4Address + ", linkStateIpv6Address="
        + linkStateIpv6Address + ", message=" + message + "]";
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public int getMessageType() {
    return messageType;
  }

  public void setMessageType(int messageType) {
    this.messageType = messageType;
  }

  public int getPacketLength() {
    return packetLength;
  }

  public void setPacketLength(int packetLength) {
    this.packetLength = packetLength;
  }

  public long getSourceOspfRouter() {
    return sourceOspfRouter;
  }

  public void setSourceOspfRouter(long sourceOspfRouter) {
    this.sourceOspfRouter = sourceOspfRouter;
  }

  public long getAreaId() {
    return areaId;
  }

  public void setAreaId(long areaId) {
    this.areaId = areaId;
  }

  public List<String> getLinkStateIpv4Address() {
    return linkStateIpv4Address;
  }

  public void setLinkStateIpv4Address(List<String> linkStateIpv4Address) {
    this.linkStateIpv4Address = linkStateIpv4Address;
  }

  public List<String> getLinkStateIpv6Address() {
    return linkStateIpv6Address;
  }

  public void setLinkStateIpv6Address(List<String> linkStateIpv6Address) {
    this.linkStateIpv6Address = linkStateIpv6Address;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
