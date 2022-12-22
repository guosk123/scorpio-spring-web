package com.machloop.fpc.manager.knowledge.service;

import java.nio.file.Path;
import java.util.List;

import com.machloop.fpc.manager.knowledge.bo.DecryptSettingBO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月20日, fpc-manager
 */
public interface DecryptService {

  List<DecryptSettingBO> queryDecryptSettings(String ipAddress, String port, String protocol);
  
  List<DecryptSettingBO> queryDecryptSettings();

  DecryptSettingBO queryDecryptSetting(String id);

  DecryptSettingBO saveDecryptSetting(DecryptSettingBO decryptSettingBO, Path file,
      String operatorId);

  DecryptSettingBO updateDecryptSetting(String id, DecryptSettingBO decryptSettingBO, Path file,
      String operatorId);

  DecryptSettingBO deleteDecryptSetting(String id, String id2);

}
