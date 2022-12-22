package com.machloop.fpc.cms.npm.analysis.bo;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/13 11:27 AM,cms
 * @version 1.0
 */
public class MitreAttackBO {

  private String id;
  private String name;
  private String parentId;

  private int ruleSize;
  private long alertSize;

  @Override
  public String toString() {
    return "MitreAttackBO [id=" + id + ", name=" + name + ", parentId=" + parentId + ", ruleSize="
        + ruleSize + ", alertSize=" + alertSize + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public int getRuleSize() {
    return ruleSize;
  }

  public void setRuleSize(int ruleSize) {
    this.ruleSize = ruleSize;
  }

  public long getAlertSize() {
    return alertSize;
  }

  public void setAlertSize(long alertSize) {
    this.alertSize = alertSize;
  }
}
