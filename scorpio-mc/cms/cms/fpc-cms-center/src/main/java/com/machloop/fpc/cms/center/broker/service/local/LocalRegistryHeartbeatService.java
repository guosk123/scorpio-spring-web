package com.machloop.fpc.cms.center.broker.service.local;

import java.util.Map;

/**
 * @author guosk
 *
 * create at 2021年12月8日, fpc-cms-center
 */
public interface LocalRegistryHeartbeatService {

  void init();

  Map<String, String> register();

  void heartbeat();

  String getParentCmsIp();

  void setParentCmsIp(String parentCmsIp);

  String getLocalCmsIp();

  String getSerialNumber();

  boolean isAlive();

}
