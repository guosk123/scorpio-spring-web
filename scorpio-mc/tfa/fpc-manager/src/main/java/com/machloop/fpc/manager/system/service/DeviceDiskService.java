package com.machloop.fpc.manager.system.service;

import java.util.List;

import com.machloop.fpc.manager.system.bo.DeviceDiskBO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
public interface DeviceDiskService {

  List<DeviceDiskBO> queryDeviceDisks();

  int monitorDiskState();

}
