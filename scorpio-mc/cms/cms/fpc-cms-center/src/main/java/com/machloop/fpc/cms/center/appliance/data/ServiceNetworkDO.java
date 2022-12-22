package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public class ServiceNetworkDO extends BaseDO {

  private String serviceId;
  private String networkId;
  private String networkGroupId;

  @Override
  public String toString() {
    return "ServiceNetworkDO [serviceId=" + serviceId + ", networkId=" + networkId
        + ", networkGroupId=" + networkGroupId + "]";
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getNetworkGroupId() {
    return networkGroupId;
  }

  public void setNetworkGroupId(String networkGroupId) {
    this.networkGroupId = networkGroupId;
  }
}

