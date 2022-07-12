package com.scorpio.metric.system.data;

public class MonitorRaid {

  private String raidNo;
  private String raidLevel;
  private String state;


  @Override
  public String toString() {
    return "MonitorRaid [raidNo=" + raidNo + ", raidLevel=" + raidLevel + ", state=" + state
        + ", toString()=" + super.toString() + "]";
  }

  public String getRaidNo() {
    return raidNo;
  }

  public void setRaidNo(String raidNo) {
    this.raidNo = raidNo;
  }

  public String getRaidLevel() {
    return raidLevel;
  }

  public void setRaidLevel(String raidLevel) {
    this.raidLevel = raidLevel;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}
