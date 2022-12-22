package com.machloop.fpc.manager.appliance.service;

import java.util.List;

import com.machloop.fpc.manager.appliance.bo.CustomTimeBO;

/**
 * @author minjiajun
 *
 * create at 2022年6月9日, fpc-manager
 */
public interface CustomTimeService {

  List<CustomTimeBO> queryCustomTimes(String type);

  CustomTimeBO queryCustomTime(String id);

  CustomTimeBO saveCustomTime(CustomTimeBO customTimeBO, String operatorId);

  CustomTimeBO updateCustomTime(String id, CustomTimeBO customTimeBO, String operatorId);

  List<CustomTimeBO> batchDeleteCustomTime(List<String> idList, String operatorId);

}
