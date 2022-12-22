package com.machloop.fpc.manager.appliance.service;

import com.machloop.fpc.manager.appliance.bo.StoragePolicyBO;

/**
 * @author liyongjun
 *
 * create at 2019年9月5日, fpc-manager
 */
public interface StoragePolicyService {

  StoragePolicyBO queryStoragePolicy();

  StoragePolicyBO updateStoragePolicy(StoragePolicyBO storagePolicyBO, String operatorId);
}
