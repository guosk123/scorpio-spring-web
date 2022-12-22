package com.machloop.fpc.manager.boot.configuration;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.machloop.alpha.webapp.security.service.impl.SessionExpireInterceptor;

/**
 * @author liyongjun
 *
 * create at 2019年12月4日, fpc-manager
 */
@Configuration
public class SessionExpireConfiguration {

  @Autowired
  private SessionExpireInterceptor sessionExpireInterceptor;

  @PostConstruct
  public void init() {

    // 概览页，网口信息和硬盘信息
    sessionExpireInterceptor.addExceptUri("/webapi/fpc-v1/system/device-disks");
    sessionExpireInterceptor.addExceptUri("/webapi/fpc-v1/system/device-netifs");

    // 全包留存查询任务
    sessionExpireInterceptor.addExceptUri("/webapi/fpc-v1/appliance/transmition-tasks");

    sessionExpireInterceptor.addExceptUri("/webapi/fpc-v1/system/runtime-environments");

    sessionExpireInterceptor.addExceptUri("/webapi/fpc-v1/analysis/scenario-tasks");
  }

}
