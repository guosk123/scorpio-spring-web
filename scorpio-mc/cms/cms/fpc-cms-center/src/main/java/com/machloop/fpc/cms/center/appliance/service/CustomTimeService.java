package com.machloop.fpc.cms.center.appliance.service;

import java.util.List;

import com.machloop.fpc.cms.center.appliance.bo.CustomTimeBO;

/**
 * @author minjiajun
 *
 * create at 2022年7月18日, fpc-cms-center
 */
public interface CustomTimeService {

  List<CustomTimeBO> queryCustomTimes(String type);

  CustomTimeBO queryCustomTime(String id);

  CustomTimeBO saveCustomTime(CustomTimeBO customTimeBO, String operatorId);

  CustomTimeBO updateCustomTime(String id, CustomTimeBO customTimeBO, String operatorId);

  int batchDeleteCustomTime(List<String> idList, String operatorId);

}
