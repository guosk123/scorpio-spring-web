package com.machloop.fpc.npm.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2021年5月21日, fpc-manager
 */
public class ServiceFollowDO extends BaseDO {

  private String userId;
  private String serviceId;
  private String networkId;
  private Date followTime;

  @Override
  public String toString() {
    return "ServiceFollowDO [userId=" + userId + ", serviceId=" + serviceId + ", networkId="
        + networkId + ", followTime=" + followTime + "]";
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
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

  public Date getFollowTime() {
    return followTime;
  }

  public void setFollowTime(Date followTime) {
    this.followTime = followTime;
  }

}
