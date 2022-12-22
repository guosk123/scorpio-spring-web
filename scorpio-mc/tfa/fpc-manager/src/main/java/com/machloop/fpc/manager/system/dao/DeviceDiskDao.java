package com.machloop.fpc.manager.system.dao;

import java.util.List;

import com.machloop.fpc.manager.system.data.DeviceDiskDO;

/**
 * 
 * @author liumeng
 *
 * create at 2018年12月14日, fpc-manager
 */
public interface DeviceDiskDao {

  List<DeviceDiskDO> queryDeviceDisks();

  int saveOrUpdateDeviceDisk(DeviceDiskDO deviceDisk);

  int deleteDeviceDisk(String deviceId, String slotNo);

}
