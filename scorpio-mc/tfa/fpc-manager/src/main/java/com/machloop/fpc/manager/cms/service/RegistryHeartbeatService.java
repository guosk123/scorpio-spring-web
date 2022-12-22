package com.machloop.fpc.manager.cms.service;

import java.util.Map;

/**
 * @author liyongjun
 *
 * create at 2019年12月4日, fpc-manager
 */
public interface RegistryHeartbeatService {

  void init();

  Map<String, String> register();

  void heartbeat();

  void setCmsIp(String cmsIp);

  String getCmsIp();

  String getFpcIp();

  String getSerialNumber();

  boolean isAlive();

}
