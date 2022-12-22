package com.machloop.fpc.cms.center.appliance.data;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月19日, fpc-cms-center
 */
public class AlertScopeDO extends BaseDO {

  private String alertId;
  private String sourceType;
  private String networkId;
  private String serviceId;

  @Override
  public String toString() {
    return "AlertScopeDO [alertId=" + alertId + ", sourceType=" + sourceType + ", networkId="
        + networkId + ", serviceId=" + serviceId + "]";
  }

  public String getAlertId() {
    return alertId;
  }

  public void setAlertId(String alertId) {
    this.alertId = alertId;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getNetworkId() {
    return networkId;
  }

  public void setNetworkId(String networkId) {
    this.networkId = networkId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

}
