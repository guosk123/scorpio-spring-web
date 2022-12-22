package com.machloop.fpc.cms.center.appliance.data;

/**
 * @author chenshimiao
 * 
 * create at 2022/8/18 10:59 AM,cms
 * @version 1.0
 */
public class FilterRuleDO extends BaselineSettingDO {

  private String assignId;
  private String name;
  private String tuple;
  private String state;
  private String description;
  private Integer priority;
  private String priorId;
  private String nextId;
  private String networkId;

  @Override
  public String toString() {
    return "FilterRuleBO [assignId=" + assignId + ", name=" + name + ", tuple=" + tuple + ", state="
        + state + ", description=" + description + ", priority=" + priority + ", priorId=" + priorId
        + ", nextId=" + nextId + ", networkId=" + networkId + "]";
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTuple() {
    return tuple;
  }

  public void setTuple(String tuple) {
    this.tuple = tuple;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public String getPriorId() {
    return priorId;
  }

  public void setPriorId(String priorId) {
    this.priorId = priorId;
  }

  public String getNextId() {
    return nextId;
  }

  public void setNextId(String nextId) {
    this.nextId = nextId;
  }

  @Override
  public String getNetworkId() {
    return networkId;
  }

  @Override
  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }
}
