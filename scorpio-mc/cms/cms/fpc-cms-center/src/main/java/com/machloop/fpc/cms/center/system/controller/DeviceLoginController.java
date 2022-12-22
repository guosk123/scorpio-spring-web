package com.machloop.fpc.cms.center.system.controller;

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.cms.center.system.bo.DeviceLoginSettingBO;
import com.machloop.fpc.cms.center.system.service.DeviceLoginService;
import com.machloop.fpc.cms.center.system.vo.DeviceLoginSettingVO;

/**
 * @author guosk
 *
 * create at 2021年11月8日, fpc-cms-center
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/system")
public class DeviceLoginController {

  @Autowired
  private DeviceLoginService deviceLoginService;

  @GetMapping("/device-login-settings")
  @Secured({"PERM_SYS_USER"})
  public Map<String, Object> queryDeviceLoginSettings() {

    return deviceLoginService.queryDeviceLoginSettings();
  }

  @PutMapping("/device-login-settings")
  @Secured({"PERM_SYS_USER"})
  public void updateDeviceLoginSettings(DeviceLoginSettingVO deviceLoginSettingVO) {
    DeviceLoginSettingBO deviceLoginSettingBO = new DeviceLoginSettingBO();
    BeanUtils.copyProperties(deviceLoginSettingVO, deviceLoginSettingBO);

    deviceLoginService.updateDeviceLoginSettings(deviceLoginSettingBO);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, deviceLoginSettingBO);
  }

}
