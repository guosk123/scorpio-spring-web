package com.machloop.fpc.npm.appliance.bo;

/**
 * @author guosk
 *
 * create at 2021年5月21日, fpc-manager
 */
public class ServiceFollowBO {

  private String userId;
  private String serviceId;
  private String networkId;
  private String followTime;

  private String state;

  @Override
  public String toString() {
    return "ServiceFollowBO [userId=" + userId + ", serviceId=" + serviceId + ", networkId="
        + networkId + ", followTime=" + followTime + ", state=" + state + "]";
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

  public String getFollowTime() {
    return followTime;
  }

  public void setFollowTime(String followTime) {
    this.followTime = followTime;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

}
