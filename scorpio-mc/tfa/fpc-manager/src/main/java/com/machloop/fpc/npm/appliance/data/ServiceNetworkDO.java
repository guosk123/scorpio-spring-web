package com.machloop.fpc.npm.appliance.data;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public class ServiceNetworkDO extends BaseDO {

  private String serviceId;
  private String networkId;

  @Override
  public String toString() {
    return "ServiceNetworkDO [serviceId=" + serviceId + ", networkId=" + networkId + "]";
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

}
