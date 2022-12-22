package com.machloop.fpc.cms.center.central.controller;

import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.fpc.cms.center.central.bo.CentralDeviceBO;
import com.machloop.fpc.cms.center.central.service.CentralService;
import com.machloop.fpc.cms.center.central.service.FpcService;

@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/central")
public class CentralController {

  @Autowired
  private CentralService centralService;

  @Autowired
  private FpcService fpcService;

  @GetMapping("/cms-settings")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public Map<String, Object> queryCmsSetting() {
    return centralService.queryCmsSetting();
  }

  @GetMapping("/devices/as-list")
  @Secured({"PERM_USER", "PERM_AUDIT_USER", "PERM_SYS_USER"})
  public CentralDeviceBO getSensorTree() {

    return fpcService.queryCentralDevices();
  }

  @PutMapping("/cms-settings")
  @Secured({"PERM_USER"})
  public void updateCmsSetting(@NotEmpty(message = "cmsIp不能为空") String cmsIp,
      @NotEmpty(message = "state不能为空") String state) {
    centralService.updateCmsSetting(cmsIp, state);
  }
}
