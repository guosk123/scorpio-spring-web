package com.machloop.fpc.manager.appliance.service.impl;

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.machloop.alpha.common.dict.DictManager;
import com.machloop.fpc.manager.appliance.bo.StoragePolicyBO;
import com.machloop.fpc.manager.appliance.dao.StoragePolicyDao;
import com.machloop.fpc.manager.appliance.data.StoragePolicyDO;
import com.machloop.fpc.manager.appliance.service.StoragePolicyService;

@Service
public class StoragePolicyServiceImpl implements StoragePolicyService {

  @Autowired
  private DictManager dictManager;

  @Autowired
  private StoragePolicyDao storagePolicyDao;

  /**
   * @see com.machloop.fpc.manager.appliance.service.StoragePolicyService#queryStoragePolicy()
   */
  @Override
  public StoragePolicyBO queryStoragePolicy() {
    Map<String, String> compressAactionDict = dictManager.getBaseDict()
        .getItemMap("appliance_storage_compress");
    Map<String, String> encryptActionDict = dictManager.getBaseDict()
        .getItemMap("appliance_storage_encrypt");

    StoragePolicyDO storagePolicyDO = storagePolicyDao.queryStoragePolicy();

    StoragePolicyBO storagePolicyBO = new StoragePolicyBO();
    BeanUtils.copyProperties(storagePolicyDO, storagePolicyBO);

    storagePolicyBO.setCompressActionText(
        compressAactionDict.getOrDefault(storagePolicyBO.getCompressAction(), ""));
    storagePolicyBO.setEncryptActionText(
        encryptActionDict.getOrDefault(storagePolicyBO.getEncryptAction(), ""));

    return storagePolicyBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.StoragePolicyService#updateStoragePolicy(com.machloop.fpc.manager.appliance.bo.StoragePolicyBO, java.lang.String)
   */
  @Override
  public StoragePolicyBO updateStoragePolicy(StoragePolicyBO storagePolicyBO, String operatorId) {

    StoragePolicyBO oldStoragePolicyBO = queryStoragePolicy();

    StoragePolicyDO storagePolicyDO = new StoragePolicyDO();
    BeanUtils.copyProperties(storagePolicyBO, storagePolicyDO);
    storagePolicyDO.setId(oldStoragePolicyBO.getId());
    storagePolicyDO.setOperatorId(operatorId);

    storagePolicyDao.updateStoragePolicy(storagePolicyDO);

    return queryStoragePolicy();
  }

}
