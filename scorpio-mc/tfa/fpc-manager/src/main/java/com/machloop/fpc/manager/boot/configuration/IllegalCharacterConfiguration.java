package com.machloop.fpc.manager.boot.configuration;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.machloop.alpha.webapp.system.service.impl.IllegalCharacterService;

/**
 * @author guosk
 *
 * create at 2022年4月21日, fpc-manager
 */
@Configuration
public class IllegalCharacterConfiguration {

  @Autowired
  private IllegalCharacterService illegalCharacterService;

  @PostConstruct
  public void init() {

    illegalCharacterService.addExceptUri("/webapi/fpc-v1/suricata/rules");
    illegalCharacterService.addExceptUri("/webapi/fpc-v1/appliance/transmition-tasks");
    illegalCharacterService.addExceptUri("/webapi/fpc-v1/appliance/send-rule");
    illegalCharacterService.addExceptUri("/webapi/fpc-v1/appliance/external-receiver");
    illegalCharacterService.addExceptUri("/webapi/fpc-v1/appliance/domain-white");
  }

}
