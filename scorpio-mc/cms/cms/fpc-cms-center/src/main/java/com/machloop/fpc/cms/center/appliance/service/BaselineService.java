package com.machloop.fpc.cms.center.appliance.service;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.appliance.bo.BaselineSettingBO;
import com.machloop.fpc.cms.center.appliance.bo.BaselineValueBO;

import reactor.util.function.Tuple3;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public interface BaselineService {

  List<BaselineSettingBO> queryBaselineSettings(String sourceType, String networkId,
      String networkGroupId, String serviceId);

  List<BaselineSettingBO> querySubdivisionBaselineSettings(String sourceType, String networkId,
      String networkGroupId, String serviceId, String category);

  List<String> queryWindowingModelByInterval(int interval);
  

  /**
   * 获取前N周期时间段
   * @param currentStartTime
   * @param currentEndTime
   * @param baselineSetting
   * @param n
   * @return
   */
  Tuple3<Date, Date, Integer> queryPreviousPeriodTimePeriod(Date currentStartTime,
      Date currentEndTime, BaselineSettingBO baselineSetting, int n);

  int updateBaselineSettings(List<BaselineSettingBO> baselineSettings, String operatorId);

  int deleteBaselineSettings(String sourceType, String networkId, String networkGroupId,
      String serviceId);

  /************************************************************
   * 
   *************************************************************/

  /**
   * 查询基线值
   * @param sourceType 基线定义来源
   * @param sourceId 基线定义ID
   * @param startTime
   * @param endTime
   * @return
   */
  List<BaselineValueBO> queryBaselineValue(String sourceType, String sourceId, Date startTime,
      Date endTime);

}
