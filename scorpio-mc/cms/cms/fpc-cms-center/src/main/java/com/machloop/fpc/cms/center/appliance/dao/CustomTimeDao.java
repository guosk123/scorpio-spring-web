package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.CustomTimeDO;

/**
 * @author minjiajun
 *
 * create at 2022年7月18日, fpc-cms-center
 */
public interface CustomTimeDao {

  List<CustomTimeDO> queryCustomTimes(String type);

  CustomTimeDO queryCustomTime(String id);

  CustomTimeDO queryCustomTimeByName(String name);

  List<String> queryCustomTimeIds(boolean onlyLocal);
  
  CustomTimeDO queryCustomTimeByAssignId(String assignId);
  
  List<CustomTimeDO> queryAssignCustomTimeIds(Date beforeTime);
  
  CustomTimeDO saveCustomTime(CustomTimeDO customTimeDO);

  int updateCustomTime(CustomTimeDO customTimeDO);

  int deleteCustomTime(List<String> idList, String operatorId);

}
