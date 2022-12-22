package com.machloop.fpc.cms.center.system.bo;

import com.machloop.alpha.webapp.base.LogAudit;

/**
 * @author guosk
 *
 * create at 2021年11月8日, fpc-cms-center
 */
public class DeviceLoginSettingBO implements LogAudit {

  private String userId;
  private String deviceSerialNumbers;

  /**
   * @see com.machloop.alpha.webapp.base.LogAudit#toAuditLogText(int)
   */
  @Override
  public String toAuditLogText(int auditLogAction) {
    StringBuilder builder = new StringBuilder();

    builder.append("配置探针设备单点登录：");
    builder.append("探针设备登录用户ID：").append(userId).append(";");
    builder.append("可登录设备集合：").append(deviceSerialNumbers).append("。");
    return builder.toString();
  }

  @Override
  public String toString() {
    return "DeviceLoginSettingBO [userId=" + userId + ", deviceSerialNumbers=" + deviceSerialNumbers
        + "]";
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getDeviceSerialNumbers() {
    return deviceSerialNumbers;
  }

  public void setDeviceSerialNumbers(String deviceSerialNumbers) {
    this.deviceSerialNumbers = deviceSerialNumbers;
  }

}
