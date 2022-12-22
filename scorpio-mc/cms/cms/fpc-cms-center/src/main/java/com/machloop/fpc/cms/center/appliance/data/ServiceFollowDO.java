package com.machloop.fpc.cms.center.appliance.data;

import java.util.Date;
import com.machloop.alpha.common.base.BaseDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public class ServiceFollowDO extends BaseDO {

  private String userId;
  private String serviceId;
  private String networkId;
  private String networkGroupId;
  private Date followTime;

  @Override
  public String toString() {
    return "ServiceFollowDO [userId=" + userId + ", serviceId=" + serviceId + ", networkId="
        + networkId + ", networkGroupId=" + networkGroupId + ", followTime=" + followTime + "]";
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

  public String getNetworkGroupId() {
    return networkGroupId;
  }

  public void setNetworkGroupId(String networkGroupId) {
    this.networkGroupId = networkGroupId;
  }

  public Date getFollowTime() {
    return followTime;
  }

  public void setFollowTime(Date followTime) {
    this.followTime = followTime;
  }

}
