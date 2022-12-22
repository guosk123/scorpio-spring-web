package com.machloop.fpc.cms.center.broker.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;

import com.machloop.fpc.cms.center.broker.service.subordinate.RegistryHeartbeatService;

/**
 * @author guosk
 *
 * create at 2021年11月30日, fpc-cms-center
 */
@Component
@WebEndpoint(id = "assignConfiguration")
public class AssignConfigurationEndpoint {

  @Autowired
  private RegistryHeartbeatService registryHeartbeatService;

  @WriteOperation
  public String assignConfiguration() {
    // 下发全量配置，有效范围为所有下级设备
    registryHeartbeatService.assignmentFullConfigurations("", "", null);

    return "success";
  }

}
