package com.machloop.fpc.manager.system.service;

import java.util.Map;

public interface CentralService {

  Map<String, Object> queryCmsSetting();

  void updateCmsSetting(String cmsIp, String state);
}
