package com.machloop.fpc.cms.center.sensor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkPermBO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;

/**
 * @author guosk
 *
 * create at 2022年3月10日, fpc-cms-center
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class SensorNetworkPermController {

  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  @GetMapping("/user-network-perms")
  @Secured({"PERM_SERVICE_USER"})
  public Page<SensorNetworkPermBO> querySensorNetworkPerms(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {
    PageRequest page = new PageRequest(pageNumber, pageSize);

    return sensorNetworkPermService.querySensorNetworkPerms(page);
  }

  @PutMapping("/user-network-perms")
  @Secured({"PERM_SERVICE_USER"})
  public void updateSensorNetworkPerms(@RequestParam(required = true) String userId,
      String networkIds, String networkGroupIds) {
    SensorNetworkPermBO sensorNetworkPermBO = new SensorNetworkPermBO();
    sensorNetworkPermBO.setUserId(userId);
    sensorNetworkPermBO.setNetworkIds(networkIds);
    sensorNetworkPermBO.setNetworkGroupIds(networkGroupIds);

    sensorNetworkPermService.updateSensorNetworkPerms(sensorNetworkPermBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, sensorNetworkPermBO);
  }

}
