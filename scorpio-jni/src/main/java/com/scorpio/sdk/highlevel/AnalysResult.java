package com.machloop.iosp.sdk.highlevel;

public class AnalysResult {
  private String analysMsg;
  private String datanodeMsg;

  AnalysResult(String analysMsg, String datanodeMsg) {
    this.analysMsg = analysMsg;
    this.datanodeMsg = datanodeMsg;
  }

  @Override
  public String toString() {
    return "AnalysResult [analysMsg=" + analysMsg + ", datanodeMsg=" + datanodeMsg + "]";
  }

  public String getAnalysMsg() {
    return analysMsg;
  }

  public String getDatanodeMsg() {
    return datanodeMsg;
  }
}
