package com.machloop.fpc.cms.npm.analysis.data;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
public class SuricataAlertMessageDO {

  private String timestamp;
  private int sid;
  private String msg;
  private String networkId;
  private String classtypeId;
  private String mitreTacticId;
  private String mitreTechniqueId;
  private String cve;
  private String cnnvd;
  private Integer signatureSeverity;
  private String target;
  private String srcIpv4;
  private String srcIpv6;
  private int srcPort;
  private String destIpv4;
  private String destIpv6;
  private int destPort;
  private String protocol;
  private String l7Protocol;
  private String flowId;
  private String domain;
  private String url;
  private Integer countryIdInitiator;
  private Integer provinceIdInitiator;
  private Integer cityIdInitiator;
  private Integer countryIdResponder;
  private Integer provinceIdResponder;
  private Integer cityIdResponder;
  private String source;
  private String tag;
  private String basicTag;

  @Override
  public String toString() {
    return "SuricataAlertMessageDO{" + "timestamp='" + timestamp + '\'' + ", sid=" + sid + ", msg='"
        + msg + '\'' + ", networkId='" + networkId + '\'' + ", classtypeId='" + classtypeId + '\''
        + ", mitreTacticId='" + mitreTacticId + '\'' + ", mitreTechniqueId='" + mitreTechniqueId
        + '\'' + ", cve='" + cve + '\'' + ", cnnvd='" + cnnvd + '\'' + ", signatureSeverity="
        + signatureSeverity + ", target='" + target + '\'' + ", srcIpv4='" + srcIpv4 + '\''
        + ", srcIpv6='" + srcIpv6 + '\'' + ", srcPort=" + srcPort + ", destIpv4='" + destIpv4 + '\''
        + ", destIpv6='" + destIpv6 + '\'' + ", destPort=" + destPort + ", protocol='" + protocol
        + '\'' + ", l7Protocol='" + l7Protocol + '\'' + ", flowId='" + flowId + '\'' + ", domain='"
        + domain + '\'' + ", url='" + url + '\'' + ", countryIdInitiator=" + countryIdInitiator
        + ", provinceIdInitiator=" + provinceIdInitiator + ", cityIdInitiator=" + cityIdInitiator
        + ", countryIdResponder=" + countryIdResponder + ", provinceIdResponder="
        + provinceIdResponder + ", cityIdResponder=" + cityIdResponder + ", source='" + source
        + '\'' + ", tag='" + tag + '\'' + ", basicTag='" + basicTag + '\'' + '}';
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public int getSid() {
    return sid;
  }

  public void setSid(int sid) {
    this.sid = sid;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getClasstypeId() {
    return classtypeId;
  }

  public void setClasstypeId(String classtypeId) {
    this.classtypeId = classtypeId;
  }

  public String getMitreTacticId() {
    return mitreTacticId;
  }

  public void setMitreTacticId(String mitreTacticId) {
    this.mitreTacticId = mitreTacticId;
  }

  public String getMitreTechniqueId() {
    return mitreTechniqueId;
  }

  public void setMitreTechniqueId(String mitreTechniqueId) {
    this.mitreTechniqueId = mitreTechniqueId;
  }

  public String getCve() {
    return cve;
  }

  public void setCve(String cve) {
    this.cve = cve;
  }

  public String getCnnvd() {
    return cnnvd;
  }

  public void setCnnvd(String cnnvd) {
    this.cnnvd = cnnvd;
  }

  public Integer getSignatureSeverity() {
    return signatureSeverity;
  }

  public void setSignatureSeverity(Integer signatureSeverity) {
    this.signatureSeverity = signatureSeverity;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getSrcIpv4() {
    return srcIpv4;
  }

  public void setSrcIpv4(String srcIpv4) {
    this.srcIpv4 = srcIpv4;
  }

  public String getSrcIpv6() {
    return srcIpv6;
  }

  public void setSrcIpv6(String srcIpv6) {
    this.srcIpv6 = srcIpv6;
  }

  public int getSrcPort() {
    return srcPort;
  }

  public void setSrcPort(int srcPort) {
    this.srcPort = srcPort;
  }

  public String getDestIpv4() {
    return destIpv4;
  }

  public void setDestIpv4(String destIpv4) {
    this.destIpv4 = destIpv4;
  }

  public String getDestIpv6() {
    return destIpv6;
  }

  public void setDestIpv6(String destIpv6) {
    this.destIpv6 = destIpv6;
  }

  public int getDestPort() {
    return destPort;
  }

  public void setDestPort(int destPort) {
    this.destPort = destPort;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getL7Protocol() {
    return l7Protocol;
  }

  public void setL7Protocol(String l7Protocol) {
    this.l7Protocol = l7Protocol;
  }

  public String getFlowId() {
    return flowId;
  }

  public void setFlowId(String flowId) {
    this.flowId = flowId;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
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

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getBasicTag() {
    return basicTag;
  }

  public void setBasicTag(String basicTag) {
    this.basicTag = basicTag;
  }
}
