package com.machloop.fpc.manager.analysis.vo;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
public class ScenarioTaskQueryVO {

  private String type;
  private String state;

  @Override
  public String toString() {
    return "ScenarioTaskQueryVO [type=" + type + ", state=" + state + "]";
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}
