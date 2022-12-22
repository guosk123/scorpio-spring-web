package com.machloop.fpc.manager.system.data;

public class MetricRestApiRecordDO {

  private String timestamp;
  private String apiName;
  private String uri;
  private String method;
  private String userIp;
  private String userId;
  private int status;
  private String response;


  @Override
  public String toString() {
    return "MetricRestApiRecordDO{" + "timestamp='" + timestamp + '\'' + ", apiName='" + apiName
        + '\'' + ", uri='" + uri + '\'' + ", method='" + method + '\'' + ", userIp='" + userIp
        + '\'' + ", userId='" + userId + '\'' + ", status=" + status + ", response='" + response
        + '\'' + '}';
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getApiName() {
    return apiName;
  }

  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getUserIp() {
    return userIp;
  }

  public void setUserIp(String userIp) {
    this.userIp = userIp;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

}
