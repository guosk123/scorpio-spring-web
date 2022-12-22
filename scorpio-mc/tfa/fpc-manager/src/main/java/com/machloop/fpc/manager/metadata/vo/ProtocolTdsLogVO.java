package com.machloop.fpc.manager.metadata.vo;

/**
 * @author guosk
 *
 * create at 2021年7月1日, fpc-manager
 */
public class ProtocolTdsLogVO extends AbstractLogRecordVO {

  private String cmd;
  private String error;
  private long delaytime;

  @Override
  public String toString() {
    return "ProtocolTdsLogVO [cmd=" + cmd + ", error=" + error + ", delaytime=" + delaytime + "]";
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
