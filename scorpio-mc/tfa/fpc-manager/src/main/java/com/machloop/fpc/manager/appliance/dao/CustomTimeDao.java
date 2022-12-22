package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;
import com.machloop.fpc.manager.appliance.data.CustomTimeDO;

/**
 * @author minjiajun
 *
 * create at 2022年6月9日, fpc-manager
 */
public interface CustomTimeDao {

  List<CustomTimeDO> queryCustomTimes(String type);

  CustomTimeDO queryCustomTime(String id);

  CustomTimeDO queryCustomTimeByName(String name);

  List<String> queryCustomTimeIds(boolean onlyLocal);

  List<String> queryAssignCustomTimeIds(Date beforeTime);

  CustomTimeDO saveCustomTime(CustomTimeDO customTimeDO);

  int updateCustomTime(CustomTimeDO customTimeDO);

  int deleteCustomTime(List<String> idList, String operatorId);

}
