package com.machloop.fpc.cms.center.system.service;

import java.util.Map;

import com.machloop.fpc.cms.center.system.bo.DeviceLoginSettingBO;

/**
 * @author guosk
 *
 * create at 2021年11月8日, fpc-cms-center
 */
public interface DeviceLoginService {

  Map<String, Object> queryDeviceLoginSettings();

  void updateDeviceLoginSettings(DeviceLoginSettingBO deviceLoginSetting);

}
