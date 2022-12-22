package com.machloop.fpc.npm.analysis.data;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2022年4月2日, fpc-manager
 */
public class MitreAttackDO extends BaseDO {

  private String name;
  private String parentId;

  @Override
  public String toString() {
    return "MitreAttackDO [name=" + name + ", parentId=" + parentId + "]";
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

}
