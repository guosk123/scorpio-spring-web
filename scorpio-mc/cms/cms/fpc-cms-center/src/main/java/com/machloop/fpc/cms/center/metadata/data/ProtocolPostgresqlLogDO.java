package com.machloop.fpc.cms.center.metadata.data;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
public class ProtocolPostgresqlLogDO extends AbstractLogRecordDO {

  private String username;
  private String databaseName;
  private String cmd;
  private String error;
  private long delaytime;

  @Override
  public String toString() {
    return "ProtocolPostgresqlLogDO [username=" + username + ", databaseName=" + databaseName
        + ", cmd=" + cmd + ", error=" + error + ", delaytime=" + delaytime + "]";
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
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
