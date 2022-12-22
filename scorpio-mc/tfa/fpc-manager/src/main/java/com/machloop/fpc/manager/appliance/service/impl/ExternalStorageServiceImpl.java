package com.machloop.fpc.manager.appliance.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.appliance.bo.ExternalStorageBO;
import com.machloop.fpc.manager.appliance.dao.ExternalStorageDao;
import com.machloop.fpc.manager.appliance.data.ExternalStorageDO;
import com.machloop.fpc.manager.appliance.service.ExternalStorageService;

/**
 * @author guosk
 *
 * create at 2020年11月13日, fpc-manager
 */
@Service
public class ExternalStorageServiceImpl implements ExternalStorageService {

  @Autowired
  private ExternalStorageDao externalStorageDao;

  /**
   * @see com.machloop.fpc.manager.appliance.service.ExternalStorageService#queryExternalStorages(java.lang.String, java.lang.String)
   */
  @Override
  public List<ExternalStorageBO> queryExternalStorages(String usage, String type) {
    List<
        ExternalStorageDO> externalStorages = externalStorageDao.queryExternalStorages(usage, type);

    return externalStorages.stream().map(externalStorageDO -> {
      ExternalStorageBO externalStorageBO = new ExternalStorageBO();
      BeanUtils.copyProperties(externalStorageDO, externalStorageBO);
      externalStorageBO.setUpdateTime(DateUtils.toStringISO8601(externalStorageDO.getUpdateTime()));

      return externalStorageBO;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.ExternalStorageService#queryExternalStorage(java.lang.String)
   */
  @Override
  public ExternalStorageBO queryExternalStorage(String id) {
    ExternalStorageDO externalStorageDO = externalStorageDao.queryExternalStorage(id);

    ExternalStorageBO externalStorageBO = new ExternalStorageBO();
    BeanUtils.copyProperties(externalStorageDO, externalStorageBO);
    externalStorageBO.setUpdateTime(DateUtils.toStringISO8601(externalStorageDO.getUpdateTime()));
    return externalStorageBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.ExternalStorageService#saveExternalStorage(com.machloop.fpc.manager.appliance.bo.ExternalStorageBO, java.lang.String)
   */
  @Override
  public ExternalStorageBO saveExternalStorage(ExternalStorageBO externalStorageBO,
      String operatorId) {
    ExternalStorageDO existName = externalStorageDao
        .queryExternalStorageByName(externalStorageBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "存储服务器名称已经存在");
    }

    ExternalStorageDO externalStorageDO = new ExternalStorageDO();
    BeanUtils.copyProperties(externalStorageBO, externalStorageDO);
    externalStorageDO.setOperatorId(operatorId);
    ExternalStorageDO saveExternalStorage = externalStorageDao
        .saveExternalStorage(externalStorageDO);

    BeanUtils.copyProperties(saveExternalStorage, externalStorageBO);
    return externalStorageBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.ExternalStorageService#updateExternalStorage(java.lang.String, com.machloop.fpc.manager.appliance.bo.ExternalStorageBO, java.lang.String)
   */
  @Override
  public ExternalStorageBO updateExternalStorage(String id, ExternalStorageBO externalStorageBO,
      String operatorId) {
    ExternalStorageDO externalStorageDO = externalStorageDao.queryExternalStorage(id);
    if (StringUtils.isBlank(externalStorageDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "存储服务器不存在");
    }

    ExternalStorageDO existName = externalStorageDao
        .queryExternalStorageByName(externalStorageBO.getName());
    if (StringUtils.isNotBlank(existName.getId()) && !StringUtils.equals(existName.getId(), id)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "存储服务器名称已经存在");
    }

    if (StringUtils.isBlank(externalStorageBO.getPassword())) {
      externalStorageBO.setPassword(externalStorageDO.getPassword());
    }
    BeanUtils.copyProperties(externalStorageBO, externalStorageDO);
    externalStorageDO.setId(id);
    externalStorageDO.setOperatorId(operatorId);
    externalStorageDao.updateExternalStorage(externalStorageDO);

    return queryExternalStorage(id);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.ExternalStorageService#deleteExternalStorage(java.lang.String, java.lang.String)
   */
  @Override
  public ExternalStorageBO deleteExternalStorage(String id, String operatorId) {
    ExternalStorageDO exist = externalStorageDao.queryExternalStorage(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "存储服务器不存在");
    }

    externalStorageDao.deleteExternalStorage(id, operatorId);

    return queryExternalStorage(id);
  }

}
