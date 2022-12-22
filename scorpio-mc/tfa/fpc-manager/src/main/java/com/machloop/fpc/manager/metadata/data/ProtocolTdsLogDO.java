package com.machloop.fpc.manager.metadata.data;

/**
 * @author guosk
 *
 * create at 2021年7月1日, fpc-manager
 */
public class ProtocolTdsLogDO extends AbstractLogRecordDO {

  private String cmd;
  private String error;
  private long delaytime;

  @Override
  public String toString() {
    return "ProtocolTdsLogDO [cmd=" + cmd + ", error=" + error + ", delaytime=" + delaytime + "]";
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
