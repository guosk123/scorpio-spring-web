package com.machloop.fpc.manager.appliance.service;

import java.util.List;

import com.machloop.fpc.manager.appliance.bo.ExternalStorageBO;

/**
 * @author guosk
 *
 * create at 2020年11月13日, fpc-manager
 */
public interface ExternalStorageService {

  List<ExternalStorageBO> queryExternalStorages(String usage, String type);

  ExternalStorageBO queryExternalStorage(String id);

  ExternalStorageBO saveExternalStorage(ExternalStorageBO externalStorageBO, String operatorId);

  ExternalStorageBO updateExternalStorage(String id, ExternalStorageBO externalStorageBO,
      String operatorId);

  ExternalStorageBO deleteExternalStorage(String id, String operatorId);

}
