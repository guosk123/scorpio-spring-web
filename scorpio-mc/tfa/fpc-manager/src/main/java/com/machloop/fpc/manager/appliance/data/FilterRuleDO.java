package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author chenshimiao
 *
 * create at 2022/8/9 9:15 AM,cms
 * @version 1.0
 */
public class FilterRuleDO extends BaseOperateDO {
  private String name;
  private String tuple;
  private String storageRuleInCmsId;
  private String state;
  private String description;
  private Integer priority;

  @Override
  public String toString() {
    return "FilterRuleBO [ name=" + name + ", tuple=" + tuple + ", storageRuleInCmsId="
        + storageRuleInCmsId + ", state" + state + ", description=" + description + ", priority"
        + priority + "]";
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

  public String getStorageRuleInCmsId() {
    return storageRuleInCmsId;
  }

  public void setStorageRuleInCmsId(String storageRuleInCmsId) {
    this.storageRuleInCmsId = storageRuleInCmsId;
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
}
