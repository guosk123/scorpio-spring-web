package com.machloop.fpc.cms.center.metadata.data;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
public class ProtocolTnsLogDO extends AbstractLogRecordDO {

  private long version;
  private String connectData;
  private String connectResult;
  private String cmd;
  private String error;
  private long delaytime;

  @Override
  public String toString() {
    return "ProtocolTnsLogDO [version=" + version + ", connectData=" + connectData
        + ", connectResult=" + connectResult + ", cmd=" + cmd + ", error=" + error + ", delaytime="
        + delaytime + "]";
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public String getConnectData() {
    return connectData;
  }

  public void setConnectData(String connectData) {
    this.connectData = connectData;
  }

  public String getConnectResult() {
    return connectResult;
  }

  public void setConnectResult(String connectResult) {
    this.connectResult = connectResult;
  }

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public long getDelaytime() {
    return delaytime;
  }

  public void setDelaytime(long delaytime) {
    this.delaytime = delaytime;
  }

}
