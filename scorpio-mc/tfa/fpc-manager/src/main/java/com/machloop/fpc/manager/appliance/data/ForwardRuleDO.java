package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/7 16:39,IntelliJ IDEA
 *
 */
public class ForwardRuleDO extends BaseOperateDO {

  private String name;
  private String defaultAction;

  private String exceptBpf;
  private String exceptTuple;
  private String forwardRuleInCmsId;
  private String description;

  @Override public String toString() {
    return "ForwardRuleDO{" + "name='" + name + '\'' + ", defaultAction='" + defaultAction + '\''
        + ", exceptBpf='" + exceptBpf + '\'' + ", exceptTuple='" + exceptTuple + '\''
        + ", forwardRuleInCmsId='" + forwardRuleInCmsId + '\'' + ", description='" + description
        + '\'' + '}';
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDefaultAction() {
    return defaultAction;
  }

  public void setDefaultAction(String defaultAction) {
    this.defaultAction = defaultAction;
  }

  public String getExceptBpf() {
    return exceptBpf;
  }

  public void setExceptBpf(String exceptBpf) {
    this.exceptBpf = exceptBpf;
  }

  public String getExceptTuple() {
    return exceptTuple;
  }

  public void setExceptTuple(String exceptTuple) {
    this.exceptTuple = exceptTuple;
  }

  public String getForwardRuleInCmsId() {
    return forwardRuleInCmsId;
  }

  public void setForwardRuleInCmsId(String forwardRuleInCmsId) {
    this.forwardRuleInCmsId = forwardRuleInCmsId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
