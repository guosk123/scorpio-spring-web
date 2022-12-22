package com.machloop.fpc.manager.knowledge.dao;

import java.util.List;

import com.machloop.fpc.manager.knowledge.data.DecryptSettingDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月20日, fpc-manager
 */
public interface DecryptDao {

  List<DecryptSettingDO> queryDecryptSettings(String ipAddress, String port, String protocol);
  
  List<DecryptSettingDO> queryDecryptSettings();

  DecryptSettingDO queryDecryptSetting(String id);

  DecryptSettingDO saveDecryptSetting(DecryptSettingDO decryptSettingDO);

  int updateDecryptSetting(DecryptSettingDO decryptSettingDO);

  int deleteDecryptSetting(String id, String operatorId);

}
