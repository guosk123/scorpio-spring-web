package com.machloop.fpc.cms.center.metadata.data;

public class ProtocolHttpLogDO extends AbstractLogRecordDO {

  private String method;
  private String host;
  private String uri;
  private String origin;
  private String cookie;
  private String userAgent;
  private String referer;
  private String xff;
  private String status;
  private String setCookie;
  private String contentType;
  private String acceptLanguage;
  private String requestHeader;
  private String requestBody;
  private String responseHeader;
  private String responseBody;
  private String fileName;
  private String fileType;
  private String fileFlag;
  private String acceptEncoding;
  private String location;
  private String decrypted;
  private String authorization;
  private String authType;
  private String osVersion;
  private Integer channelState;
  private String xffFirst;
  private String xffLast;
  private String xffFirstAlias;
  private String xffLastAlias;

  @Override
  public String toString() {
    return "ProtocolHttpLogDO [method=" + method + ", host=" + host + ", uri=" + uri + ", origin="
        + origin + ", cookie=" + cookie + ", userAgent=" + userAgent + ", referer=" + referer
        + ", xff=" + xff + ", status=" + status + ", setCookie=" + setCookie + ", contentType="
        + contentType + ", acceptLanguage=" + acceptLanguage + ", requestHeader=" + requestHeader
        + ", requestBody=" + requestBody + ", responseHeader=" + responseHeader + ", responseBody="
        + responseBody + ", fileName=" + fileName + ", fileType=" + fileType + ", fileFlag="
        + fileFlag + ", acceptEncoding=" + acceptEncoding + ", location=" + location
        + ", decrypted=" + decrypted + ", authorization=" + authorization + ", authType=" + authType
        + ", osVersion=" + osVersion + ", channelState=" + channelState + ", xffFirst=" + xffFirst
        + ", xffLast=" + xffLast + ", xffFirstAlias=" + xffFirstAlias + ", xffLastAlias="
        + xffLastAlias + "]";
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public String getCookie() {
    return cookie;
  }

  public void setCookie(String cookie) {
    this.cookie = cookie;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getReferer() {
    return referer;
  }

  public void setReferer(String referer) {
    this.referer = referer;
  }

  public String getXff() {
    return xff;
  }

  public void setXff(String xff) {
    this.xff = xff;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getSetCookie() {
    return setCookie;
  }

  public void setSetCookie(String setCookie) {
    this.setCookie = setCookie;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getAcceptLanguage() {
    return acceptLanguage;
  }

  public void setAcceptLanguage(String acceptLanguage) {
    this.acceptLanguage = acceptLanguage;
  }

  public String getRequestHeader() {
    return requestHeader;
  }

  public void setRequestHeader(String requestHeader) {
    this.requestHeader = requestHeader;
  }

  public String getRequestBody() {
    return requestBody;
  }

  public void setRequestBody(String requestBody) {
    this.requestBody = requestBody;
  }

  public String getResponseHeader() {
    return responseHeader;
  }

  public void setResponseHeader(String responseHeader) {
    this.responseHeader = responseHeader;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public String getFileFlag() {
    return fileFlag;
  }

  public void setFileFlag(String fileFlag) {
    this.fileFlag = fileFlag;
  }

  public String getAcceptEncoding() {
    return acceptEncoding;
  }

  public void setAcceptEncoding(String acceptEncoding) {
    this.acceptEncoding = acceptEncoding;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getDecrypted() {
    return decrypted;
  }

  public void setDecrypted(String decrypted) {
    this.decrypted = decrypted;
  }

  public String getAuthorization() {
    return authorization;
  }

  public void setAuthorization(String authorization) {
    this.authorization = authorization;
  }

  public String getAuthType() {
    return authType;
  }

  public void setAuthType(String authType) {
    this.authType = authType;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public void setOsVersion(String osVersion) {
    this.osVersion = osVersion;
  }

  public Integer getChannelState() {
    return channelState;
  }

  public void setChannelState(Integer channelState) {
    this.channelState = channelState;
  }

  public String getXffFirst() {
    return xffFirst;
  }

  public void setXffFirst(String xffFirst) {
    this.xffFirst = xffFirst;
  }

  public String getXffLast() {
    return xffLast;
  }

  public void setXffLast(String xffLast) {
    this.xffLast = xffLast;
  }

  public String getXffFirstAlias() {
    return xffFirstAlias;
  }

  public void setXffFirstAlias(String xffFirstAlias) {
    this.xffFirstAlias = xffFirstAlias;
  }

  public String getXffLastAlias() {
    return xffLastAlias;
  }

  public void setXffLastAlias(String xffLastAlias) {
    this.xffLastAlias = xffLastAlias;
  }
}
