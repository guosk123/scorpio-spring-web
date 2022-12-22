package com.machloop.fpc.manager.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/12/8 11:50 AM,cms
 * @version 1.0
 */
public class DomainWhiteListDO extends BaseOperateDO {

  private String domainWhiteListInCmsId;
  private String name;
  private String domain;
  private String description;

  @Override
  public String toString() {
    return "DomainWhiteListDO{" + "domainWhiteListInCmsId='" + domainWhiteListInCmsId + '\''
        + ", name='" + name + '\'' + ", domain='" + domain + '\'' + ", description='" + description
        + '\'' + '}';
  }

  public String getDomainWhiteListInCmsId() {
    return domainWhiteListInCmsId;
  }

  public void setDomainWhiteListInCmsId(String domainWhiteListInCmsId) {
    this.domainWhiteListInCmsId = domainWhiteListInCmsId;
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
