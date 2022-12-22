package com.machloop.fpc.cms.npm.analysis.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
public class SuricataRuleClasstypeDO extends BaseOperateDO {

  private String name;

  private String assignId;

  @Override
  public String toString() {
    return "SuricataRuleClasstypeDO [name=" + name + ", assignId=" + assignId + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAssignId() {
    return assignId;
  }

  public void setAssignId(String assignId) {
    this.assignId = assignId;
  }
}
