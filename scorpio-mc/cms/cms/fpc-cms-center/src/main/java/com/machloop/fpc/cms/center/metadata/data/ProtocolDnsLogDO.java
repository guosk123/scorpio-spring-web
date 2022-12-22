package com.machloop.fpc.cms.center.metadata.data;

import java.util.List;

public class ProtocolDnsLogDO extends AbstractLogRecordDO {

  private String answer;
  private String domain;
  private List<String> domainAddress;
  private int domainIntelligence;
  private String dnsRcode;
  private String dnsRcodeName;
  private String dnsQueries;
  private long subdomainCount;
  private String transactionId;

  @Override
  public String toString() {
    return "ProtocolDnsLogDO [answer=" + answer + ", domain=" + domain + ", domainAddress="
        + domainAddress + ", domainIntelligence=" + domainIntelligence + ", dnsRcode=" + dnsRcode
        + ", dnsRcodeName=" + dnsRcodeName + ", dnsQueries=" + dnsQueries + ", subdomainCount="
        + subdomainCount + ", transactionId=" + transactionId + "]";
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public List<String> getDomainAddress() {
    return domainAddress;
  }

  public void setDomainAddress(List<String> domainAddress) {
    this.domainAddress = domainAddress;
  }

  public int getDomainIntelligence() {
    return domainIntelligence;
  }

  public void setDomainIntelligence(int domainIntelligence) {
    this.domainIntelligence = domainIntelligence;
  }

  public String getDnsRcode() {
    return dnsRcode;
  }

  public void setDnsRcode(String dnsRcode) {
    this.dnsRcode = dnsRcode;
  }

  public String getDnsRcodeName() {
    return dnsRcodeName;
  }

  public void setDnsRcodeName(String dnsRcodeName) {
    this.dnsRcodeName = dnsRcodeName;
  }

  public String getDnsQueries() {
    return dnsQueries;
  }

  public void setDnsQueries(String dnsQueries) {
    this.dnsQueries = dnsQueries;
  }

  public long getSubdomainCount() {
    return subdomainCount;
  }

  public void setSubdomainCount(long subdomainCount) {
    this.subdomainCount = subdomainCount;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }
}
