package com.machloop.fpc.manager.appliance.dao;

import com.machloop.fpc.manager.appliance.data.StoragePolicyDO;

/**
 * @author liyongjun
 *
 * create at 2019年9月5日, fpc-manager
 */
public interface StoragePolicyDao {

  StoragePolicyDO queryStoragePolicy();

  int updateStoragePolicy(StoragePolicyDO storagePolicyDO);
}
