package com.machloop.fpc.cms.center.system.vo;

import javax.validation.constraints.NotEmpty;

/**
 * @author guosk
 *
 * create at 2021年11月8日, fpc-cms-center
 */
public class DeviceLoginSettingVO {

  @NotEmpty(message = "登录用户不能为空")
  private String userId;
  @NotEmpty(message = "可登录设备不能为空")
  private String deviceSerialNumbers;

  @Override
  public String toString() {
    return "DeviceLoginSettingVO [userId=" + userId + ", deviceSerialNumbers=" + deviceSerialNumbers
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
