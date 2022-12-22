package com.machloop.fpc.manager.appliance.dao;

import java.util.List;

import com.machloop.fpc.manager.appliance.data.ExternalStorageDO;

/**
 * @author guosk
 *
 * create at 2020年11月13日, fpc-manager
 */
public interface ExternalStorageDao {

  List<ExternalStorageDO> queryExternalStorages(String usage, String type);

  ExternalStorageDO queryExternalStorage(String id);

  ExternalStorageDO queryExternalStorageByName(String name);

  ExternalStorageDO saveExternalStorage(ExternalStorageDO externalStorageDO);

  int updateExternalStorage(ExternalStorageDO externalStorageDO);

  int deleteExternalStorage(String id, String operatorId);

}
