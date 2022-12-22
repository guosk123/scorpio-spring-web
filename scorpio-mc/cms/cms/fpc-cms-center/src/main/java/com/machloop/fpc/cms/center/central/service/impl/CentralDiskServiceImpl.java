package com.machloop.fpc.cms.center.central.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.fpc.cms.center.central.bo.CentralDiskBO;
import com.machloop.fpc.cms.center.central.bo.CentralRaidBO;
import com.machloop.fpc.cms.center.central.dao.CentralDiskDao;
import com.machloop.fpc.cms.center.central.dao.CentralRaidDao;
import com.machloop.fpc.cms.center.central.data.CentralDiskDO;
import com.machloop.fpc.cms.center.central.data.CentralRaidDO;
import com.machloop.fpc.cms.center.central.service.CentralDiskService;

@Service
public class CentralDiskServiceImpl implements CentralDiskService {

  @Autowired
  private CentralRaidDao centralRaidDao;

  @Autowired
  private CentralDiskDao centralDiskDao;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralDiskService#queryCentralRaids(java.lang.String, java.lang.String)
   */
  @Override
  public List<CentralRaidBO> queryCentralRaids(String deviceType, String deviceSerialNumber) {
    Map<String, String> stateDict = dictManager.getBaseDict().getItemMap("device_raid_state");

    List<CentralRaidDO> centralRaidDOList = centralRaidDao.queryRaidBySerialNumber(deviceType,
        deviceSerialNumber);
    List<CentralRaidBO> centralRaidBOList = Lists
        .newArrayListWithExpectedSize(centralRaidDOList.size());
    for (CentralRaidDO centralRaidDO : centralRaidDOList) {
      CentralRaidBO centralRaidBO = new CentralRaidBO();
      BeanUtils.copyProperties(centralRaidDO, centralRaidBO);

      centralRaidBO.setStateText(MapUtils.getString(stateDict, centralRaidBO.getState(), ""));
      centralRaidBOList.add(centralRaidBO);
    }

    return centralRaidBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralDiskService#queryCentralDisks(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<CentralDiskBO> queryCentralDisks(String deviceType, String deviceSerialNumber,
      String raidNo) {
    Map<String, String> stateDict = dictManager.getBaseDict().getItemMap("device_disk_state");
    Map<String, String> mediumDict = dictManager.getBaseDict().getItemMap("device_disk_medium");

    if (StringUtils.isBlank(deviceSerialNumber)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备编号不能为空");
    }

    List<CentralDiskDO> centralDiskDOList = centralDiskDao.queryCentralDisks(deviceType,
        deviceSerialNumber, raidNo);

    List<CentralDiskBO> centralDiskBOList = Lists
        .newArrayListWithExpectedSize(centralDiskDOList.size());
    for (CentralDiskDO centralDiskDO : centralDiskDOList) {
      CentralDiskBO centralDiskBO = new CentralDiskBO();
      BeanUtils.copyProperties(centralDiskDO, centralDiskBO);

      centralDiskBO.setMediumText(MapUtils.getString(mediumDict, centralDiskDO.getMedium(), ""));
      centralDiskBO.setStateText(MapUtils.getString(stateDict, centralDiskDO.getState(), ""));
      centralDiskBO
          .setRebuildProgressText(StringUtils.isBlank(centralDiskDO.getRebuildProgress()) ? "N/A"
              : centralDiskDO.getRebuildProgress() + "%");
      centralDiskBO
          .setCopybackProgressText(StringUtils.isBlank(centralDiskDO.getCopybackProgress()) ? "N/A"
              : centralDiskDO.getCopybackProgress() + "%");

      centralDiskBOList.add(centralDiskBO);
    }

    return centralDiskBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralDiskService#countCentralDiskByState()
   */
  @Override
  public List<Map<String, Object>> countCentralDiskByState() {
    Map<String, String> stateDict = dictManager.getBaseDict().getItemMap("device_disk_state");

    List<Map<String, Object>> list = centralDiskDao.countCentralDiskByState();
    for (Map<String, Object> map : list) {
      map.put("stateText", MapUtils.getString(stateDict, map.get("state"), ""));
    }

    return list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralDiskService#deleteCentralDisk(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCentralDisk(String deviceType, String deviceSerialNumber) {
    int deleteCount = centralRaidDao.deleteCentralRaids(deviceType, deviceSerialNumber);
    deleteCount += centralDiskDao.deleteCentralDisks(deviceType, deviceSerialNumber);

    return deleteCount;
  }

}
