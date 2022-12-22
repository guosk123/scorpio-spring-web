package com.machloop.fpc.cms.center.central.service;

import java.util.Map;

public interface CentralService {

  Map<String, Object> queryCmsSetting();

  void updateCmsSetting(String parentCmsIp, String state);
}
