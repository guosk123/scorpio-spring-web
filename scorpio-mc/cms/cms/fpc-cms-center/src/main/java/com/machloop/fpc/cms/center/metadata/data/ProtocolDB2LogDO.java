package com.machloop.fpc.cms.center.metadata.data;

import java.util.Map;

/**
 * @author minjiajun
 *
 * create at 2022年8月22日, fpc-cms-center
 */
public class ProtocolDB2LogDO extends AbstractLogRecordDO {

  private String codePoint;
  private Map<String, String> data;

  @Override
  public String toString() {
    return "ProtocolDB2LogDO [codePoint=" + codePoint + ", data=" + data + "]";
  }

  public String getCodePoint() {
    return codePoint;
  }

  public void setCodePoint(String codePoint) {
    this.codePoint = codePoint;
  }

  public Map<String, String> getData() {
    return data;
  }

  public void setData(Map<String, String> data) {
    this.data = data;
  }
}
