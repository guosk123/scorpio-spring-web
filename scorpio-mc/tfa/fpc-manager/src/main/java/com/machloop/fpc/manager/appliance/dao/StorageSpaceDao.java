package com.machloop.fpc.manager.appliance.dao;

import java.util.List;

import com.machloop.fpc.manager.appliance.data.StorageSpaceDO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public interface StorageSpaceDao {

  List<StorageSpaceDO> queryStorageSpaces();

  int updateStorageSpace(String spaceType, long capacity, String operatorId);

}
