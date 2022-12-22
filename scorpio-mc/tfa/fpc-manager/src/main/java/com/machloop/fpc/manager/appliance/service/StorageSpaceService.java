package com.machloop.fpc.manager.appliance.service;

import java.util.List;

import com.machloop.fpc.manager.appliance.bo.StorageSpaceBO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public interface StorageSpaceService {

  List<StorageSpaceBO> queryStorageSpaces();

  void updateStorageSpaces(List<StorageSpaceBO> storageSpaces, String operatorId);

}
