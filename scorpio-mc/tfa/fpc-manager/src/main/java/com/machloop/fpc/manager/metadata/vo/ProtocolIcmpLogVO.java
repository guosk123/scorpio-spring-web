package com.machloop.fpc.manager.metadata.vo;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
public class ProtocolIcmpLogVO extends AbstractLogRecordVO {

  private int version;
  private String result;
  private long requestDataLen;
  private long responseDataLen;
  private int onlyRequest;
  private int onlyResponse;
  private Integer payloadHashInconsistent;

  @Override
  public String toString() {
    return "ProtocolIcmpLogVO [version=" + version + ", result=" + result + ", requestDataLen="
        + requestDataLen + ", responseDataLen=" + responseDataLen + ", onlyRequest=" + onlyRequest
        + ", onlyResponse=" + onlyResponse + ", payloadHashInconsistent=" + payloadHashInconsistent
        + "]";
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public long getRequestDataLen() {
    return requestDataLen;
  }

  public void setRequestDataLen(long requestDataLen) {
    this.requestDataLen = requestDataLen;
  }

  public long getResponseDataLen() {
    return responseDataLen;
  }

  public void setResponseDataLen(long responseDataLen) {
    this.responseDataLen = responseDataLen;
  }

  public int getOnlyRequest() {
    return onlyRequest;
  }

  public void setOnlyRequest(int onlyRequest) {
    this.onlyRequest = onlyRequest;
  }

  public int getOnlyResponse() {
    return onlyResponse;
  }

  public void setOnlyResponse(int onlyResponse) {
    this.onlyResponse = onlyResponse;
  }

  public Integer getPayloadHashInconsistent() {
    return payloadHashInconsistent;
  }

  public void setPayloadHashInconsistent(Integer payloadHashInconsistent) {
    this.payloadHashInconsistent = payloadHashInconsistent;
  }

}
