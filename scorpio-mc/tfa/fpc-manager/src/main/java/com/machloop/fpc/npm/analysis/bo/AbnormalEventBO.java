package com.machloop.fpc.npm.analysis.bo;

/**
 * @author guosk
 *
 * create at 2021年7月15日, fpc-manager
 */
public class AbnormalEventBO {

  private String id;
  private String startTime;
  private String networkId;
  private int type;
  private String content;
  private String description;
  private String srcIp;
  private String destIp;
  private int destPort;
  private int l7ProtocolId;
  private Integer countryIdInitiator;
  private Integer provinceIdInitiator;
  private Integer cityIdInitiator;
  private Integer countryIdResponder;
  private Integer provinceIdResponder;
  private Integer cityIdResponder;

  @Override
  public String toString() {
    return "AbnormalEventBO [id=" + id + ", startTime=" + startTime + ", networkId=" + networkId
        + ", type=" + type + ", content=" + content + ", description=" + description + ", srcIp="
        + srcIp + ", destIp=" + destIp + ", destPort=" + destPort + ", l7ProtocolId=" + l7ProtocolId
        + ", countryIdInitiator=" + countryIdInitiator + ", provinceIdInitiator="
        + provinceIdInitiator + ", cityIdInitiator=" + cityIdInitiator + ", countryIdResponder="
        + countryIdResponder + ", provinceIdResponder=" + provinceIdResponder + ", cityIdResponder="
        + cityIdResponder + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSrcIp() {
    return srcIp;
  }

  public void setSrcIp(String srcIp) {
    this.srcIp = srcIp;
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

  public int getL7ProtocolId() {
    return l7ProtocolId;
  }

  public void setL7ProtocolId(int l7ProtocolId) {
    this.l7ProtocolId = l7ProtocolId;
  }

  public Integer getCountryIdInitiator() {
    return countryIdInitiator;
  }

  public void setCountryIdInitiator(Integer countryIdInitiator) {
    this.countryIdInitiator = countryIdInitiator;
  }

  public Integer getProvinceIdInitiator() {
    return provinceIdInitiator;
  }

  public void setProvinceIdInitiator(Integer provinceIdInitiator) {
    this.provinceIdInitiator = provinceIdInitiator;
  }

  public Integer getCityIdInitiator() {
    return cityIdInitiator;
  }

  public void setCityIdInitiator(Integer cityIdInitiator) {
    this.cityIdInitiator = cityIdInitiator;
  }

  public Integer getCountryIdResponder() {
    return countryIdResponder;
  }

  public void setCountryIdResponder(Integer countryIdResponder) {
    this.countryIdResponder = countryIdResponder;
  }

  public Integer getProvinceIdResponder() {
    return provinceIdResponder;
  }

  public void setProvinceIdResponder(Integer provinceIdResponder) {
    this.provinceIdResponder = provinceIdResponder;
  }

  public Integer getCityIdResponder() {
    return cityIdResponder;
  }

  public void setCityIdResponder(Integer cityIdResponder) {
    this.cityIdResponder = cityIdResponder;
  }

}
