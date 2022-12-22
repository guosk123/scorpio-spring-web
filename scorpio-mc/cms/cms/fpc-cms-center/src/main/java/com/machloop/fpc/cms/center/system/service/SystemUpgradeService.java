package com.machloop.fpc.cms.center.system.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author guosk
 *
 * create at 2021年1月14日, fpc-manager
 */
public interface SystemUpgradeService {

  Map<String, String> queryCurrentUpgradeVersion();

  Map<String, Object> queryUpgradeLogs(long cursor);

  void systemUpgrade(MultipartFile file);
}
