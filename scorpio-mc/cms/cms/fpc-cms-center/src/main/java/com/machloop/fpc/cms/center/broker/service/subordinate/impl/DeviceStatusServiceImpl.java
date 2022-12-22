package com.machloop.fpc.cms.center.broker.service.subordinate.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.fpc.cms.center.broker.data.DeviceStatusDO;
import com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService;

/**
 * @author guosk
 *
 * create at 2021年11月3日, fpc-cms-center
 */
@Service
public class DeviceStatusServiceImpl implements DeviceStatusService {

  private static final Map<String, DeviceStatusDO> heartbeatMap = Maps.newConcurrentMap();

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService#queryDeviceStatus()
   */
  @Override
  public List<DeviceStatusDO> queryDeviceStatus() {
    return Lists.newArrayList(heartbeatMap.values());
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService#queryDeviceStatus(java.lang.String, java.lang.String)
   */
  @Override
  public DeviceStatusDO queryDeviceStatus(String deviceType, String serialNumber) {
    DeviceStatusDO deviceStatusDO = heartbeatMap
        .get(StringUtils.joinWith("_", deviceType, serialNumber));
    return deviceStatusDO == null ? new DeviceStatusDO() : deviceStatusDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService#refreshDeviceStatus(com.machloop.fpc.cms.center.broker.data.DeviceStatusDO)
   */
  @Override
  public synchronized void refreshDeviceStatus(DeviceStatusDO deviceStatus) {
    String deviceId = StringUtils.joinWith("_", deviceStatus.getDeviceType(),
        deviceStatus.getSerialNumber());

    DeviceStatusDO deviceStatusDO = heartbeatMap.get(deviceId);
    if (deviceStatusDO == null || deviceStatusDO.getLastInteractiveTime() == null
        || deviceStatus.getLastInteractiveTime() == null
        || deviceStatus.getLastInteractiveTime().after(deviceStatusDO.getLastInteractiveTime())) {
      heartbeatMap.put(deviceId, deviceStatus);
    }
  }

}
