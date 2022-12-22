package com.machloop.fpc.npm.appliance.dao;

import java.util.List;

import com.machloop.fpc.npm.appliance.data.BaselineSettingDO;

/**
 * @author guosk
 *
 * create at 2021年4月21日, fpc-manager
 */
public interface BaselineSettingDao {

  List<BaselineSettingDO> queryBaselineSettings();

  List<BaselineSettingDO> queryBaselineSettings(String sourceType, String networkId,
      String serviceId, String category);

  int batchUpdateBaselineSetting(List<BaselineSettingDO> baselineSettingDOList);

  int deleteBaselineSetting(String sourceType, String networkId, String serviceId);

}
