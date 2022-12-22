package com.machloop.fpc.manager.system.dao;

import java.util.List;

import com.machloop.fpc.manager.system.data.DeviceNetifDO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public interface DeviceNetifDao {

  List<DeviceNetifDO> queryDeviceNetifs(List<String> categoryList);

  int updateDeviceNetifs(List<DeviceNetifDO> netifDOList);

  int updateDeviceNetifState(String id, String state);
}
