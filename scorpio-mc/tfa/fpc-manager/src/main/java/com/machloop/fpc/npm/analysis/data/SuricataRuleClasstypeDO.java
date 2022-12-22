package com.machloop.fpc.npm.analysis.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2022年4月2日, fpc-manager
 */
public class SuricataRuleClasstypeDO extends BaseOperateDO {

  private String name;

  private String classtypeInCmsId;

  @Override
  public String toString() {
    return "SuricataRuleClasstypeDO [name=" + name + ", assignId=" + classtypeInCmsId + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClasstypeInCmsId() {
    return classtypeInCmsId;
  }

  public void setClasstypeInCmsId(String classtypeInCmsId) {
    this.classtypeInCmsId = classtypeInCmsId;
  }

}
