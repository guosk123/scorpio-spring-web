package com.machloop.fpc.cms.center.sensor.bo;

import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;

/**
 * @author guosk
 *
 * create at 2022年3月10日, fpc-cms-center
 */
public class SensorNetworkPermBO implements LogAudit {

  private String userId;
  private String userName;
  private String networkIds;
  private String networkNames;
  private String networkGroupIds;
  private String networkGroupNames;

  private boolean serviceUser;

  /**
   * @see com.machloop.alpha.webapp.base.LogAudit#toAuditLogText(int)
   */
  @Override
  public String toAuditLogText(int auditLogAction) {
    StringBuilder builder = new StringBuilder();
    switch (auditLogAction) {
      case LogHelper.AUDIT_LOG_ACTION_UPDATE:
        builder.append("修改用户网络权限：");
        break;
      default:
        return "";
    }
    builder.append("用户ID=").append(userId).append(";");
    builder.append("所具有的网络权限集合=").append(networkIds).append(";");
    builder.append("所具有的网络组权限集合=").append(networkGroupIds).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "SensorNetworkPermBO [userId=" + userId + ", userName=" + userName + ", networkIds="
        + networkIds + ", networkNames=" + networkNames + ", networkGroupIds=" + networkGroupIds
        + ", networkGroupNames=" + networkGroupNames + ", serviceUser=" + serviceUser + "]";
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getNetworkIds() {
    return networkIds;
  }

  public void setNetworkIds(String networkIds) {
    this.networkIds = networkIds;
  }

  public String getNetworkNames() {
    return networkNames;
  }

  public void setNetworkNames(String networkNames) {
    this.networkNames = networkNames;
  }

  public String getNetworkGroupIds() {
    return networkGroupIds;
  }

  public void setNetworkGroupIds(String networkGroupIds) {
    this.networkGroupIds = networkGroupIds;
  }

  public String getNetworkGroupNames() {
    return networkGroupNames;
  }

  public void setNetworkGroupNames(String networkGroupNames) {
    this.networkGroupNames = networkGroupNames;
  }

  public boolean getServiceUser() {
    return serviceUser;
  }

  public void setServiceUser(boolean serviceUser) {
    this.serviceUser = serviceUser;
  }

}
