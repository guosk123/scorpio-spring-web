package com.machloop.fpc.cms.center.metadata.vo;

import java.util.List;

public class ProtocolSslLogVO extends AbstractLogRecordVO {

  private String serverName;
  private List<String> serverCertsSha1;
  private String ja3Client;
  private String ja3Server;
  private String version;
  private String cipherSuite;
  private String signatureAlgorithm;
  private String issuer;
  private String commonName;
  private String validity;
  private String authType;
  private List<String> clientCipherSuite;
  private List<String> clientExtensions;
  private List<String> serverExtensions;
  private String clientCurVersion;
  private String clientMaxVersion;
  private int certsLen;
  private String crlUrls;
  private String ocspUrls;
  private String issuerUrls;
  private int isReuse;
  private int secProto;

  @Override
  public String toString() {
    return "ProtocolSslLogVO [serverName=" + serverName + ", serverCertsSha1=" + serverCertsSha1
        + ", ja3Client=" + ja3Client + ", ja3Server=" + ja3Server + ", version=" + version
        + ", cipherSuite=" + cipherSuite + ", signatureAlgorithm=" + signatureAlgorithm
        + ", issuer=" + issuer + ", commonName=" + commonName + ", validity=" + validity
        + ", authType=" + authType + ", clientCipherSuite=" + clientCipherSuite
        + ", clientExtensions=" + clientExtensions + ", serverExtensions=" + serverExtensions
        + ", clientCurVersion=" + clientCurVersion + ", clientMaxVersion=" + clientMaxVersion
        + ", certsLen=" + certsLen + ", crlUrls=" + crlUrls + ", ocspUrls=" + ocspUrls
        + ", issuerUrls=" + issuerUrls + ", isReuse=" + isReuse + ", secProto=" + secProto + "]";
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public List<String> getServerCertsSha1() {
    return serverCertsSha1;
  }

  public void setServerCertsSha1(List<String> serverCertsSha1) {
    this.serverCertsSha1 = serverCertsSha1;
  }

  public String getJa3Client() {
    return ja3Client;
  }

  public void setJa3Client(String ja3Client) {
    this.ja3Client = ja3Client;
  }

  public String getJa3Server() {
    return ja3Server;
  }

  public void setJa3Server(String ja3Server) {
    this.ja3Server = ja3Server;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getCipherSuite() {
    return cipherSuite;
  }

  public void setCipherSuite(String cipherSuite) {
    this.cipherSuite = cipherSuite;
  }

  public String getSignatureAlgorithm() {
    return signatureAlgorithm;
  }

  public void setSignatureAlgorithm(String signatureAlgorithm) {
    this.signatureAlgorithm = signatureAlgorithm;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public String getValidity() {
    return validity;
  }

  public void setValidity(String validity) {
    this.validity = validity;
  }

  public String getAuthType() {
    return authType;
  }

  public void setAuthType(String authType) {
    this.authType = authType;
  }

  public List<String> getClientCipherSuite() {
    return clientCipherSuite;
  }

  public void setClientCipherSuite(List<String> clientCipherSuite) {
    this.clientCipherSuite = clientCipherSuite;
  }

  public List<String> getClientExtensions() {
    return clientExtensions;
  }

  public void setClientExtensions(List<String> clientExtensions) {
    this.clientExtensions = clientExtensions;
  }

  public List<String> getServerExtensions() {
    return serverExtensions;
  }

  public void setServerExtensions(List<String> serverExtensions) {
    this.serverExtensions = serverExtensions;
  }

  public String getClientCurVersion() {
    return clientCurVersion;
  }

  public void setClientCurVersion(String clientCurVersion) {
    this.clientCurVersion = clientCurVersion;
  }

  public String getClientMaxVersion() {
    return clientMaxVersion;
  }

  public void setClientMaxVersion(String clientMaxVersion) {
    this.clientMaxVersion = clientMaxVersion;
  }

  public int getCertsLen() {
    return certsLen;
  }

  public void setCertsLen(int certsLen) {
    this.certsLen = certsLen;
  }

  public String getCrlUrls() {
    return crlUrls;
  }

  public void setCrlUrls(String crlUrls) {
    this.crlUrls = crlUrls;
  }

  public String getOcspUrls() {
    return ocspUrls;
  }

  public void setOcspUrls(String ocspUrls) {
    this.ocspUrls = ocspUrls;
  }

  public String getIssuerUrls() {
    return issuerUrls;
  }

  public void setIssuerUrls(String issuerUrls) {
    this.issuerUrls = issuerUrls;
  }

  public int getIsReuse() {
    return isReuse;
  }

  public void setIsReuse(int isReuse) {
    this.isReuse = isReuse;
  }

  public int getSecProto() {
    return secProto;
  }

  public void setSecProto(int secProto) {
    this.secProto = secProto;
  }

}
