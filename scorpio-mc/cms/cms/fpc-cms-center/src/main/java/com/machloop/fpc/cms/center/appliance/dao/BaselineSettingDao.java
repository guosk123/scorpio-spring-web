package com.machloop.fpc.cms.center.appliance.dao;

import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.BaselineSettingDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public interface BaselineSettingDao {

  List<BaselineSettingDO> queryBaselineSettings();

  List<BaselineSettingDO> queryBaselineSettings(String sourceType, String networkId,
      String networkGroupId, String serviceId, String category);

  int batchUpdateBaselineSetting(List<BaselineSettingDO> baselineSettingDOList);

  int deleteBaselineSetting(String sourceType, String networkId, String networkGroupId,
      String serviceId);

}
