package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/12/9 5:06 PM,cms
 * @version 1.0
 */
public class DomainWhiteListDO extends BaseOperateDO {

  private String assignId;
  private String name;
  private String domain;
  private String description;

  @Override
  public String toString() {
    return "DomainWhiteListDO{" + "assignId='" + assignId + '\'' + ", name='" + name + '\''
        + ", domain='" + domain + '\'' + ", description='" + description + '\'' + '}';
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

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
