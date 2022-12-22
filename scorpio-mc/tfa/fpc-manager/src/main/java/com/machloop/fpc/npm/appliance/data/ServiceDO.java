package com.machloop.fpc.npm.appliance.data;

import com.machloop.alpha.common.base.BaseOperateDO;

/**
 * @author guosk
 *
 * create at 2020年11月12日, fpc-manager
 */
public class ServiceDO extends BaseOperateDO {

  private String name;
  private String application;
  private String serviceInCmsId;
  private String description;

  @Override
  public String toString() {
    return "ServiceDO [name=" + name + ", application=" + application + ", serviceInCmsId="
        + serviceInCmsId + ", description=" + description + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getServiceInCmsId() {
    return serviceInCmsId;
  }

  public void setServiceInCmsId(String serviceInCmsId) {
    this.serviceInCmsId = serviceInCmsId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
