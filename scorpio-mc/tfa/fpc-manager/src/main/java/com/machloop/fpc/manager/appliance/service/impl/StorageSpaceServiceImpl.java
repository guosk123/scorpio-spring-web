package com.machloop.fpc.manager.appliance.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.appliance.bo.StorageSpaceBO;
import com.machloop.fpc.manager.appliance.dao.StorageSpaceDao;
import com.machloop.fpc.manager.appliance.data.StorageSpaceDO;
import com.machloop.fpc.manager.appliance.service.StorageSpaceService;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
@Service
public class StorageSpaceServiceImpl implements StorageSpaceService {

  @Autowired
  private StorageSpaceDao storageSpaceDao;

  /**
   * @see com.machloop.fpc.manager.appliance.service.StorageSpaceService#queryStorageSpaces()
   */
  @Override
  public List<StorageSpaceBO> queryStorageSpaces() {
    List<StorageSpaceDO> storageSpaces = storageSpaceDao.queryStorageSpaces();

    List<StorageSpaceBO> storageSpaceBOList = Lists.newArrayListWithCapacity(storageSpaces.size());
    storageSpaces.forEach(storageSpaceDO -> {
      StorageSpaceBO storageSpaceBO = new StorageSpaceBO();
      BeanUtils.copyProperties(storageSpaceDO, storageSpaceBO);
      storageSpaceBO.setUpdateTime(DateUtils.toStringISO8601(storageSpaceDO.getUpdateTime()));

      storageSpaceBOList.add(storageSpaceBO);
    });

    return storageSpaceBOList;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.StorageSpaceService#updateStorageSpaces(java.util.List, java.lang.String)
   */
  @Override
  public void updateStorageSpaces(List<StorageSpaceBO> storageSpaces, String operatorId) {
    storageSpaces.forEach(storageSpace -> {
      storageSpaceDao.updateStorageSpace(storageSpace.getSpaceType(), storageSpace.getCapacity(),
          operatorId);
    });
  }

}
