package com.machloop.fpc.cms.center.system.service;

import java.util.Map;

/**
 * @author guosk
 *
 * create at 2021年11月4日, fpc-cms-center
 */
public interface SystemMetricService {

  Map<String, Object> queryRuntimeEnvironment();

  Map<String, Object> queryDeviceCustomInfo();

}
