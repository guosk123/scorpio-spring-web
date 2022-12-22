package com.machloop.fpc.cms.center.broker.service.subordinate;

import java.util.List;

import com.machloop.fpc.cms.center.broker.data.DeviceStatusDO;

/**
 * @author guosk
 *
 * create at 2021年11月3日, fpc-cms-center
 */
public interface DeviceStatusService {

  List<DeviceStatusDO> queryDeviceStatus();

  DeviceStatusDO queryDeviceStatus(String deviceType, String serialNumber);

  void refreshDeviceStatus(DeviceStatusDO deviceStatus);

}
